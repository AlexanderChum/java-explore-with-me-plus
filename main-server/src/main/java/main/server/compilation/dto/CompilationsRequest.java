package main.server.compilation.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
public class CompilationsRequest {
    private Boolean pinned;
}
