package com.fanap.midhco.appstore.service.myException.appBundle;

import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;

/**
 * Created by admin123 on 7/23/2016.
 */
public class BaseAppBundleException extends AppStoreRuntimeException {
    public BaseAppBundleException(Exception ex) {
        super(ex);
    }

    public BaseAppBundleException(String message) {
        super(message);
    }
}
