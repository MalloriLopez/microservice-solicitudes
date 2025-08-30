package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.Email;

public record LoanApplicationRequestDTO (
        @Email
        String email,
        Double loanAmount,
        Integer termMonths,
        Long loanTypeId,
        Long applicationStatusId
) {

}



