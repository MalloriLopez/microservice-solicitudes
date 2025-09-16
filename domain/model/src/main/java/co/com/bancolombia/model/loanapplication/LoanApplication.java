package co.com.bancolombia.model.loanapplication;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanApplication {
    private UUID id;
    private String email;
    private Double loanAmount;
    private Integer termMonths;
    private Long loanTypeId;
    private Long applicationStatusId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String observations;
}

