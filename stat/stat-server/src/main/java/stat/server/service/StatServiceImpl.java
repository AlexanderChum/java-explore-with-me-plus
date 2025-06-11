package stat.server.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stat.dto.StatDto;
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
public class StatServiceImpl implements StatService {

    StatRepository repository;

    @Override
    @Transactional
    public void saveHit(StatDto statDto) {
        EndpointHit endpointHit = StatMapper.toEndpointHit(statDto);
        repository.save(endpointHit);
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        // Возвращается List<StatDto>?
        // Или List<EndpointHitDto>?
        // TODO: дописать логику
        return List.of();
    }
}