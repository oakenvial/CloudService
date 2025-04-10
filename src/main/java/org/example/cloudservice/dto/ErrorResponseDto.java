package org.example.cloudservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ErrorResponseDto {
    private String message;
    private Integer id;
}
