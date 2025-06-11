package client;

import org.springframework.beans.factory.annotation.Value;
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

@Service
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public StatsClient(@Value("http://localhost:9090")String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Void> postHit(EndpointHitDto endpointHit) {
        String url = baseUrl + "/hit";
        return restTemplate.postForEntity(url, endpointHit, Void.class);
    }

    public List<StatDto > getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startStr = start.atZone(ZoneOffset.UTC).toString();
        String endStr = end.atZone(ZoneOffset.UTC).toString();

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
        ResponseEntity<StatDto[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                StatDto[].class
        );
        return Arrays.asList(response.getBody());
    }
}
