package com.teslo.shop.common.exception;

public class ApiUnauthorizedException extends RuntimeException {

    public ApiUnauthorizedException(String message) {
        super(message);
    }
}
