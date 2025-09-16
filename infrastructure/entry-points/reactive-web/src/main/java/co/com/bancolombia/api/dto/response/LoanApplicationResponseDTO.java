package co.com.bancolombia.api.dto.response;

import java.util.UUID;

public record LoanApplicationResponseDTO (
        UUID id,
        String email,
        Double loanAmount,
        Integer termMonths,
        Long loanTypeId,
        Long applicationStatusId,
        String observations
){
}
