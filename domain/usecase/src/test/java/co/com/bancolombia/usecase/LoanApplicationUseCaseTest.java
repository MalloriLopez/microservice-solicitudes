package co.com.bancolombia.usecase;

import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.gateways.IRestConsumerUserClient;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;


class LoanApplicationUseCaseTest {

    @Mock
    private LoanApplicationRepository loanRepository;
    @Mock
    private LoanTypeRepository loanTypeRepository;
    @Mock
    private IRestConsumerUserClient userClient;

    private LoanApplicationUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new LoanApplicationUseCase(loanRepository, loanTypeRepository, userClient);
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
}

