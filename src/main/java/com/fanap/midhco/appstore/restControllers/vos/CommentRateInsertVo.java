package com.fanap.midhco.appstore.restControllers.vos;

import org.json.JSONObject;

/**
 * Created by A.Moshiri on 8/7/2017.
 */
public class CommentRateInsertVo {

    public CommentRateInsertVo() {
    }

    public CommentRateInsertVo(String inputString) {
        JSONObject jsonObject = new JSONObject(inputString);
        try {
            if (jsonObject.has("commentText") && !jsonObject.isNull("commentText")) {
                this.commentText = jsonObject.getString("commentText");
            }
            if (jsonObject.has("language") && !jsonObject.isNull("language")) {
                this.language = jsonObject.getString("language");
            }
            if (jsonObject.has("packageName") && !jsonObject.isNull("packageName")) {
                this.packageName = jsonObject.getString("packageName");
            }
            if (jsonObject.has("commentRateId") && !jsonObject.isNull("commentRateId")) {
                this.commentRateId = jsonObject.getString("commentRateId");
            }
            if (jsonObject.has("userId") && !jsonObject.isNull("userId")) {
                this.userId = jsonObject.getLong("userId");
            }
            if (jsonObject.has("deviceId") && !jsonObject.isNull("deviceId")) {
                this.deviceId = jsonObject.getString("deviceId");
            }
            if (jsonObject.has("ratingIndex") && !jsonObject.isNull("ratingIndex")) {
                this.ratingIndex = jsonObject.getInt("ratingIndex");
            }
            if (jsonObject.has("approved")) {
                this.approved = jsonObject.getBoolean("approved");
            }

        } finally {

        }
    }

    public String commentText;
    public String language;
    public String deviceId;
    public String packageName;
    public String commentRateId;
    public Integer ratingIndex;
    public Long appId;
    public Long userId;
    boolean  approved;

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getRatingIndex() {
        return ratingIndex;
    }

    public void setRatingIndex(Integer ratingIndex) {
        this.ratingIndex = ratingIndex;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getCommentRateId() {
        return commentRateId;
    }

    public void setCommentRateId(String commentRateId) {
        this.commentRateId = commentRateId;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}
