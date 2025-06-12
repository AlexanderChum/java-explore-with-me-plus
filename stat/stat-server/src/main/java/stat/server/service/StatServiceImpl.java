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
    StatRepository statRepository;

    @Override
    @Transactional
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        return statMap.toEndpointHitDto(statRepository.save(statMap.toEndpointHit(endpointHitDto)));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime startDate, LocalDateTime endDate, List<String> uris, Boolean unique) {
        log.debug("Service: Запрашиваем статистику с параметрами: start={}, end={}, uris={}, unique={}",
                startDate, endDate, uris, unique);

        if (startDate.isAfter(endDate)) {
            log.warn("Ошибка в датах: дата начала {} после даты окончания {}", startDate, endDate);
            throw new ValidationException("Дата начала не должна быть позже даты окончания");
        }

        if (unique) {
            return statRepository.getUniqueStats(startDate, endDate, uris);
        } else {
            return statRepository.getStats(startDate, endDate, uris);
        }
    }
}