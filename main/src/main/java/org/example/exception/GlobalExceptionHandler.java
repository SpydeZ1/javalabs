package org.example.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "Некорректное тело запроса");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return error(HttpStatus.BAD_REQUEST, "Параметр '" + ex.getName() + "' имеет неверный формат");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex) {
        log.warn("Конфликт версий: {}", ex.getMessage());
        return error(HttpStatus.CONFLICT, "Данные изменены другим запросом, повторите операцию");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Нарушение ограничения БД: {}", ex.getMostSpecificCause().getMessage());
        return error(HttpStatus.CONFLICT, "Нарушено ограничение целостности данных");
    }

    /**
     * Последний рубеж. Стандартные ошибки Spring MVC (404 на неизвестный URL, 405, 415 и т.п.)
     * реализуют {@link org.springframework.web.ErrorResponse} и сохраняют свой статус,
     * всё остальное — 500 с записью в лог.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        if (ex instanceof org.springframework.web.ErrorResponse springError
                && !springError.getStatusCode().is5xxServerError()) {
            HttpStatusCode code = springError.getStatusCode();
            log.debug("Ошибка запроса {}: {}", code.value(), ex.getMessage());
            return ResponseEntity.status(code)
                    .headers(springError.getHeaders())
                    .body(new ErrorResponse(code.value(), clientMessage(code, springError)));
        }
        log.error("Необработанная ошибка", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    private String clientMessage(HttpStatusCode code, org.springframework.web.ErrorResponse springError) {
        return switch (code.value()) {
            case 404 -> "Ресурс не найден";
            case 405 -> "Метод не поддерживается";
            case 406 -> "Недопустимый формат ответа";
            case 415 -> "Неподдерживаемый тип содержимого";
            default -> springError.getBody().getDetail() != null
                    ? springError.getBody().getDetail()
                    : "Некорректный запрос";
        };
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }
}
