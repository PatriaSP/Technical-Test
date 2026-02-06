package com.patria.test.controller.advice;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.patria.test.exception.AppException;
import com.patria.test.dto.response.ExceptionResponse;

@ControllerAdvice
public class ErrorControllerAdvice {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ExceptionResponse> exception(AppException ex) {
        ex.printStackTrace();
        ExceptionResponse errorResponse = ExceptionResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity
                .status(ex.getCode())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> exception(Exception ex) {
        ex.printStackTrace();
        ExceptionResponse errorResponse = ExceptionResponse.builder()
                .code(500)
                .message("Internal Server Error!")
                .build();
        return ResponseEntity
                .status(500)
                .body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ExceptionResponse errorResponse = ExceptionResponse.builder()
                .code(400)
                .message(message)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
