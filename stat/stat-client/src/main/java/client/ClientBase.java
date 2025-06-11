package client;

import org.springframework.http.*;
import java.time.LocalDateTime;
import java.util.List;

public interface ClientBase {

        ResponseEntity<Void> postHit(StatDto dto);

        List<EndpointHitDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
