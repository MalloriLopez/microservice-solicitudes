package co.com.bancolombia.model.messaging.notifications;

import lombok.*;

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
