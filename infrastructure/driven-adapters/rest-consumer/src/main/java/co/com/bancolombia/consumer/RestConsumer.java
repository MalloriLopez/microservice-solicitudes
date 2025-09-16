package co.com.bancolombia.consumer;

import co.com.bancolombia.model.client.UserClientDetails;
import co.com.bancolombia.model.loanapplication.gateways.IRestConsumerUserClient;
import co.com.bancolombia.model.exceptions.ExternalServiceCommunicationException;
import co.com.bancolombia.model.userquery.gateways.IUserQueryClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class RestConsumer implements IRestConsumerUserClient {

    private static final String SERVICE_NAME = "user-service";
    private static final String PATH_VALIDATE_USER_BY_EMAIL = "/api/v1/users/email/{email}/exists";


    private final WebClient authClient;

    public RestConsumer(@Qualifier("authClient") WebClient authClient) {
        this.authClient = authClient;
    }


    @Override
    @CircuitBreaker(name = "existsUserByEmail", fallbackMethod = "validateUserFallback")
    public Mono<Boolean> existsUserByEmail(String email) {
        return authClient.get()
                .uri(PATH_VALIDATE_USER_BY_EMAIL, email)
                .retrieve()
                .bodyToMono(ExistsUserResponse.class)
                .map(ExistsUserResponse::getExistsUser);
    }

    @Override
    public Mono<UserClientDetails> getUserByEmail(String email) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (JwtAuthenticationToken) ctx.getAuthentication())
                .map(auth -> auth.getToken().getTokenValue())
                .flatMap(token -> authClient.get()
                        .uri("/api/v1/users/email/{email}/summary", email)
                        .headers(h -> h.setBearerAuth(token))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, resp -> resp.createException().flatMap(Mono::error))
                        .bodyToMono(UserClientDetails.class)
                );
    }

    private Mono<Boolean> validateUserFallback(String email, Throwable cause) {
        log.error("Fallback activado para validar email {}. Causa: {}", email, cause.toString());
        return Mono.error(new ExternalServiceCommunicationException(
                SERVICE_NAME,
                PATH_VALIDATE_USER_BY_EMAIL,
                "Problemas de comunicación con el servicio externo, no es posible procesar la solicitud de validación de usuario.",
                cause
        ));
    }
}