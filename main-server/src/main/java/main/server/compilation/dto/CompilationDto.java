package main.server.compilation.dto;

import lombok.Builder;
import lombok.Data;
import main.server.events.dto.EventShortDto;

import java.util.Set;

@Data
@Builder
public class CompilationDto {
    private Set<EventShortDto> events;
    private Long id;
    private Boolean pinned;
    private String title;
}
