package co.com.bancolombia.usecase.loanapplication;

import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanApplicationRepository repository;

    public Mono<LoanApplication> submitApplication(LoanApplication application) {

        return repository.save(application);
    }
}

