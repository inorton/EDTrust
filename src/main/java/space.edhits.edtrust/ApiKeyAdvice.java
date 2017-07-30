package space.edhits.edtrust;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.jws.WebResult;

/**
 * Api key related exception handling
 */
@ControllerAdvice
public class ApiKeyAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {UnauthorizedApiKey.class})
    protected ResponseEntity<Object> handleUnauthorizedApiKey(RuntimeException exception, WebRequest request) {
        return handleExceptionInternal(exception,
                "unauthorized apikey",
                new HttpHeaders(),
                HttpStatus.UNAUTHORIZED, request);
    }
}
