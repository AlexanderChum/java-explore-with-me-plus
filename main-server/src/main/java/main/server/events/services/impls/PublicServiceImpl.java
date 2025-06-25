package main.server.events.services.impls;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.server.events.dto.EventPublicParams;
import main.server.events.dto.EventShortDto;
import main.server.events.enums.EventState;
import main.server.events.enums.SortOptions;
import main.server.events.mapper.EventMapper;
import main.server.events.model.EventModel;
import main.server.events.model.QEventModel;
import main.server.events.repository.EventRepository;
import main.server.events.services.PublicService;
import main.server.exception.NotFoundException;
import main.server.request.RequestRepository;
import main.server.request.model.RequestStatus;
import main.server.statserver.StatsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicServiceImpl implements PublicService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final JPAQueryFactory jpaQueryFactory;
    private final RequestRepository requestRepository;
    private final StatsService statsService;

    public List<EventShortDto> getEventsWithFilters(EventPublicParams eventParams, HttpServletRequest request) {
        // Фиксируем обращение к эндпоинту
        statsService.addView(request);

        QEventModel event = QEventModel.eventModel;
        JPAQuery<EventModel> query = jpaQueryFactory.selectFrom(event)
                .where(event.state.eq(EventState.PUBLISHED)); // Только опубликованные события

        // Текст в аннотации или описании (регистронезависимый поиск)
        if (eventParams.getText() != null) {
            String searchText = eventParams.getText().toLowerCase();
            BooleanExpression textInAnnotation = event.annotation.lower().like("%" + searchText + "%");
            BooleanExpression textInDescription = event.description.lower().like("%" + searchText + "%");
            query.where(textInAnnotation.or(textInDescription));
        }

        // Фильтр по категориям
        if (eventParams.getCategories() != null && !eventParams.getCategories().isEmpty()) {
            query.where(event.category.id.in(eventParams.getCategories()));
        }

        // Фильтр по платности
        if (eventParams.getPaid() != null) {
            query.where(event.paid.eq(eventParams.getPaid()));
        }

        // Фильтр по датам
        if (eventParams.getRangeStart() != null) {
            query.where(event.eventDate.after(eventParams.getRangeStart()));
        } else if (eventParams.getRangeEnd() != null) {
            query.where(event.eventDate.before(eventParams.getRangeEnd()));
        } else {
            // Если даты не указаны - показываем будущие события
            query.where(event.eventDate.after(LocalDateTime.now()));
        }

        // Сортировка
        if (eventParams.getSortOptions() != null && eventParams.getSortOptions() == SortOptions.EVENT_DATE) {
            query.orderBy(event.eventDate.asc());
        } else {
            // по умолчанию сортировка по дате
            query.orderBy(event.eventDate.asc());
        }

        // Пагинация
        query.offset(eventParams.getFrom())
                .limit(eventParams.getSize());

        // Выполняем запрос
        List<EventModel> events = query.fetch();

        // Получаем ID событий для статистики
        List<Long> eventIds = events.stream()
                .map(EventModel::getId)
                .collect(Collectors.toList());

        // Получаем просмотры для всех событий за один вызов
        Map<Long, Long> viewsMap = statsService.getAmountForEvents(events);

        // Если сортировка по просмотрам, сортируем в памяти
        if (eventParams.getSortOptions() != null && eventParams.getSortOptions() == SortOptions.VIEWS) {
            events.sort((e1, e2) -> Long.compare(
                    viewsMap.getOrDefault(e2.getId(), 0L),
                    viewsMap.getOrDefault(e1.getId(), 0L)
            ));
        }

        // Получаем подтвержденные заявки
        List<Object[]> results = requestRepository.countConfirmedRequestsByEventIds(eventIds, RequestStatus.CONFIRMED);
        Map<Long, Long> confirmedRequests = results.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        // Формируем DTO
        return events.stream()
                .map(e -> eventMapper.toShortDto(
                        e,
                        confirmedRequests.getOrDefault(e.getId(), 0L),
                        viewsMap.getOrDefault(e.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    public EventShortDto getEventById(Long eventId, HttpServletRequest request) {
        statsService.addView(request);

        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d не было найдено",eventId)));

        Long views = statsService.getAmount(eventId, event.getCreatedOn(), LocalDateTime.now());

        EventShortDto result = eventMapper.toShortDto(event);
        result.setViews(views);

        return result;
    }
}
