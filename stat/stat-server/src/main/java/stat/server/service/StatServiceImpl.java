package stat.server.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stat.dto.EndpointHitDto;
import stat.dto.ViewStatsDto;
import stat.server.exception.ValidationException;
import stat.server.mapper.StatMap;
import stat.server.model.EndpointHit;
import stat.server.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class StatServiceImpl implements StatService {
    StatMap statMap;
    StatRepository statRepository;

    @Override
    @Transactional
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        log.debug("Saving hit: app={}, uri={}, ip={}, timestamp={}",
                endpointHitDto.getApp(), endpointHitDto.getUri(),
                endpointHitDto.getIp(), endpointHitDto.getTimestamp());
        EndpointHit savedHit = statRepository.save(statMap.toEndpointHit(endpointHitDto));
        log.debug("Successfully saved hit with ID: {}", savedHit.getId());
        return statMap.toEndpointHitDto(savedHit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime startDate, LocalDateTime endDate, List<String> uris, Boolean unique) {
        log.debug("Service: Запрашиваем статистику с параметрами: start={}, end={}, uris={}, unique={}",
                startDate, endDate, uris, unique);

        if (startDate.isAfter(endDate)) {
            log.warn("Ошибка в датах: дата начала {} после даты окончания {}", startDate, endDate);
            throw new ValidationException("Дата начала не должна быть позже даты окончания");
        }

        List<ViewStatsDto> result;
        if (unique) {
            log.debug("Querying for unique stats");
            result = statRepository.getUniqueStats(startDate, endDate, uris);
        } else {
            log.debug("Querying for non-unique stats");
            result = statRepository.getStats(startDate, endDate, uris);
        }

        log.debug("Service: Получен результат: {} ({} entries)", result, result != null ? result.size() : 0);
        return result;
    }
}