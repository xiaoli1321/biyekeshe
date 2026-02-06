package com.learnplatform.integration.python;

/**
 * Python 服务调用异常
 */
public class PythonServiceException extends RuntimeException {
    private final int statusCode;

    public PythonServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public PythonServiceException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
