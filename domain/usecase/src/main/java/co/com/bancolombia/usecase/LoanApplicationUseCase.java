package co.com.bancolombia.usecase;

import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.gateways.IRestConsumerUserClient;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final IRestConsumerUserClient iRestConsumerUserClient;

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
                .flatMap(loanApplicationRepository::save);
    }

    private Mono<Void> validateLoanType(Long id) {
        return loanTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("EL tipo de préstamo no existe")))
                .then();
    }


}

