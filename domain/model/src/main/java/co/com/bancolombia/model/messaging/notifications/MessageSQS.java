package co.com.bancolombia.model.messaging.notifications;

import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MessageSQS {
      private UUID loanId;
      private String status;
      private String userClient;
      private String emailClient;
      private String observations;
      private OffsetDateTime updatedAt;
      private BigInteger amount;
      private Integer loanTermMonths;
      private BigDecimal annualInterestRate;
}
