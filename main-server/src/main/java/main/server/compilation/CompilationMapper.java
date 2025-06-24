package main.server.compilation;

import lombok.Getter;
import main.server.compilation.dto.CompilationDto;
import main.server.compilation.dto.NewCompilationDto;
import main.server.compilation.model.Compilation;
import main.server.events.mapper.EventMapper;
import main.server.events.model.EventModel;
import main.server.events.repository.EventRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    Compilation toEntity(NewCompilationDto dto, @org.mapstruct.MappingTarget MapperContext context);

    CompilationDto toDto(Compilation compilation);

    List<CompilationDto> toDtoList(List<Compilation> compilations);


    default Set<EventModel> map(Set<Long> eventIds, @org.mapstruct.MappingTarget MapperContext context) {
        return eventIds.stream()
                .map(id -> mapToEventModel(id, context))
                .collect(Collectors.toSet());
    }

    default EventModel mapToEventModel(Long eventId, @org.mapstruct.MappingTarget MapperContext context) {
        return context.getEventRepository().findById(eventId).orElse(null); // Можно выбросить исключение при необходимости
    }

    @Getter
    class MapperContext {
        private final EventRepository eventRepository;

        public MapperContext(EventRepository eventRepository) {
            this.eventRepository = eventRepository;
        }
    }
}

