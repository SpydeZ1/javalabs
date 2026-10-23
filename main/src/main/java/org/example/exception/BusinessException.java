package org.example.exception;

/** Ошибка бизнес-логики. О HTTP знает только контроллерный слой (GlobalExceptionHandler). */
public class BusinessException extends RuntimeException {

    public enum Kind { NOT_FOUND, INVALID, CONFLICT }

    private final Kind kind;

    public BusinessException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public static BusinessException notFound(String what, Long id) {
        return new BusinessException(Kind.NOT_FOUND, what + " not found: " + id);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(Kind.INVALID, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(Kind.CONFLICT, message);
    }
}
