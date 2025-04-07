package org.example.cloudservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class LoginRequest {
    private String login;
    private String password;
}
