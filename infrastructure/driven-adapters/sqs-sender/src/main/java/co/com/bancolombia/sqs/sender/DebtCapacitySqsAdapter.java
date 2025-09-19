package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.loanapplication.gateways.LoggerRepository;
import co.com.bancolombia.model.messaging.debtcapacity.DebtCapacityEvent;
import co.com.bancolombia.model.messaging.debtcapacity.gateways.DebtCapacityMessagingRepository;
import co.com.bancolombia.model.messaging.notifications.gateways.LoanNotificationRepository;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Map;

@RequiredArgsConstructor
public class DebtCapacitySqsAdapter implements DebtCapacityMessagingRepository {
    private final SqsAsyncClient sqs;
    private final ObjectMapper objectMapper;
    private final SQSSenderProperties properties;
    private final SQSSender publisher;
    private final LoggerRepository log;

    @Override
    public Mono<String> sendDebtCapacityEvent(DebtCapacityEvent event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .doOnNext(body -> log.info("[CAPACITY->SQS] queue={} payload={}", properties.debtCapacityQueueUrl(), body))
                .flatMap(body -> publisher.send(properties.debtCapacityQueueUrl(), body));
    }

}


