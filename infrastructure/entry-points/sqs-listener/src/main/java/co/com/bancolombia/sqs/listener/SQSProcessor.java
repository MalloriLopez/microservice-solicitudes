package co.com.bancolombia.sqs.listener;

import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.gateways.LoggerRepository;
import co.com.bancolombia.sqs.listener.dto.LoanUpdateMessage;
import co.com.bancolombia.usecase.LoanApplicationUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final LoanApplicationUseCase loanApplicationUseCase;
    private final ObjectMapper objectMapper;
    private final LoggerRepository logger;

    @Override
    public Mono<Void> apply(Message message) {
        logger.info("[SQS RECV] messageId={} body={}", message.messageId(), message.body());

        return Mono.fromCallable(() -> objectMapper.readValue(message.body(), LoanUpdateMessage.class))
                .flatMap(payload -> {
                    logger.info("[SQS PARSED] appId={} newStatus={} email={} obs={}",
                            payload.getLoanId(), payload.getNewStatus(), payload.getEmail(), payload.getObservations());

                    LoanApplication updateReq = LoanApplication.builder()
                            .id(payload.getLoanId())
                            .email(payload.getEmail())
                            .applicationStatusId(payload.getNewStatus())
                            .observations(payload.getObservations())
                            .updatedAt(OffsetDateTime.now(ZoneId.of("America/Bogota")))
                            .build();

                    return loanApplicationUseCase.update(updateReq)
                            .doOnSuccess(saved -> logger.info("[UPDATE OK] id={} status={} email={}",
                                    saved.getId(), saved.getApplicationStatusId(), saved.getEmail()))
                            .switchIfEmpty(Mono.fromRunnable(() ->
                                    logger.info("[UPDATE EMPTY] id={} email={} (no rows updated or not found)",
                                            updateReq.getId(), updateReq.getEmail())))
                            .then();
                })
                .doOnError(err -> logger.error("[SQS PROCESS ERROR] messageId={} error={}", message.messageId(), err.getMessage(), err));
    }
}
