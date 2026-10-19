package io.github.teggr.teavmshowcase.currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ConversionController.class)
public class ApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ConversionException.class)
    public ResponseEntity<ConversionError> handleConversion(ConversionException exception) {
        return ResponseEntity.badRequest()
                .body(new ConversionError(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ConversionError> handleMalformedJson(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(new ConversionError("MALFORMED_JSON", "Request body must be valid conversion JSON."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ConversionError> handleUnexpected(Exception exception) {
        logger.error("Unexpected error while processing a conversion request", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ConversionError("INTERNAL_ERROR", "The conversion service encountered an error."));
    }
}
