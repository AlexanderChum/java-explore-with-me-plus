package stat.server.service;

import stat.dto.EndpointHitDto;
import stat.dto.StatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {
    EndpointHitDto saveHit(EndpointHitDto endpointHitDto);

    List<StatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
