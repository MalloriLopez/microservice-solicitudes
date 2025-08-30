package co.com.bancolombia.r2dbc.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("loan_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanTypeEntity {

    @Id
    @Column("loan_type_id")
    private Long id;

    private String name;

    @Column("min_amount")
    private Double minAmount;

    @Column("max_amount")
    private int maxAmount;

    @Column("interest_rate")
    private Double interestRate;

    @Column("automatic_validation")
    private Boolean automaticValidation;

}