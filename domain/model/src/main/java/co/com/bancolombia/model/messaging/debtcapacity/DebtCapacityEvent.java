package co.com.bancolombia.model.messaging.debtcapacity;

import co.com.bancolombia.model.loanapplication.LoanApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DebtCapacityEvent {
    UUID id;
    String email;
    Double loanAmount;
    Integer termMonths;
    Long loanTypeId;
    Double interestRate;
    Double salaryClient;
    String nameClient;
    List<LoanApplication> approvedApplications;
}

