package com.cloudapplicationmanager.exception;

public class HealthCheckException extends Exception {

    public HealthCheckException(String message) {
        super(message);
    }

    public HealthCheckException(String message, Exception e) {
        super(message, e);
    }

    public HealthCheckException(Exception e) {
        super(e);
    }
}
