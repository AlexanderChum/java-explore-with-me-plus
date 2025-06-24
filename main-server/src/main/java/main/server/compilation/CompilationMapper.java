package main.server.compilation;

import main.server.compilation.dto.CompilationDto;
import main.server.compilation.dto.NewCompilationDto;
import main.server.compilation.model.Compilation;
import main.server.events.mapper.EventMapper;
import main.server.events.model.EventModel;
import main.server.events.repository.EventRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
@Component
public interface CompilationMapper {
    @Autowired
    EventRepository eventRepository = null;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    Compilation toEntity(NewCompilationDto dto);

    CompilationDto toDto(Compilation compilation);

    List<CompilationDto> toDtoList(List<Compilation> compilations);

    ///////// далее эксперимент, так как есть проблема при маппинге Set<Long> в Set<EventModel>, не компилировалось
    default Set<EventModel> map(Set<Long> eventIds, EventRepository eventRepository) {
        return eventIds.stream()
                .map(eventId -> mapToEventModel(eventId, eventRepository)) // передаем eventRepository
                .collect(Collectors.toSet());
    }

    default EventModel mapToEventModel(Long eventId, EventRepository eventRepository) {
        return eventRepository.findById(eventId).orElse(null);
    }
}