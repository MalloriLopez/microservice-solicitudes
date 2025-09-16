package co.com.bancolombia.model.exceptions;

public class NoLoanApplicationsException extends RuntimeException {
    public NoLoanApplicationsException(String message) {
        super(message);
    }
}
