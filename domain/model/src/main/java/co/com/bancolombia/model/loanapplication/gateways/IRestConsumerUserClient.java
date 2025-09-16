package co.com.bancolombia.model.loanapplication.gateways;

import co.com.bancolombia.model.client.UserClientDetails;
import reactor.core.publisher.Mono;

public interface IRestConsumerUserClient {

    Mono<Boolean> existsUserByEmail(String email);

    Mono<UserClientDetails> getUserByEmail(String email);
}
