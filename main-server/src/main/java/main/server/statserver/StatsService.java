package main.server.statserver;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import main.server.events.model.EventModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static stat.constant.Const.DATE_TIME_FORMAT;

@Slf4j
@UtilityClass
public class StatsService {
    private static final String APP_NAME = "main_svc";
    private static final String STATS_SERVICE_SCHEME = "http";
    private static final String STATS_SERVICE_HOST = "stat-server";
    private static final Integer STATS_SERVICE_PORT = 9090;
    private final RestTemplate restTemplate = new RestTemplate();

    public void addView(HttpServletRequest request) {
        log.info("Отправка запроса в сервис статистики");
        final String url = UriComponentsBuilder.newInstance()
                .scheme(STATS_SERVICE_SCHEME)
                .host(STATS_SERVICE_HOST)
                .port(STATS_SERVICE_PORT)
                .path("/hit")
                .toUriString();

        final ViewDto viewDto = ViewDto.builder()
                .app(APP_NAME)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                .build();

        final HttpMethod method = HttpMethod.POST;
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        final HttpEntity<Object> requestBody = new HttpEntity<>(viewDto, headers);

        ResponseEntity<Object> response = restTemplate.exchange(url, method, requestBody, Object.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            log.info("Просмотр события сохранен");
        } else {
            log.error("Ошибка при сохранении просмотра");
        }
    }

    public Long getAmount(Long eventId, LocalDateTime start, LocalDateTime end) {
        final List<String> uris = List.of("/events/" + eventId);
        final String url = UriComponentsBuilder.newInstance()
                .scheme(STATS_SERVICE_SCHEME)
                .host(STATS_SERVICE_HOST)
                .port(STATS_SERVICE_PORT)
                .path("/stats")
                .queryParam("start", start.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                .queryParam("end", end.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                .queryParam("uris", uris)
                .queryParam("unique", "true")
                .toUriString();

        final HttpMethod method = HttpMethod.GET;
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        final HttpEntity<Object> requestBody = new HttpEntity<>(null, headers);

        final ResponseEntity<String> response = restTemplate.exchange(url, method, requestBody, String.class);

        long viewsAmount = 0L;

        if (response.getStatusCode() != HttpStatus.OK) return viewsAmount;

        StatsDto[] responses = new Gson().fromJson(response.getBody(), StatsDto[].class);
        viewsAmount = Arrays.stream(responses)
                .mapToLong(StatsDto::getHits)
                .sum();

        return viewsAmount;
    }

    public Map<Long, Long> getAmountForEvents(List<EventModel> events) {
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        LocalDateTime minDate = events.stream()
                .map(EventModel::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now()); //на всякий случай оставлю тут комментарий о возможной проблеме со часами

        String url = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("stat-server")
                .port(9090)
                .path("/stats")
                .queryParam("start", minDate.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                .queryParam("end", LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                .queryParam("uris", uris)
                .queryParam("unique", "true")
                .toUriString();

        ResponseEntity<StatsDto[]> response = restTemplate.getForEntity(url, StatsDto[].class);

        return Arrays.stream(response.getBody())
                .collect(Collectors.toMap(
                        dto -> Long.parseLong(dto.getUri().split("/")[2]), //либо 1, если получаем обратно uri вида event/123
                        StatsDto::getHits
                ));
    }
}
