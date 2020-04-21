package com.fanap.midhco.appstore.restControllers.vos;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by A.Moshiri on 7/17/2017.
 */
public class AppInstalledVO {

    public AppInstalledVO() {
    }

    public AppInstalledVO(String inputString) {
        JSONObject jsonObject = new JSONObject(inputString);
        try {
            if (jsonObject.has("packageList")) {
                JSONArray packageJsonArray = jsonObject.getJSONArray("packageList");
                packageList = new ArrayList<>();
                for(int i = 0; i < packageJsonArray.length(); i++) {
                    packageList.add(packageJsonArray.getString(i));
            }
            }
            if (jsonObject.has("deviceId")) {
                this.deviceId = jsonObject.getString("deviceId");
            }
        } finally {

        }
    }

    List<String> packageList;
    String deviceId;

    public List<String> getPackageList() {
        return packageList;
    }

    public void setPackageList(List<String> packageList) {
        this.packageList = packageList;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
