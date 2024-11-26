package com.challenge.rental_cars_spring_api.exception.handler;

import com.challenge.rental_cars_spring_api.exception.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ErrorResponse.class)
    public ResponseEntity<ErrorResponse> handleCustomException(ErrorResponse ex, WebRequest request) {
        ErrorResponse response = new ErrorResponse(
                ex.getStatus(),
                ex.getError(),
                request.getDescription(false),
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        ErrorResponse response = new ErrorResponse(
                500,
                "Internal Server Error",
                request.getDescription(false),
                "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde."
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Conflict",
                        "/alugueis/processar",
                        "Erro de integridade: " + ex.getMessage()
                ));
    }
}
