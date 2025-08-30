package co.com.bancolombia.api.dto.response;

public record LoanApplicationResponseDTO (
        String id,
        String email,
        Double loanAmount,
        Integer termMonths,
        Long loanTypeId,
        Long applicationStatusId
){
}
