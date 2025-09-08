package co.com.bancolombia.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    @Bean("authClient")
    public WebClient authClient(@Value("${auth.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter((request, next) ->
                        ReactiveSecurityContextHolder.getContext()
                                .map(ctx -> (JwtAuthenticationToken) ctx.getAuthentication())
                                .map(jwt -> ClientRequest.from(request)
                                        .headers(h -> h.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getToken().getTokenValue()))
                                        .build())
                                .defaultIfEmpty(request)
                                .flatMap(next::exchange)
                )
                .build();
    }
}

