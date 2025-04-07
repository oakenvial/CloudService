package org.example.cloudservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class FileDeleteRequest {
    private String name;
}
