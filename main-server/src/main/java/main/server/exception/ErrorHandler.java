package main.server.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice()
@SuppressWarnings("unused")
public class ErrorHandler {
    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validationExceptionHandle(final MethodArgumentNotValidException e, final ValidationException v) {
        log.error("Validation error: ", e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Ошибка валидации")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError repositoryDuplicatedDataExceptionHandle(final DuplicatedDataException e) {
        log.error("Duplicated Data Exception error: ", e);
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Ресурс дублируется")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError repositoryNotFoundExceptionHandle(final NotFoundException e) {
        log.error("Not Found Exception error: ", e);
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("Запрашиваемый ресурс не найден")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError exceptionHandle(final ConflictException e) {
        log.error("CONFLICT error: ", e);
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Нарушено ограничение целостности")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequestExceptionHandle(final BadRequestException e) {
        log.error("Bad Request Exception error: ", e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Некорректный запрос")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Throwable e) {
        log.error("Internal Server Error: ", e);
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .reason("Внутренняя ошибка сервера")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}