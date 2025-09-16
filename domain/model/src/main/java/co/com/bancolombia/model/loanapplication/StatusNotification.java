package co.com.bancolombia.model.loanapplication;

public record StatusNotification(
        String loanId,
        String status,
        String userClient,
        String emailClient
) {}
