package main.server.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.server.location.LocationDto;

import java.time.LocalDateTime;

import static stat.constant.Const.DATE_TIME_FORMAT;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {

    @Size(min = 20, max = 2000)
    @NotBlank
    String annotation;

    @NotNull
    Long category;

    @Size(min = 20, max = 7000)
    @NotBlank
    String description;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    @NotNull
    LocalDateTime eventDate;

    Boolean paid;

    @PositiveOrZero
    Long participantsLimit;

    Boolean requestModeration;

    @Size(min = 3, max = 120)
    @NotBlank
    String title;

    @NotNull
    LocationDto locationDto;
}
