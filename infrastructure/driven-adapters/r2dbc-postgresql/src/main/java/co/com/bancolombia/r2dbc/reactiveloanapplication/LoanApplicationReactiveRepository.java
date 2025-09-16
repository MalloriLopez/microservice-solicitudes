package co.com.bancolombia.r2dbc.reactiveloanapplication;

import co.com.bancolombia.r2dbc.entities.LoanApplicationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

// TODO: This file is just an example, you should delete or modify it
public interface LoanApplicationReactiveRepository extends ReactiveCrudRepository<LoanApplicationEntity, String>, ReactiveQueryByExampleExecutor<LoanApplicationEntity> {

    @Query("""
    SELECT application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id
    FROM application
    WHERE application_status_id IN (1,2,3)
    AND (:loanTypeId IS NULL OR loan_type_id = :loanTypeId)
    AND (:status IS NULL OR application_status_id = :status)
    ORDER BY application_id
    LIMIT :limit OFFSET :offset
    """)
    Flux<LoanApplicationEntity> findForReviewPage(
            @Param("offset") int offset,
            @Param("limit")  int limit,
            @Param("loanTypeId") Long loanTypeId,
            @Param("status") Long status
    );

    Mono<LoanApplicationEntity> findByEmailAndId(String email, UUID id);

}
