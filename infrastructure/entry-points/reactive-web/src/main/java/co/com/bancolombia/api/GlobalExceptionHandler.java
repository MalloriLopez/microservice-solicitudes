package co.com.bancolombia.api;

import co.com.bancolombia.model.exceptions.ExternalServiceCommunicationException;
import io.r2dbc.spi.R2dbcException;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Forbidden");
        problem.setDetail("No tienes permisos para realizar esta acción");
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem));
    }

    @ExceptionHandler({InvalidBearerTokenException.class, AuthenticationException.class})
    public Mono<ResponseEntity<ProblemDetail>> handleAuthErrors(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized");
        problem.setDetail("Token inválido o expirado");
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem));
    }

    @ExceptionHandler(ExternalServiceCommunicationException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleExternalService(ExternalServiceCommunicationException ex) {
        log.error("Fallo de comunicación con servicio externo: service={}, endpoint={}, msg={}",
                ex.getService(), ex.getEndpoint(), ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setTitle("Servicio externo no disponible");
        problem.setDetail(ex.getMessage());

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem));
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleValidationException(ValidationException ex) {
        log.warn("Error de validación en DTO: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(400);
        problem.setTitle("Bad Request");
        problem.setDetail(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Error de validación: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(400);
        problem.setTitle("Bad Request");
        problem.setDetail(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem));
    }

    @ExceptionHandler(DecodingException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleDecodingException(DecodingException ex) {
        log.error("Error de deserialización: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(400);
        problem.setTitle("Bad Request");
        problem.setDetail("Formato de solicitud inválido");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        ProblemDetail problem = ProblemDetail.forStatus(status);

        if (status == HttpStatus.NOT_FOUND) {
            log.warn("Recurso no encontrado: {}", ex.getMessage());
            problem.setTitle("Not Found");
            problem.setDetail("El recurso solicitado no existe");
        } else {
            log.error("Error de estado {}: {}", status.value(), ex.getMessage());
            problem.setTitle(status.getReasonPhrase());
            problem.setDetail(ex.getReason() != null ? ex.getReason() : "Ocurrió un error al procesar la solicitud");
        }
        return Mono.just(ResponseEntity.status(status).body(problem));
    }


    @ExceptionHandler(R2dbcException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleR2dbcBadGrammarException(R2dbcException ex) {
        log.error("Error con la base de datos: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(500);
        problem.setTitle("Internal Server Error");
        problem.setDetail("Ocurrió un error en el servidor");
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem));
    }

    @ExceptionHandler(ConnectException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleConnectException(ConnectException ex) {
        log.error("No se pudo conectar a la base de datos: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(500);
        problem.setTitle("Internal Server Error");
        problem.setDetail("Ocurrió un error en el servidor");
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem));
    }
}
