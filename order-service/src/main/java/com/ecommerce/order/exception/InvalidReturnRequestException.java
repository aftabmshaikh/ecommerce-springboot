package com.ecommerce.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidReturnRequestException extends RuntimeException {
    public InvalidReturnRequestException(String message) {
        super(message);
    }

    public InvalidReturnRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
