package co.com.bancolombia.r2dbc.reactiveloanapplication;

import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.r2dbc.entities.LoanApplicationEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class LoanApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
    LoanApplication/* change for domain model */,
    LoanApplicationEntity/* change for adapter model */,
    String,
        LoanApplicationReactiveRepository
> implements LoanApplicationRepository {
    public LoanApplicationReactiveRepositoryAdapter(LoanApplicationReactiveRepository repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, LoanApplicationEntity -> mapper.map(LoanApplicationEntity, LoanApplication.class));
    }

    @Transactional
    @Override
    public Mono<LoanApplication> save(LoanApplication a) {
        return super.save(a);
    }

    @Override
    public Flux<LoanApplication> filterLoanApplications(int offset, int limit, Long loanTypeId, Long status) {
        return repository
                .findForReviewPage(offset, limit, loanTypeId, status)
                .map(this::toEntity);
    }

    @Override
    public Mono<LoanApplication> findByEmailAndId(String email, UUID id) {
        return repository.findByEmailAndId(email, id)
                .map(this::toEntity);
    }

    @Override
    public Flux<LoanApplication> findByEmailAndApplicationStatusId(String email, Long applicationStatusId) {
        return repository.findByEmailAndApplicationStatusId(email, applicationStatusId)
                .map(this::toEntity);
    }


}
