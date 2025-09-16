package co.com.bancolombia.api.dto.response;

import java.util.UUID;

public record LoanApplicationListResponse(
        UUID id,
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
