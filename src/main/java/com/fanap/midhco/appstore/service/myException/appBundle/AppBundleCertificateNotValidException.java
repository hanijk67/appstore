package com.fanap.midhco.appstore.service.myException.appBundle;

import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;

/**
 * Created by admin123 on 7/23/2016.
 */
public class AppBundleCertificateNotValidException extends BaseAppBundleException {
    public AppBundleCertificateNotValidException(String messsage) {
        super(messsage);
    }
}
