package org.example.cloudservice.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class LoginResponseDto {

    // Optionally, use @NotBlank if you want to enforce that a non-empty token is always provided.
    @NotBlank(message = "Authentication token must not be blank")
    private String authToken;
}
