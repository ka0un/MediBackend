package com.hapangama.medibackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when staff attempts to access restricted patient records
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessRestrictedException extends RuntimeException {
    public AccessRestrictedException(String message) {
        super(message);
    }
}
