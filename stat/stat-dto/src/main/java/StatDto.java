import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StatDto {
    Integer id;

    @NotBlank
    String app;

    @NotBlank
    String uri;

    @NotBlank
    @Size(max = 15)
    String ip;

    @NotBlank
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;
    /*в спецификации указан тип String для данного поля, но я не уверен что он подходит учитывая то что в будущем мы
    будем работать со временем
     */
}
