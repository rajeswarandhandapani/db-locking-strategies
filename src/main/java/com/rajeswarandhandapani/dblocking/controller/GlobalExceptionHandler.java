package com.rajeswarandhandapani.dblocking.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> handleObjectOptimisticLockingFailure(ObjectOptimisticLockingFailureException e) {
        logger.error("ObjectOptimisticLockingFailure caught by global handler: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("Another user has modified this item. Please refresh and try again.");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<String> handleOptimisticLockingFailure(OptimisticLockingFailureException e) {
        logger.error("OptimisticLockingFailure caught by global handler: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("Another user has modified this item. Please refresh and try again.");
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<String> handlePessimisticLockingFailure(PessimisticLockingFailureException e) {
        logger.error("PessimisticLockingFailure caught by global handler: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("Could not acquire lock on the resource. Please try again.");
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<String> handleInterruptedException(InterruptedException e) {
        logger.error("InterruptedException caught by global handler: {}", e.getMessage());
        Thread.currentThread().interrupt(); // Restore interrupt status
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Operation was interrupted. Please try again.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        logger.error("Unexpected error caught by global handler: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("An unexpected error occurred. Please try again later.");
    }
}
