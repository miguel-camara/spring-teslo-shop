package com.teslo.shop.common.exception;

public class ApiForbiddenException extends RuntimeException {

    public ApiForbiddenException(String message) {
        super(message);
    }
}
