package co.com.bancolombia.usecase;

import co.com.bancolombia.model.client.UserClientDetails;
import co.com.bancolombia.model.exceptions.UnchangedStatusApplicationsException;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.gateways.IRestConsumerUserClient;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loanapplication.gateways.LoggerRepository;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.notifications.MessageSQS;
import co.com.bancolombia.model.notifications.gateways.LoanNotificationRepository;
import co.com.bancolombia.model.userquery.gateways.IUserQueryClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final IRestConsumerUserClient iRestConsumerUserClient;
    private final LoggerRepository logger;
    private final LoanNotificationRepository loanNotificationRepository;

    public Mono<LoanApplication> submitApplication(LoanApplication application) {
        return iRestConsumerUserClient.existsUserByEmail(application.getEmail())
                .flatMap(existUser -> Boolean.TRUE.equals(existUser)
                        ? internalManagement(application)
                        : Mono.error(new IllegalArgumentException("EL usuario no existe, no es posible realizar la solicitud del préstamo")));
    }

    private Mono<LoanApplication> internalManagement(LoanApplication application){
                return Mono.just(application)
               .flatMap(loan -> validateLoanType(application.getLoanTypeId())
                       .thenReturn(loan))
                       .doOnNext(loan -> loan.setCreatedAt(OffsetDateTime.now(ZoneId.of("America/Bogota"))))
                .flatMap(loanApplicationRepository::save);
    }

    private Mono<Void> validateLoanType(Long id) {
        return loanTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("EL tipo de préstamo no existe")))
                .then();
    }

    public Mono<LoanApplication> update(LoanApplication loanApplication) {
        final String email = loanApplication.getEmail();
        final UUID id = loanApplication.getId();
        final Long newStatusId = loanApplication.getApplicationStatusId();

        return loanApplicationRepository.findByEmailAndId(email, id)
                .zipWhen(ignored -> iRestConsumerUserClient.getUserByEmail(email))
                .flatMap(tuple -> {
                    LoanApplication loanBd = tuple.getT1();
                    UserClientDetails userClientDetails = tuple.getT2();
                    if (loanBd.getApplicationStatusId().equals(newStatusId)) {
                        return Mono.error(new UnchangedStatusApplicationsException("La solicitud de préstamo ya se encuentra en estado " + newStatusId));
                    }
                    loanBd.setApplicationStatusId(newStatusId);
                    loanBd.setObservations(loanApplication.getObservations());
                    loanBd.setUpdatedAt(OffsetDateTime.now(ZoneId.of("America/Bogota")));
                    return loanApplicationRepository.save(loanBd)

                    .flatMap(saved -> {
                    String statusName = statusToLabel(saved.getApplicationStatusId());
                    if ("APPROVED".equalsIgnoreCase(statusName) || "REJECTED".equalsIgnoreCase(statusName)) {
                        String userClient =
                                (userClientDetails.getName() != null ? userClientDetails.getName() : "")
                                        + (userClientDetails.getLastname() != null ? " " + userClientDetails.getLastname() : "");

                        MessageSQS msg = MessageSQS.builder()
                                .loanId(saved.getId())
                                .status(statusName.equalsIgnoreCase("APPROVED") ? "APROBADO" : "RECHAZADO")
                                .emailClient(saved.getEmail())
                                .userClient(userClient)
                                .build();
                        logger.info("ENtre al if"+ msg.toBuilder());
                        return loanNotificationRepository.sendMessageUpdateLoan(msg)
                                .doOnSuccess(msgId -> logger.info("SQS enviado messageId={} solicitudId={}", msgId, saved.getId()))
                                .thenReturn(saved);
                    }
                    return Mono.just(saved);
                });
    });
    }

    private static String statusToLabel(Long statusId) {
        if (statusId == null) return "Unknown";
        return switch (statusId.intValue()) {
            case 1 -> "Pending review";
            case 2 -> "Rejected";
            case 3 -> "Manual review";
            case 4 -> "Approved";
            default -> "Unknown";
        };
    }
}

