package com.fanap.midhco.appstore.service.myException;

/**
 * Created by A.Moshiri on 4/9/2017.
 */
public class SSOServerException extends Exception {
    String errorMessage;
    String responseCode;

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public SSOServerException(String errorMessage, String responseCode) {
        this.errorMessage = errorMessage;
        this.responseCode = responseCode;
    }

    public SSOServerException(String message, String errorMessage, String responseCode) {
        super(message);
        this.errorMessage = errorMessage;
        this.responseCode = responseCode;
    }

    public SSOServerException(String message, Throwable cause, String errorMessage, String responseCode) {
        super(message, cause);
        this.errorMessage = errorMessage;
        this.responseCode = responseCode;
    }
}
