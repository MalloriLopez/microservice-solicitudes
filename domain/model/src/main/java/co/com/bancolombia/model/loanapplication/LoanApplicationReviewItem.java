package co.com.bancolombia.model.loanapplication;

import java.util.UUID;

public record LoanApplicationReviewItem(
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

