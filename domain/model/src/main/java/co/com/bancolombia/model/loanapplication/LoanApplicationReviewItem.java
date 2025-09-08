package co.com.bancolombia.model.loanapplication;

public record LoanApplicationReviewItem(
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

