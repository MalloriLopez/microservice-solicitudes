package co.com.bancolombia.r2dbc.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table("application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanApplicationEntity {

    @Id
    @Column("application_id")
    private UUID id;

    @Column("loan_amount")
    private Double loanAmount;

    @Column("term_months")
    private int termMonths;

    @Column("email_user")
    private String email;

    @Column("loan_type_id")
    private Long loanTypeId;

    @Column("application_status_id")
    private Long applicationStatusId;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;

    private String observations;

}

