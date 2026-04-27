package com.devplatform.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ControllerException extends RuntimeException {

    static final String errorMessage = "I'm sorry, there was an issue with your request." +
            " Please try again later.";

    public ControllerException() {
        super(errorMessage);
    }
}
