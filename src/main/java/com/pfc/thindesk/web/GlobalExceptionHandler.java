package com.pfc.thindesk.web;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte exceções de regra de negócio em respostas HTTP claras (em vez de 500). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(NoSuchElementException e) {
        return erro(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> regraDeNegocio(IllegalStateException e) {
        return erro(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> requisicaoInvalida(IllegalArgumentException e) {
        return erro(HttpStatus.BAD_REQUEST, e);
    }

    private ResponseEntity<Map<String, String>> erro(HttpStatus status, RuntimeException e) {
        return ResponseEntity.status(status)
                .body(Map.of("erro", Objects.toString(e.getMessage(), status.getReasonPhrase())));
    }
}
