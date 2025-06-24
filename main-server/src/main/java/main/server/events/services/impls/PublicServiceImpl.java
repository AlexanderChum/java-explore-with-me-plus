package main.server.events.services.impls;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.server.events.dto.EventPublicParams;
import main.server.events.dto.EventShortDto;
import main.server.events.mapper.EventMapper;
import main.server.events.model.EventModel;
import main.server.events.repository.EventRepository;
import main.server.events.services.PublicService;
import main.server.exception.NotFoundException;
import main.server.statserver.StatsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicServiceImpl implements PublicService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public List<EventShortDto> getEventsWithFilters(EventPublicParams eventParams, HttpServletRequest request) {
        /*StatsService.addView(request);

        //спросить у Андрея, есть ли опция написать запрос с помощью Q класса

        StatsService.getAmountForEvents(//сюда список событий, полученных с помощью запроса в бд)
        );

        //дальше для каждого события достаем количество просмотров из мапы, полученной с сервера статистики

         */
        return null;
    }

    public EventShortDto getEventById(Long eventId, HttpServletRequest request) {
        StatsService.addView(request);

        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не было найдено"));

        Long views = StatsService.getAmount(eventId, event.getCreatedOn(), LocalDateTime.now());

        EventShortDto result = eventMapper.toShortDto(event);
        result.setViews(views);

        return result;
    }
}
