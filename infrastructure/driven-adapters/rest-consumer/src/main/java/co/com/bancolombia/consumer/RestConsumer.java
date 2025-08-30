package co.com.bancolombia.consumer;

import co.com.bancolombia.model.loanapplication.gateways.IRestConsumerUserClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestConsumer implements IRestConsumerUserClient {
    private final WebClient client;
    private final String PATH_VALIDATE_USER_BY_EMAIL = "/api/v1/users/email/{email}/exists";

    @Override
    @CircuitBreaker(name = "existsUserByEmail" , fallbackMethod = "validateUserFallback")
    public Mono<Boolean> existsUserByEmail(String email) {
        return client
                .get()
                .uri(PATH_VALIDATE_USER_BY_EMAIL, email)
                .retrieve()
                .bodyToMono(ExistsUserResponse.class)
                .map(ExistsUserResponse::getExistsUser);
    }

    private Mono<Boolean> validateUserFallback(String email, Throwable throwable) {
        log.error("Se activa el fallback para la peticion de validacion de correo: {}. Causa: {}", email, throwable.getMessage());
        return Mono.just(false);
    }
}
