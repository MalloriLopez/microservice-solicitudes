package co.com.bancolombia.usecase;

import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanApplicationReviewItem;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.userquery.gateways.IUserQueryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class LoanApplicationListUseCaseTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LoanTypeRepository loanTypeRepository;
    @Mock
    private IUserQueryClient iUserQueryClient;

    private LoanApplicationListUseCase useCase;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        useCase = new LoanApplicationListUseCase(loanApplicationRepository, loanTypeRepository, iUserQueryClient);
    }

    @Test
    void shouldReturnReviewItem_whenApplicationApproved() {

        LoanApplication app = LoanApplication.builder()
                .id("id123")
                .loanAmount(100000.0)
                .termMonths(12)
                .email("test@email.com")
                .loanTypeId(1L)
                .applicationStatusId(4L)
                .build();

        LoanType loanType = LoanType.builder()
                .id(1L)
                .name("Personal")
                .interestRate(0.01)
                .build();

        IUserQueryClient.UserSummary user = new IUserQueryClient.UserSummary("Maria", (2000000.0));

        when(loanApplicationRepository.filterLoanApplications(0, 10, null, null)).thenReturn(Flux.just(app));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));
        when(iUserQueryClient.getUserByEmail("test@email.com")).thenReturn(Mono.just(user));

        Flux<LoanApplicationReviewItem> result = useCase.listLoanApplications(0, 10, null, null);

        StepVerifier.create(result)
                .expectNextMatches(item -> item.email().equals("test@email.com") && item.statusName().equals("Approved") && item.approvedMonthlyDebtTotal() > 0)
                .verifyComplete();
    }

    @Test
    void shouldReturnReviewItem_whenStatusUnknown() {

        LoanApplication app = LoanApplication.builder()
                .id("id456")
                .loanAmount(50000.0)
                .termMonths(6)
                .email("other@email.com")
                .loanTypeId(2L)
                .applicationStatusId(99L)
                .build();

        LoanType loanType = LoanType.builder().id(2L).name("Car").interestRate(0.02).build();
        IUserQueryClient.UserSummary user = new IUserQueryClient.UserSummary("Carlos", (1500000.0));

        when(loanApplicationRepository.filterLoanApplications(0, 10, null, null)).thenReturn(Flux.just(app));
        when(loanTypeRepository.findById(2L)).thenReturn(Mono.just(loanType));
        when(iUserQueryClient.getUserByEmail("other@email.com")).thenReturn(Mono.just(user));

        Flux<LoanApplicationReviewItem> result = useCase.listLoanApplications(0, 10, null, null);

        StepVerifier.create(result)
                .expectNextMatches(item -> item.statusName().equals("Unknown") && item.approvedMonthlyDebtTotal() == 0.0)
                .verifyComplete();
    }

    @Test
    void shouldReturnError_whenUserQueryFails() {

        LoanApplication app = LoanApplication.builder()
                .loanTypeId(1L)
                .email("fail@email.com")
                .applicationStatusId(1L)
                .build();

        when(loanApplicationRepository.filterLoanApplications(0, 10, null, null)).thenReturn(Flux.just(app));
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(new LoanType()));
        when(iUserQueryClient.getUserByEmail("fail@email.com")).thenReturn(Mono.error(new RuntimeException("User not found")));


        Flux<LoanApplicationReviewItem> result = useCase.listLoanApplications(0, 10, null, null);


        StepVerifier.create(result)
                .expectErrorMatches(e -> e.getMessage().contains("User not found"))
                .verify();
    }

    @Test
    void shouldHandleEmptyResult() {
        when(loanApplicationRepository.filterLoanApplications(0, 10, null, null)).thenReturn(Flux.empty());

        Flux<LoanApplicationReviewItem> result = useCase.listLoanApplications(0, 10, null, null);

        StepVerifier.create(result).verifyComplete();
    }
}
