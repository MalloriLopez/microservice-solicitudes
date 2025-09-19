package co.com.bancolombia.usecase;

import co.com.bancolombia.model.exceptions.UnchangedStatusApplicationsException;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.gateways.IRestConsumerUserClient;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loanapplication.gateways.LoggerRepository;
import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.messaging.debtcapacity.DebtCapacityEvent;
import co.com.bancolombia.model.messaging.debtcapacity.gateways.DebtCapacityMessagingRepository;
import co.com.bancolombia.model.messaging.notifications.MessageSQS;
import co.com.bancolombia.model.messaging.notifications.gateways.LoanNotificationRepository;
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
    private final IUserQueryClient iUserQueryClient;
    private final DebtCapacityMessagingRepository debtCapacityMessagingRepository;

    public Mono<LoanApplication> submitApplication(LoanApplication application) {
        return iRestConsumerUserClient.existsUserByEmail(application.getEmail())
                .flatMap(existUser -> Boolean.TRUE.equals(existUser)
                        ? internalManagement(application)
                        : Mono.error(new IllegalArgumentException("EL usuario no existe, no es posible realizar la solicitud del préstamo")));
    }

    private Mono<LoanApplication> internalManagement(LoanApplication application) {
        return loanTypeRepository.findById(application.getLoanTypeId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("EL tipo de préstamo no existe")))
                .flatMap(loanType -> {
                    application.setCreatedAt(OffsetDateTime.now(ZoneId.of("America/Bogota")));
                    return loanApplicationRepository.save(application)
                            .flatMap(saved -> onAutomaticValidation(saved, loanType).thenReturn(saved));
                });
    }

    private Mono<Void> onAutomaticValidation(LoanApplication saved, LoanType loanType) {
        logger.info("Entre al metodo doAutomaticValidation:::" + loanType.getAutomaticValidation());
        if (loanType.getAutomaticValidation() == null || !loanType.getAutomaticValidation()) {
            return Mono.empty();
        }
        final String email = saved.getEmail();
        final Long approvedStatus = 4L;

        return loanApplicationRepository.findByEmailAndApplicationStatusId(email, approvedStatus) // Flux<LoanApplication>
                .collectList()
                .flatMap(approvedList -> {
                    return iUserQueryClient.getUserByEmail(email)
                            .flatMap(user -> {
                                DebtCapacityEvent event = DebtCapacityEvent.builder()
                                        .id(saved.getId())
                                        .email(email)
                                        .loanAmount(saved.getLoanAmount())
                                        .termMonths(saved.getTermMonths())
                                        .loanTypeId(saved.getLoanTypeId())
                                        .interestRate(loanType.getInterestRate())
                                        .salaryClient(user.baseSalary())
                                        .nameClient(user.name() != null ? user.name() : saved.getEmail())
                                        .approvedApplications(approvedList)
                                        .build();

                                return debtCapacityMessagingRepository.sendDebtCapacityEvent(event)
                                        .doOnSuccess(msgId -> logger.info("DebtCapacityEvent enviado SQS messageId={} loanId={}", msgId, saved.getId()))
                                        .then();
                            });
                });
    }


    public Mono<LoanApplication> update(LoanApplication loanApplication) {
        final String email = loanApplication.getEmail();
        final UUID id = loanApplication.getId();
        final Long newStatusId = loanApplication.getApplicationStatusId();

        return loanApplicationRepository.findByEmailAndId(email, id)
                .zipWhen(ignored -> iUserQueryClient.getUserByEmail(email))
                .flatMap(tuple -> {
                    LoanApplication loanBd = tuple.getT1();
                    IUserQueryClient.UserSummary userClientDetails = tuple.getT2();
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
                        String userClient = userClientDetails.name();

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

