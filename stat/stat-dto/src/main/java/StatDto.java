import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatDto {
    Long id;

    @NotBlank(message = "Параметр app не должен быть пустым")
    @Size(max = 200, message = "Слишком длинный параметр app")
    String app;

    @Size(max = 200, message = "Слишком длинный URI")
    @Pattern(regexp = "^/.*", message = "URI должен начинаться со слеша")
    String uri;

    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])\\.)" +
            "{3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])$",
            message = "Некорректный ip адрес")
    String ip;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;
    /*в спецификации указан тип String для данного поля, но я не уверен что он подходит учитывая то что в будущем мы
    будем работать со временем
     */
}
