package client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.RequestEntity.post;

@Service
public class StatsClient {

    private final RestTemplate restTemplate;

    @Value("http://localhost:9090")
    private String statsServiceUrl;

    public StatsClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public ResponseEntity<?> postHit(StatDto dto) {
        return post("/hit", dto);
    }

    public List<StatAnswer> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        long startTimestamp = start.toInstant(ZoneOffset.UTC).toEpochMilli();
        long endTimestamp = end.toInstant(ZoneOffset.UTC).toEpochMilli();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(statsServiceUrl)
                .path("/stats")
                .queryParam("start", startTimestamp)
                .queryParam("end", endTimestamp)
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            String urisParam = String.join(",", uris);
            builder.queryParam("uris", urisParam);
        }

        String url = builder.toUriString();
        ResponseEntity<StatAnswer[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                StatAnswer[].class
        );
        return Arrays.asList(response.getBody());
    }
}
