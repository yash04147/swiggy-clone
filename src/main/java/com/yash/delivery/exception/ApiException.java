package com.yash.delivery.exception;

public class ApiException extends RuntimeException{

    public ApiException(String message){
        super(message);
    }
}
