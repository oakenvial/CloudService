package org.example.cloudservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class FilenameUpdateRequestDto {

    @NotBlank(message = "Filename must not be blank")
    private String filename;
}
