package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoanApplicationRequestDTO (
        @NotBlank
        @NotNull
        @Email
        String email,
        @NotNull
        Double loanAmount,
        @NotNull
        Integer termMonths,
        @NotNull
        Long loanTypeId,
        @NotNull
        Long applicationStatusId
) {

}



