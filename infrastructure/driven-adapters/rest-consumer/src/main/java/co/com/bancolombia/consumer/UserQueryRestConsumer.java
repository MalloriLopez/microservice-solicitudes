package co.com.bancolombia.consumer;

import co.com.bancolombia.model.userquery.gateways.IUserQueryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserQueryRestConsumer implements IUserQueryClient {

    private final WebClient client;

    @Override
    public Mono<UserSummary> getUserByEmail(String email) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (JwtAuthenticationToken) ctx.getAuthentication())
                .map(auth -> auth.getToken().getTokenValue())
                .flatMap(token -> client.get()
                        .uri("/api/v1/users/email/{email}/summary", email)
                        .headers(h -> h.setBearerAuth(token))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, resp -> resp.createException().flatMap(Mono::error))
                        .bodyToMono(UserSummary.class)
                );
    }
}

