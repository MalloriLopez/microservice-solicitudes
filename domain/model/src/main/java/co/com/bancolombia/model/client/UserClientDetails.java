package co.com.bancolombia.model.client;

import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserClientDetails {

    private String userId;
    private String documentId;
    private String name;
    private String lastname;
    private LocalDate birthDate;
    private String address;
    private String email;
    private String phone;
    private Long idRol;
    private Double baseSalary;
}

