package co.com.bancolombia.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.util.*;

@Configuration
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper; // Boot la provee

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder jwtDecoder,
            ReactiveJwtAuthenticationConverterAdapter jwtAuthConverter
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .headers(h -> h.frameOptions(fr -> fr.disable()))
                .authorizeExchange(ex -> ex
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/h2/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitudes").hasRole("CLIENTE")
                        .pathMatchers(HttpMethod.GET, "/api/v1/solicitud").hasRole("ASESOR")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .authenticationEntryPoint((exchange, exAuth) -> {
                            String detail = "Se requiere autenticación";
                            if (exAuth instanceof InvalidBearerTokenException) {
                                detail = "Token inválido o expirado";
                            } else if (exAuth instanceof OAuth2AuthenticationException oae) {
                                String code = oae.getError().getErrorCode(); // p.ej. invalid_token
                                if ("invalid_token".equals(code) || "invalid_request".equals(code)) {
                                    detail = "Token inválido o expirado";
                                }
                            }
                            return writeProblem(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", detail);
                        })
                        .accessDeniedHandler((exchange, denied) ->
                                writeProblem(exchange, HttpStatus.FORBIDDEN, "Forbidden",
                                        "No tienes permisos para realizar esta acción"))
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthConverter)
                        )
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secretProp) {
        // Acepta Base64 estándar y Base64-URL
        byte[] secretBytes;
        try {
            secretBytes = Base64.getDecoder().decode(secretProp);
        } catch (IllegalArgumentException e) {
            secretBytes = Base64.getUrlDecoder().decode(secretProp);
        }
        var key = new SecretKeySpec(secretBytes, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthConverter() {
        var delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(SecurityConfig::extractAuthorities);
        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }

    private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> auths = new ArrayList<>();
        String role = jwt.getClaimAsString("role");
        if (role != null && !role.isBlank()) auths.add(new SimpleGrantedAuthority("ROLE_" + role));
        List<String> perms = jwt.getClaimAsStringList("permissions");
        if (perms != null) perms.stream()
                .filter(Objects::nonNull).filter(p -> !p.isBlank())
                .map(SimpleGrantedAuthority::new)
                .forEach(auths::add);
        return auths;
    }

    private Mono<Void> writeProblem(ServerWebExchange exchange,
                                    HttpStatus status,
                                    String title,
                                    String detail) {
        var req = exchange.getRequest();
        var res = exchange.getResponse();
        res.setStatusCode(status);
        res.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        var pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        pd.setDetail(detail);
        pd.setInstance(URI.create(req.getPath().value()));

        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(pd))
                .flatMap(bytes -> {
                    DataBuffer buffer = res.bufferFactory().wrap(bytes);
                    return res.writeWith(Mono.just(buffer));
                });
    }
}
