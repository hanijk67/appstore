package com.fanap.midhco.appstore.service.app;

import com.fanap.midhco.appstore.service.myException.appBundle.BaseAppBundleException;

import java.io.Serializable;
import java.security.cert.Certificate;
import java.util.List;

/**
 * Created by admin123 on 6/30/2016.
 */
public interface IAPPPackageService extends Serializable {
    public String getVersionCode();
    public String getVersionName();
    public String getPackage();
    public String getMinSDK();
    public String getTargetSDK();
    public Certificate verifyPackage(Certificate previousCertficate) throws BaseAppBundleException, Exception;
    public List<String> getPermissions();
}
