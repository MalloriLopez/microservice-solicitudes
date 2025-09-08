package co.com.bancolombia.model.userquery.gateways;

import reactor.core.publisher.Mono;

public interface IUserQueryClient {
    record UserSummary(String name, Double baseSalary) {}
    Mono<UserSummary> getUserByEmail(String email);
}

