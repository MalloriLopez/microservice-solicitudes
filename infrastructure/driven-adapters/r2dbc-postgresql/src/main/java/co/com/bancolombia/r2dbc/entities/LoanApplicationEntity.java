package co.com.bancolombia.r2dbc.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanApplicationEntity {

    @Id
    private String id;

    @Column("document_id")
    private String documentId;

    @Column("loan_amount")
    private Double loanAmount;

    @Column("term_months")
    private int termMonths;

    @Column("loan_type_id")
    private Long loanTypeId;

    @Column("application_status_id")
    private Long applicationStatusId;

    @Column("application_date")
    private LocalDate applicationDate;
}

