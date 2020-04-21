package com.fanap.midhco.appstore.restControllers.vos;

import org.json.*;

import java.io.Serializable;

/**
 * Created by admin123 on 7/16/2016.
 */
public class ProductRequestVO implements Serializable {
    String packageName;
    String versionCode;

    public ProductRequestVO(String s) {
        JSONObject jsonObject = new JSONObject(s);
        this.packageName = jsonObject.getString("packageName");
        this.versionCode = jsonObject.getString("versionCode");
    }

    public ProductRequestVO(String[] s) {
        JSONObject jsonObject = new JSONObject(s);
        this.packageName = jsonObject.getString("packageName");
        this.versionCode = jsonObject.getString("versionCode");
    }

    public ProductRequestVO() {
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }
}
