package main.server.events.mapper;

import lombok.experimental.UtilityClass;
import main.server.category.mapper.CategoryMapper;
import main.server.events.dto.EventFullDto;
import main.server.events.dto.EventShortDto;
import main.server.events.dto.NewEventDto;
import main.server.events.dto.UpdateEventAdminRequest;
import main.server.events.dto.UpdateEventUserRequest;
import main.server.events.model.EventModel;
import main.server.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static stat.constant.Const.DATE_TIME_FORMAT;

@Mapper(componentModel = "spring",
        uses = {CategoryMapper.class, UserMapper.class}) //возможно потребуется маппер для location, чисто символический
public interface EventMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "confirmedRequests", constant = "0L")
    @Mapping(target = "views", constant = "0L")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())", dateFormat = DATE_TIME_FORMAT)
    @Mapping(source = "dto.category", target = "category.id")
    @Mapping(source = "locationDto", target = "location")
    EventModel toEntity(NewEventDto dto);

    @Mapping(source = "category", target = "categoryDto")
    @Mapping(source = "location", target = "locationDto")
    @Mapping(source = "initiator", target = "initiatorShortDto")
    @Mapping(source = "confirmedRequests", target = "confirmedRequests")
    EventFullDto toFullDto(EventModel entity);

    @Mapping(source = "category", target = "categoryDto")
    @Mapping(source = "initiator", target = "initiatorDto")
    EventShortDto toShortDto(EventModel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(source = "categoryDto", target = "category.id")
    @Mapping(source = "locationDto", target = "location")
    void updateFromAdminRequest(UpdateEventAdminRequest dto, @MappingTarget EventModel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(source = "categoryDto", target = "category.id")
    @Mapping(source = "locationDto", target = "location")
    void updateFromUserRequest(UpdateEventUserRequest dto, @MappingTarget EventModel entity);
}
