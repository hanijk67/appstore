package com.fanap.midhco.appstore.service.myException.appBundle;

/**
 * Created by admin123 on 7/19/2016.
 */
public class AppBundleSignNotValidException extends BaseAppBundleException {
    public AppBundleSignNotValidException(Exception ex) {super(ex);}
    public AppBundleSignNotValidException(String message) {
        super(message);
    }
}
