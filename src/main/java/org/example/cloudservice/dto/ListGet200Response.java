package org.example.cloudservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ListGet200Response {
    private String filename;
    private Integer size;
}
