package client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import stat.dto.EndpointHitDto;
import stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
@SuppressWarnings("unused")
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("http://localhost:9090") String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<EndpointHitDto> postHit(EndpointHitDto endpointHitDto) {
        String url = baseUrl + "/hit";
        return restTemplate.postForEntity(url, endpointHitDto, EndpointHitDto.class);
    }

    public List<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startStr = start.format(formatter);
        String endStr = end.format(formatter);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/stats")
                .queryParam("start", startStr)
                .queryParam("end", endStr)
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            String urisParam = String.join(",", uris);
            builder.queryParam("uris", urisParam);
        }

        String url = builder.toUriString();
        ResponseEntity<ViewStatsDto[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                ViewStatsDto[].class
        );
        return Arrays.asList(response.getBody());
    }
}
