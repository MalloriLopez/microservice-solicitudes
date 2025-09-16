package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.notifications.MessageSQS;
import co.com.bancolombia.model.notifications.gateways.LoanNotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class NotifyUpdateStatusSqsAdapter implements LoanNotificationRepository {
    private final SQSSender publisher;
    private final ObjectMapper objectMapper;


    @Override
    public Mono<String> sendMessageUpdateLoan(MessageSQS message) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(message))
                .flatMap(publisher::send);
    }
}

