package com.patria.test.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppException extends RuntimeException {

    private int code;
    private String message;

    public AppException(HttpStatus status, String message, Throwable cause) {
        super(cause); 
        this.code = status.value();
        this.message = message;
    }
}
