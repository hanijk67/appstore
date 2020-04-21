package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.OS;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.service.os.OSService;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by admin123 on 8/12/2017.
 */
public class AppUploadVO {
    Long osId;
    OSType osType;
    String packageName;
    String title;
    String description;
    String shortDescription;
    Long categoryId;
    AppPackageVO appPackageVO;
    Long developerUserId;
    Long id;
    public String publishState;

    public List<String> keywords;

    public AppUploadVO() {
    }

    public AppUploadVO(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        if (jsonObject.has("id") && !jsonObject.get("id").equals(JSONObject.NULL))
            this.id = jsonObject.getLong("id");
        if(jsonObject.has("osId") && !jsonObject.get("osId").equals(JSONObject.NULL))
            this.osId = jsonObject.getLong("osId");
        if (jsonObject.has("developerUserId") && !jsonObject.get("developerUserId").equals(JSONObject.NULL))
            this.osId = jsonObject.getLong("developerUserId");

        if(jsonObject.has("packageName") && !jsonObject.get("packageName").equals(JSONObject.NULL))
            this.packageName = jsonObject.getString("packageName");
        if(jsonObject.has("title") && !jsonObject.get("title").equals(JSONObject.NULL))
            this.title = jsonObject.getString("title");
        if(jsonObject.has("description") && !jsonObject.get("description").equals(JSONObject.NULL))
            this.description = jsonObject.getString("description");
        if(jsonObject.has("shortDescription") && !jsonObject.get("shortDescription").equals(JSONObject.NULL))
            this.shortDescription = jsonObject.getString("shortDescription");
        if(jsonObject.has("categoryId") && !jsonObject.get("categoryId").equals(JSONObject.NULL))
            this.categoryId = jsonObject.getLong("categoryId");
        if(jsonObject.has("appPackageVO")  && !jsonObject.get("appPackageVO").equals(JSONObject.NULL))
            this.appPackageVO = new AppPackageVO(jsonObject.getJSONObject("appPackageVO").toString());

        if (jsonObject.has("publishState") && !jsonObject.isNull("publishState")) {
            this.publishState = jsonObject.get("publishState").toString();
        }

        if (jsonObject.has("keywords")) {
            JSONArray keywordJasonArray = jsonObject.getJSONArray("keywords");
            Set<String> keywordList = new HashSet<>();

            for (Object keywordJsonObj : keywordJasonArray) {
                keywordList.add(keywordJsonObj.toString());
            }
            if (keywordList.size() > 0) {
                this.keywords = new ArrayList<>(keywordList);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOsId() {
        return osId;
    }

    public void setOsId(Long osId) {
        this.osId = osId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public AppPackageVO getAppPackageVO() {
        return appPackageVO;
    }

    public void setAppPackageVO(AppPackageVO appPackageVO) {
        this.appPackageVO = appPackageVO;
    }

    public Long getDeveloperUserId() {
        return developerUserId;
    }

    public void setDeveloperUserId(Long developerUserId) {
        this.developerUserId = developerUserId;
    }

    public String getPublishState() {
        return publishState;
    }

    public void setPublishState(String publishState) {
        this.publishState = publishState;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public OSType getOsType() {
        return osType;
    }

    public void setOsType(OSType osType) {
        this.osType = osType;
    }

    public void checkValidity(Session session, OSType osType) throws Exception {
        if(this.packageName == null || this.packageName.trim().isEmpty()) {
            throw new Exception("appPackageName is Empty!");
        }

        if(this.title == null || this.title.trim().isEmpty()) {
            throw new Exception("app title is Empty!");
        }

        if(this.shortDescription == null || this.shortDescription.trim().isEmpty()) {
            throw new Exception("app short Description is Empty!");
        }

        if(this.categoryId == null) {
            throw new Exception("categoryId is Empty!");
        }

        if(this.appPackageVO == null) {
            throw new Exception("app upload has no package!");
        }

        if(this.categoryId == null) {
            throw new Exception("categoryId is Empty!");
        }

        this.appPackageVO.checkValidity();

        OS os = null;
        if(this.osId != null) {
            os = OSService.Instance.loadOSByOSId(osId, session);
            if(os == null) {
                throw new Exception("no such OS Exists!");
            }
        }
    }

    public static void main(String[] args) {
        new AppUploadVO("{\"osId\":3019,\"appPackageName\":\"com.test.mtos\",\"title\":\"com.test.mtos\",\"description\":\"\",\"categoryId\":1022,\"appPackageVO\":{\"packageFileKey\":\"34860243A7BD09F5BE796D2D1531103D5.852656502056086E15\",\"packageFileName\":\"com.test.mtos-1.0.warc\",\"iconFileKey\":null,\"iconFileName\":null,\"packageImagesKeys\":null,\"packageImageNames\":null}}");
    }
}
