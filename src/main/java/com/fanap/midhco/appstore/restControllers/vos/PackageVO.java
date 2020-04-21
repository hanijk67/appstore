package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.File;
import com.fanap.midhco.appstore.entities.PublishState;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin123 on 8/17/2017.
 */
public class PackageVO {
    String versionCode;
    String versionName;
    String publishStateStr;
    PublishState publishState;
    String appPackageName;
    String iconFileKey;
    String packageFileKey;
    List<String> imagesFileKey;
    String changesLog;
    File iconFile;
    List<File> thumbFiles;

    Long id;

    public PackageVO() {
    }

    public PackageVO(String jsonString) {
        if (jsonString != null && !jsonString.trim().equals("")) {
            JSONObject jsonObject = new JSONObject(jsonString);

            if (jsonObject.has("versionCode") && !jsonObject.get("versionCode").equals(JSONObject.NULL))
                this.versionCode = jsonObject.getString("versionCode");

            if (jsonObject.has("versionName") && !jsonObject.get("versionName").equals(JSONObject.NULL))
                this.versionName = jsonObject.getString("versionName");

            if (jsonObject.has("appPackageName") && !jsonObject.get("appPackageName").equals(JSONObject.NULL))
                this.appPackageName = jsonObject.getString("appPackageName");

            if (jsonObject.has("publishStateStr") && !jsonObject.get("publishStateStr").equals(JSONObject.NULL)) {
                this.publishStateStr = jsonObject.getString("publishStateStr");
            }

            if (jsonObject.has("iconFileKey") && !jsonObject.get("iconFileKey").equals(JSONObject.NULL)) {
                this.iconFileKey = jsonObject.getString("iconFileKey");
            }

            if (jsonObject.has("packageFileKey") && !jsonObject.get("packageFileKey").equals(JSONObject.NULL)) {
                this.iconFileKey = jsonObject.getString("packageFileKey");
            }

            if (jsonObject.has("changesLog") && !jsonObject.get("changesLog").equals(JSONObject.NULL)) {
                this.changesLog = jsonObject.getString("changesLog");
            }

            if (jsonObject.has("packageImagesKeys")) {
                JSONArray jsonArray = jsonObject.getJSONArray("packageImagesKeys");
                List<String> imageFileKeys = new ArrayList<>();
                for (Object obj : jsonArray) {
                    imageFileKeys.add(obj.toString());
                }
                if (!imageFileKeys.isEmpty()) {
                    this.imagesFileKey = imageFileKeys;
                }
            }

            if (jsonObject.has("id")) {
                this.id = jsonObject.getLong("id");
            }
        }
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getPublishStateStr() {
        return publishStateStr;
    }

    public void setPublishStateStr(String publishStateStr) {
        this.publishStateStr = publishStateStr;
    }

    public PublishState getPublishState() {
        return publishState;
    }

    public void setPublishState(PublishState publishState) {
        this.publishState = publishState;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public String getIconFileKey() {
        return iconFileKey;
    }

    public void setIconFileKey(String iconFileKey) {
        this.iconFileKey = iconFileKey;
    }

    public List<String> getImagesFileKey() {
        return imagesFileKey;
    }

    public void setImagesFileKey(List<String> imagesFileKey) {
        this.imagesFileKey = imagesFileKey;
    }

    public String getPackageFileKey() {
        return packageFileKey;
    }

    public void setPackageFileKey(String packageFileKey) {
        this.packageFileKey = packageFileKey;
    }

    public String getChangesLog() {
        return changesLog;
    }

    public void setChangesLog(String changesLog) {
        this.changesLog = changesLog;
    }

    public File getIconFile() {
        return iconFile;
    }

    public void setIconFile(File iconFile) {
        this.iconFile = iconFile;
    }

    public List<File> getThumbFiles() {
        return thumbFiles;
    }

    public void setThumbFiles(List<File> thumbFiles) {
        this.thumbFiles = thumbFiles;
    }

    public static List<PackageVO> buildPackageVO(List<AppPackageService.AppPackageSearchResult> searchResultList) {
        List<PackageVO> packageVOList = new ArrayList<>();

        for(AppPackageService.AppPackageSearchResult searchResult : searchResultList) {
            PackageVO packageVO = new PackageVO();
            packageVO.versionCode = searchResult.getAppPackage().getVersionCode();
            packageVO.versionName = searchResult.getAppPackage().getVersionName();
            packageVO.publishState = searchResult.getAppPackage().getPublishState();
            packageVOList.add(packageVO);
        }

        return packageVOList;
    }
}
