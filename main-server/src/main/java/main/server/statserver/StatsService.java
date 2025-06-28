package main.server.statserver;

import lombok.extern.slf4j.Slf4j;
import main.server.events.model.EventModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public Map<Long, Long> getViewsAmount(List<EventModel> events) {
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
