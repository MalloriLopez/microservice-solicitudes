package co.com.bancolombia.sqs.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanUpdateMessage {
    private UUID loanId;
    private String email;
    private Long newStatus;
    private String observations;
}
