package org.example.cloudservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ErrorResponseDto {

    @NotBlank(message = "Error message must not be blank")
    private String message;

    @NotNull(message = "Error ID must not be null")
    private Integer id;
}
