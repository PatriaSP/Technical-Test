package com.patria.test.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private PaginationResponse pagination;
    
}
