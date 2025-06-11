package stat.server.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stat.dto.EndpointHitDto;
import stat.dto.ViewStatsDto;
import stat.server.mapper.StatMap;
import stat.server.mapper.StatMapper;
import stat.server.model.EndpointHit;
import stat.server.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class StatServiceImpl implements StatService {
    StatMap statMap;
    StatRepository repository;

    @Override
    @Transactional
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = StatMapper.toEndpointHit(endpointHitDto);
        return statMap.toEndpointHitDto(repository.save(endpointHit));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        // Возвращается List<StatDto>?
        // Или List<EndpointHitDto>?
        // TODO: дописать логику
        return List.of();
    }
}