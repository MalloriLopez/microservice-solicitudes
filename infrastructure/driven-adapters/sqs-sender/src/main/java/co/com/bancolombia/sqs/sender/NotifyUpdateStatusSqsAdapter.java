package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.loanapplication.gateways.LoggerRepository;
import co.com.bancolombia.model.messaging.notifications.MessageSQS;
import co.com.bancolombia.model.messaging.notifications.gateways.LoanNotificationRepository;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class NotifyUpdateStatusSqsAdapter implements LoanNotificationRepository {
    private final SQSSender publisher;
    private final ObjectMapper objectMapper;
    private final SQSSenderProperties properties;
    private final LoggerRepository log;

    @Override
    public Mono<String> sendMessageUpdateLoan(MessageSQS message) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(message))
                .doOnNext(body -> log.info("[NOTIF->SQS] queue={} payload={}", properties.notificationQueueUrl(), body))
                .flatMap(body -> publisher.send(properties.notificationQueueUrl(), body));
    }
}

