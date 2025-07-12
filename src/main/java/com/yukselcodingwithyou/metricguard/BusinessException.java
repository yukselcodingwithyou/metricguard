package com.yukselcodingwithyou.metricguard;

/**
 * Base exception for business errors. Clients should extend this class to
 * represent application specific exceptions.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
