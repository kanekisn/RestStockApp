package org.reststockapp.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CustomErrorDetails> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseEntity<>(new CustomErrorDetails("The username or password is incorrect", ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({InvalidDateEntriesException.class, UnknownTickerException.class})
    public ResponseEntity<CustomErrorDetails> handleBadRequestExceptions(Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String specificDetail = ex instanceof InvalidDateEntriesException ? "Invalid date entries" : "The ticker was not found in service";
        return new ResponseEntity<>(new CustomErrorDetails(specificDetail, ex.getMessage()), status);
    }

    @ExceptionHandler({AccessDeniedException.class, AccountStatusException.class, SignatureException.class, ExpiredJwtException.class})
    public ResponseEntity<CustomErrorDetails> handleAccessExceptions(Exception ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        String description = "Access issue encountered";
        if (ex instanceof AccountStatusException) {
            description = "The account is locked";
        } else if (ex instanceof AccessDeniedException) {
            description = "You are not authorized to access this resource";
        } else if (ex instanceof SignatureException) {
            description = "The JWT signature is invalid";
        } else if (ex instanceof ExpiredJwtException) {
            description = "The JWT token has expired";
        }
        return new ResponseEntity<>(new CustomErrorDetails(description, ex.getMessage()), status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorDetails> handleGeneralException(Exception ex) {
        return new ResponseEntity<>(new CustomErrorDetails("Unknown internal server error", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}