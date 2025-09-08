package co.com.bancolombia.api.dto.response;

public record LoanApplicationListResponse(
        String id,
        Double amount,
        Integer termMonths,
        String email,
        String name,
        String loanTypeName,
        Double interestRate,
        String statusName,
        Double baseSalary,
        Double approvedMonthlyDebtTotal
) {}
