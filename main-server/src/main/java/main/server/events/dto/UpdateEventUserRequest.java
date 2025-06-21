package main.server.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.server.events.CategoryDto;
import main.server.events.enums.StateAction;
import main.server.location.LocationDto;

import java.time.LocalDateTime;

import static stat.constant.Const.DATE_TIME_FORMAT;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000)
    String annotation;

    CategoryDto category;

    @Size(min = 20, max = 7000)
    String description;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    Boolean paid;

    @PositiveOrZero
    Long participantLimit;

    Boolean requestModeration;

    @JsonProperty("stateAction")
    StateAction state;

    @Size(min = 3, max = 120)
    String title;

    LocationDto location;
}
