package main.server.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import main.server.category.dto.CategoryDto;
import main.server.events.enums.EventState;
import main.server.location.LocationDto;
import main.server.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static stat.constant.Const.DATE_TIME_FORMAT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto {
    Long id;
    String annotation;

    @JsonProperty("category")
    CategoryDto categoryDto;

    Long confirmedRequests;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime createdOn;

    String description;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    UserShortDto initiator;

    @JsonProperty("location")
    LocationDto locationDto;

    Boolean paid;
    Long participantLimit;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime publishedOn;
    Boolean requestModeration;
    EventState state;
    String title;
    Long views;
}
