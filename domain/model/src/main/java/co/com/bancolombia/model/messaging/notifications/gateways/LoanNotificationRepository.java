package co.com.bancolombia.model.messaging.notifications.gateways;

import co.com.bancolombia.model.messaging.notifications.MessageSQS;
import reactor.core.publisher.Mono;

public interface LoanNotificationRepository {
    Mono<String> sendMessageUpdateLoan(MessageSQS message);

}
