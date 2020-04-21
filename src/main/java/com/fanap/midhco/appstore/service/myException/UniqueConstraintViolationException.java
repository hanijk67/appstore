package com.fanap.midhco.appstore.service.myException;

public class UniqueConstraintViolationException extends AppStoreRuntimeException {
    public UniqueConstraintViolationException(Throwable cause) {
        super(cause);
    }
}