package com.fanap.midhco.appstore.service.myException.appBundle;

import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;

/**
 * Created by admin123 on 7/19/2016.
 */
public class AppBundleNotSignedException extends BaseAppBundleException {
    public AppBundleNotSignedException(String message) {
        super(message);
    }
}
