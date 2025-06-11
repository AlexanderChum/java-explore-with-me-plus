package stat.server.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stat.dto.EndpointHitDto;
import stat.dto.StatDto;
import stat.server.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatServiceImpl implements StatService {

    StatRepository repository;

    @Override
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        return null;
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        return List.of();
    }
}