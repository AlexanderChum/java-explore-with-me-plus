package client;

import org.springframework.http.*;
import java.time.LocalDateTime;
import java.util.List;
import stat.dto.EndpointHitDto;
import stat.dto.ViewStatsDto;

public interface ClientBase {

        ResponseEntity<Void> postHit(ViewStatsDto dto);

        List<EndpointHitDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
