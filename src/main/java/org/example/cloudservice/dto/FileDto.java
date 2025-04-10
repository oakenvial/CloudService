package org.example.cloudservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class FileDto {

    @NotBlank(message = "Filename must not be blank")
    private String filename;

    @NotNull(message = "File size must not be null")
    @PositiveOrZero(message = "File size must be zero or positive")
    private Long size;
}
