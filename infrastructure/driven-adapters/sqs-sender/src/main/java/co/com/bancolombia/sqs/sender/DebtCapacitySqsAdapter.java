package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.loanapplication.gateways.LoggerRepository;
import co.com.bancolombia.model.messaging.debtcapacity.DebtCapacityEvent;
import co.com.bancolombia.model.messaging.debtcapacity.gateways.DebtCapacityMessagingRepository;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Service
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


