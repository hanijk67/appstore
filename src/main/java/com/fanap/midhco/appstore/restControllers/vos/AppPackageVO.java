package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin123 on 8/12/2017.
 */
public class AppPackageVO {
    Long id;
    String packageFileKey;
    String packageFileName;
    String versionName;
    String versionCode;

    String iconFileKey;
    String iconFileName;
    String changesLog;

    List<String> packageImagesKeys;
    List<String> packageImageNames;

    public AppPackageVO() {
    }

    public AppPackageVO(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);

        if (jsonObject.has("packageFileKey") && !jsonObject.get("packageFileKey").equals(JSONObject.NULL))
            this.packageFileKey = jsonObject.getString("packageFileKey");

        if (jsonObject.has("packageFileName") && !jsonObject.get("packageFileName").equals(JSONObject.NULL))
            this.packageFileName = jsonObject.getString("packageFileName");

        if (jsonObject.has("iconFileKey") && !jsonObject.get("iconFileKey").equals(JSONObject.NULL)) {
            iconFileKey = jsonObject.getString("iconFileKey");
        }

        if (jsonObject.has("iconFileName") && !jsonObject.get("iconFileName").equals(JSONObject.NULL)) {
            iconFileName = jsonObject.getString("iconFileName");
        }

        if (jsonObject.has("changesLog") && !jsonObject.get("changesLog").equals(JSONObject.NULL)) {
            changesLog = jsonObject.getString("changesLog");
        }

        if (jsonObject.has("versionName") && !jsonObject.get("versionName").equals(JSONObject.NULL)) {
            versionName = jsonObject.getString("versionName");
        }

        if (jsonObject.has("versionCode") && !jsonObject.get("versionCode").equals(JSONObject.NULL)) {
            versionCode = jsonObject.getString("versionCode");
        }

        if (jsonObject.has("packageImagesKeys") && !jsonObject.get("packageImagesKeys").equals(JSONObject.NULL)) {
            packageImagesKeys = new ArrayList<>();
            JSONArray jsonArray = jsonObject.getJSONArray("packageImagesKeys");
            for (int i = 0; i < jsonArray.length(); i++) {
                packageImagesKeys.add(jsonArray.getString(i));
            }
        }

        if (jsonObject.has("packageImageNames") && !jsonObject.get("packageImageNames").equals(JSONObject.NULL)) {
            packageImageNames = new ArrayList<>();
            JSONArray jsonArray = jsonObject.getJSONArray("packageImageNames");
            for (int i = 0; i < jsonArray.length(); i++) {
                packageImageNames.add(jsonArray.getString(i));
            }
        }
    }

    public String getPackageFileKey() {
        return packageFileKey;
    }

    public void setPackageFileKey(String packageFileKey) {
        this.packageFileKey = packageFileKey;
    }

    public String getPackageFileName() {
        return packageFileName;
    }

    public void setPackageFileName(String packageFileName) {
        this.packageFileName = packageFileName;
    }

    public String getIconFileKey() {
        return iconFileKey;
    }

    public void setIconFileKey(String iconFileKey) {
        this.iconFileKey = iconFileKey;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public void setIconFileName(String iconFileName) {
        this.iconFileName = iconFileName;
    }

    public List<String> getPackageImageNames() {
        return packageImageNames;
    }

    public void setPackageImageNames(List<String> packageImageNames) {
        this.packageImageNames = packageImageNames;
    }

    public List<String> getPackageImagesKeys() {
        return packageImagesKeys;
    }

    public void setPackageImagesKeys(List<String> packageImagesKeys) {
        this.packageImagesKeys = packageImagesKeys;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getChangesLog() {
        return changesLog;
    }

    public void setChangesLog(String changesLog) {
        this.changesLog = changesLog;
    }

    public void checkValidity() throws Exception {

        if (packageFileKey == null || packageFileKey.trim().isEmpty()) {
            throw new Exception("app package file is Empty!");
        }

        boolean fileExistsonFileServer = FileServerService.Instance.doesFileExistOnFileServer(packageFileKey);
        if (!fileExistsonFileServer) {
            throw new Exception("file key not exists on fileServer!");
        }


        if (iconFileKey != null && !iconFileKey.trim().equals("")) {
            boolean iconExistsonFileServer = FileServerService.Instance.doesFileExistOnFileServer(iconFileKey);
            if (!iconExistsonFileServer) {
                throw new Exception("icon file key not exists on fileServer!");
            }
        }

        if (packageImagesKeys != null) {
            for (String packageImageKey : packageImagesKeys) {
                if (!packageImageKey.equals("")) {
                    boolean packageFileExistsonFileServer = FileServerService.Instance.doesFileExistOnFileServer(packageImageKey);
                    if (!packageFileExistsonFileServer) {
                        throw new Exception("package file key " + packageImageKey + " not exists on fileServer!");
                    }
                }
            }
        }

    }
}
