package co.com.bancolombia.model.notifications;

import lombok.*;

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
}
