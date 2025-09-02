package com.example.demo.exception;

public class TrueLayerException extends Exception {

    public TrueLayerException(String message) {
        super(message);
    }

    public TrueLayerException(String message, Throwable cause) {
        super(message, cause);
    }
}