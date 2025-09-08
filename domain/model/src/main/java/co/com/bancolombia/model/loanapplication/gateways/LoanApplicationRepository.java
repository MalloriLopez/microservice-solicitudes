package co.com.bancolombia.model.loanapplication.gateways;

import co.com.bancolombia.model.loanapplication.LoanApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanApplicationRepository {
    Mono<LoanApplication> save(LoanApplication loanApplication);

    Flux<LoanApplication> filterLoanApplications(
            int offset,
            int limit,
            Long loanTypeId,
            Long status
    );
}

