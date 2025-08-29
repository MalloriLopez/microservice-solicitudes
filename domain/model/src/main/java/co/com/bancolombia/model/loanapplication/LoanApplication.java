package co.com.bancolombia.model.loanapplication;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanApplication {
    private String id;
    private String documentId;
    private Double amount;
    private Integer termInMonths;
    private Long loanTypeId;
    private Long statusId;
    private LocalDateTime createdAt;
}

