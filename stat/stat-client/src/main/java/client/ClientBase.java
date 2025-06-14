package client;

import org.springframework.http.ResponseEntity;
import stat.dto.EndpointHitDto;
import stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
public interface ClientBase {

    ResponseEntity<EndpointHitDto> postHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
