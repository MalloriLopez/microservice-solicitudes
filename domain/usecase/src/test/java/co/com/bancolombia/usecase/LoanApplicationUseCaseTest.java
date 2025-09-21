package co.com.bancolombia.usecase;

import co.com.bancolombia.model.exceptions.UnchangedStatusApplicationsException;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.gateways.IRestConsumerUserClient;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loanapplication.gateways.LoggerRepository;
import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.messaging.debtcapacity.gateways.DebtCapacityMessagingRepository;
import co.com.bancolombia.model.messaging.notifications.gateways.LoanNotificationRepository;
import co.com.bancolombia.model.userquery.gateways.IUserQueryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;


class LoanApplicationUseCaseTest {

    @Mock
    private LoanApplicationRepository loanRepository;
    @Mock
    private LoanTypeRepository loanTypeRepository;
    @Mock
    private IRestConsumerUserClient userClient;
    @Mock
    private LoggerRepository logger;
    @Mock
    private LoanNotificationRepository notificationRepository;
    @Mock
    private IUserQueryClient iUserQueryClient;
    @Mock
    private DebtCapacityMessagingRepository debtCapacityMessagingRepository;

    private LoanApplicationUseCase useCase;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new LoanApplicationUseCase(loanRepository, loanTypeRepository, userClient, logger, notificationRepository, iUserQueryClient, debtCapacityMessagingRepository  );
    }

    @Test
    void submitApplication_WhenUserExistsAndLoanTypeIsValid_ShouldSaveLoanApplication() {

        LoanApplication application = LoanApplication.builder()
                .email("test@example.com")
                .loanAmount(Double.valueOf(5000))
                .termMonths(12)
                .loanTypeId(1L)
                .build();

        when(userClient.existsUserByEmail("test@example.com")).thenReturn(Mono.just(true));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(mock(LoanType.class)));
        when(loanRepository.save(application)).thenReturn(Mono.just(application));

        Mono<LoanApplication> result = useCase.submitApplication(application);

        StepVerifier.create(result)
                .expectNext(application)
                .verifyComplete();

        verify(userClient).existsUserByEmail("test@example.com");
        verify(loanTypeRepository).findById(1L);
        verify(loanRepository).save(application);
    }

    @Test
    void submitApplication_WhenUserDoesNotExist_ShouldReturnError() {

        LoanApplication application = LoanApplication.builder()
                .email("notfound@example.com")
                .loanTypeId(2L)
                .build();

        when(userClient.existsUserByEmail("notfound@example.com")).thenReturn(Mono.just(false));

        Mono<LoanApplication> result = useCase.submitApplication(application);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("EL usuario no existe"))
                .verify();

        verify(userClient).existsUserByEmail("notfound@example.com");
        verifyNoInteractions(loanTypeRepository, loanRepository);
    }

    @Test
    void submitApplication_WhenLoanTypeDoesNotExist_ShouldReturnError() {

        LoanApplication application = LoanApplication.builder()
                .email("user@example.com")
                .loanTypeId(99L)
                .build();

        when(userClient.existsUserByEmail("user@example.com")).thenReturn(Mono.just(true));
        when(loanTypeRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<LoanApplication> result = useCase.submitApplication(application);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("tipo de préstamo no existe"))
                .verify();

        verify(userClient).existsUserByEmail("user@example.com");
        verify(loanTypeRepository).findById(99L);
        verifyNoInteractions(loanRepository);
    }

    @Test
    void update_WhenStatusChangesToApproved_ShouldSendNotificationAndUpdate() {
   
        UUID loanId = UUID.randomUUID();
        String email = "user@example.com";
        Long previousStatus = 1L;
        Long newStatus = 4L; // Approved

        LoanApplication existing = LoanApplication.builder()
                .id(loanId)
                .email(email)
                .applicationStatusId(previousStatus)
                .build();

        LoanApplication updateRequest = LoanApplication.builder()
                .id(loanId)
                .email(email)
                .applicationStatusId(newStatus)
                .observations("All checks passed")
                .build();

        IUserQueryClient.UserSummary userDetails = new IUserQueryClient.UserSummary("Carlos", Double.valueOf(5000));

        when(loanRepository.findByEmailAndId(email, loanId)).thenReturn(Mono.just(existing));
        when(iUserQueryClient.getUserByEmail(email)).thenReturn(Mono.just(userDetails));
        when(loanRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(notificationRepository.sendMessageUpdateLoan(any())).thenReturn(Mono.just("message-id-123"));


        Mono<LoanApplication> result = useCase.update(updateRequest);


        StepVerifier.create(result)
                .expectNextMatches(updated -> updated.getApplicationStatusId().equals(newStatus)
                        && "All checks passed".equals(updated.getObservations()))
                .verifyComplete();

        verify(notificationRepository).sendMessageUpdateLoan(any());
        verify(logger).info(startsWith("SQS enviado"), any(), eq(loanId));
    }

    @Test
    void update_WhenStatusDoesNotChange_ShouldThrowUnchangedStatusException() {

        UUID loanId = UUID.randomUUID();
        String email = "user@example.com";
        Long status = 1L;

        LoanApplication existing = LoanApplication.builder()
                .id(loanId)
                .email(email)
                .applicationStatusId(status)
                .build();

        LoanApplication updateRequest = LoanApplication.builder()
                .id(loanId)
                .email(email)
                .applicationStatusId(status)
                .build();

        when(loanRepository.findByEmailAndId(email, loanId)).thenReturn(Mono.just(existing));
        when(iUserQueryClient.getUserByEmail(email)).thenReturn(Mono.just(new IUserQueryClient.UserSummary("User", Double.valueOf(5000))));


        Mono<LoanApplication> result = useCase.update(updateRequest);


        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof UnchangedStatusApplicationsException &&
                        e.getMessage().contains("ya se encuentra en estado"))
                .verify();

        verifyNoInteractions(notificationRepository);
    }

}

