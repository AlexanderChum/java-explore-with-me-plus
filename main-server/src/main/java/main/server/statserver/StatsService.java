package main.server.statserver;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import main.server.events.model.EventModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static stat.constant.Const.DATE_TIME_FORMAT;

@Slf4j
@Service
public class StatsService {
    private static final String APP_NAME = "main_svc";

    @Value("${stats.service.enabled:true}")
    private boolean statsServiceEnabled;

    @Value("${stats.service.scheme:http}")
    private String statsServiceScheme;

    @Value("${stats.service.host:stat-server}")
    private String statsServiceHost;

    @Value("${stats.service.port:9090}")
    private Integer statsServicePort;

    private final RestTemplate restTemplate;

    @Autowired
    public StatsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void addView(HttpServletRequest request) {
        try {
            log.info("Отправка запроса в сервис статистики для URI: {}", request.getRequestURI());
            final String url = UriComponentsBuilder.newInstance()
                    .scheme(statsServiceScheme)
                    .host(statsServiceHost)
                    .port(statsServicePort)
                    .path("/hit")
                    .toUriString();

            log.debug("URL для отправки статистики: {}", url);

            final ViewDto viewDto = ViewDto.builder()
                    .app(APP_NAME)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();

            log.debug("Отправляемые данные: {}", viewDto);

            final HttpMethod method = HttpMethod.POST;
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            final HttpEntity<Object> requestBody = new HttpEntity<>(viewDto, headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, method, requestBody, Object.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("Просмотр события сохранен успешно");
            } else {
                log.error("Ошибка при сохранении просмотра: {}", response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Ошибка при отправке запроса в сервис статистики: {}", e.getMessage());
            log.debug("Детали ошибки:", e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при сохранении просмотра: {}", e.getMessage());
            log.debug("Детали ошибки:", e);
        }
    }

    public Long getAmount(Long eventId, LocalDateTime start, LocalDateTime end) {
        try {
            final List<String> uris = List.of("/events/" + eventId);
            final String url = UriComponentsBuilder.newInstance()
                    .scheme(statsServiceScheme)
                    .host(statsServiceHost)
                    .port(statsServicePort)
                    .path("/stats")
                    .queryParam("start", start.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                    .queryParam("end", end.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                    .queryParam("uris", uris)
                    .queryParam("unique", "true")
                    .toUriString();

            log.debug("Запрос статистики для события {}: {}", eventId, url);

            final HttpMethod method = HttpMethod.GET;
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            final HttpEntity<Object> requestBody = new HttpEntity<>(null, headers);

            final ResponseEntity<String> response = restTemplate.exchange(url, method, requestBody, String.class);

            long viewsAmount = 0L;

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.warn("Не удалось получить статистику для события {}: {}", eventId, response.getStatusCode());
                return viewsAmount;
            }

            StatsDto[] responses = new Gson().fromJson(response.getBody(), StatsDto[].class);
            if (responses != null) {
                viewsAmount = Arrays.stream(responses)
                        .mapToLong(StatsDto::getHits)
                        .sum();
            }

            log.debug("Получено {} просмотров для события {}", viewsAmount, eventId);
            return viewsAmount;
        } catch (RestClientException e) {
            log.error("Ошибка при получении статистики для события {}: {}", eventId, e.getMessage());
            return 0L;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении статистики для события {}: {}", eventId, e.getMessage());
            return 0L;
        }
    }

    public Map<Long, Long> getAmountForEvents(List<EventModel> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<String> uris = events.stream()
                    .map(event -> "/events/" + event.getId())
                    .toList();

            LocalDateTime minDate = events.stream()
                    .map(EventModel::getCreatedOn)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now().minusDays(1));

            String url = UriComponentsBuilder.newInstance()
                    .scheme(statsServiceScheme)
                    .host(statsServiceHost)
                    .port(statsServicePort)
                    .path("/stats")
                    .queryParam("start", minDate.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                    .queryParam("end", LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                    .queryParam("uris", uris)
                    .queryParam("unique", "true")
                    .toUriString();

            log.debug("Запрос статистики для {} событий: {}", events.size(), url);

            ResponseEntity<StatsDto[]> response = restTemplate.getForEntity(url, StatsDto[].class);

            if (response.getBody() == null) {
                log.warn("Получен пустой ответ от сервиса статистики");
                return Collections.emptyMap();
            }

            Map<Long, Long> result = Arrays.stream(response.getBody())
                    .filter(dto -> dto.getUri() != null && dto.getUri().split("/").length >= 3)
                    .collect(Collectors.toMap(
                            dto -> {
                                try {
                                    return Long.parseLong(dto.getUri().split("/")[2]);
                                } catch (NumberFormatException e) {
                                    log.error("Не удалось распарсить ID события из URI: {}", dto.getUri());
                                    return -1L;
                                }
                            },
                            StatsDto::getHits,
                            (existing, replacement) -> existing
                    ));

            log.debug("Получена статистика для {} событий", result.size());
            return result;
        } catch (RestClientException e) {
            log.error("Ошибка при получении статистики для событий: {}", e.getMessage());
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении статистики для событий: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
