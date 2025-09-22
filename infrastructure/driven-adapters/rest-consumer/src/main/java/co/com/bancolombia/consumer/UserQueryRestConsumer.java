package co.com.bancolombia.consumer;

import co.com.bancolombia.model.userquery.gateways.IUserQueryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserQueryRestConsumer implements IUserQueryClient {

    private final WebClient client;

    @Override
    public Mono<UserSummary> getUserByEmail(String email) {
        return client.get()
                .uri("/api/v1/users/email/{email}/summary", email)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    log.error("[USER-CLIENT] HTTP {} email={} body={}",
                                            resp.statusCode().value(), email, body);
                                    return Mono.error(new IllegalStateException(
                                            "user-service error " + resp.statusCode().value()));
                                })
                )
                .bodyToMono(UserSummary.class)
                .doOnSubscribe(s -> log.info("[USER-CLIENT] GET summary email={}", email))
                .doOnNext(u -> log.info("[USER-CLIENT] OK email={} name={} baseSalary={}",
                        email, u.name(), u.baseSalary()))
                .doOnError(e -> log.error("[USER-CLIENT] FAIL email={} err={}", email, e.getMessage(), e));
    }
}


