package client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import stat.dto.EndpointHitDto;
import stat.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
@SuppressWarnings("unused")
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    // Use the format expected by the tests
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        // Use default values for now
        this.baseUrl = "http://stat-server:9090";
    }

    public ResponseEntity<EndpointHitDto> postHit(EndpointHitDto endpointHitDto) {
        String url = baseUrl + "/hit";
        return restTemplate.postForEntity(url, endpointHitDto, EndpointHitDto.class);
    }

    public List<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startStr = start.format(DATE_TIME_FORMATTER);
        String endStr = end.format(DATE_TIME_FORMATTER);

        StringBuilder urlBuilder = new StringBuilder(baseUrl)
                .append("/stats?start=").append(startStr)
                .append("&end=").append(endStr)
                .append("&unique=").append(unique);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                urlBuilder.append("&uris=").append(uri);
            }
        }

        String url = urlBuilder.toString();
        ResponseEntity<ViewStatsDto[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                ViewStatsDto[].class
        );
        return Arrays.asList(response.getBody());
    }
}