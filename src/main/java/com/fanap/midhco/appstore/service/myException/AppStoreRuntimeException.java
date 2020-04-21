package com.fanap.midhco.appstore.service.myException;

/**
 * Created by admin123 on 6/21/2016.
 */
public class AppStoreRuntimeException extends RuntimeException {
    final private int errorCode;

    public AppStoreRuntimeException() {
        this.errorCode = 0;
    }

    public AppStoreRuntimeException(Throwable cause) {
        super(cause);
        this.errorCode = 0;
    }

    public AppStoreRuntimeException(String message) {
        super(message);
        this.errorCode = 0;
    }

    public AppStoreRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 0;
    }

    public AppStoreRuntimeException(Throwable cause, int baseCode, int errorCode) {
        super(cause);
        this.errorCode = errorCode + baseCode;
    }

    public AppStoreRuntimeException(String msg, int baseCode, int errorCode) {
        super(msg);
        this.errorCode = errorCode + baseCode;
    }

    public AppStoreRuntimeException(String msg, Throwable cause, int baseCode, int errorCode) {
        super(msg, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + getMessage() + "(" + errorCode + ")";
    }

    public String getErrorKey() {
        return "exception." + getErrorCode();
    }
}
