package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.AppCategory;
import org.json.JSONObject;

/**
 * Created by A.Moshiri on 4/7/2018.
 */
public class AppCategoryVO {
    public AppCategoryVO() {
    }

    public AppCategoryVO(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        if (jsonObject != null) {
            if (jsonObject.has("id")) {
                this.id = jsonObject.getLong("id");
            }
            if(jsonObject.has("categoryName")){
                this.categoryName = jsonObject.getString("categoryName");
            }

            if(jsonObject.has("iconFileKey")){
                this.iconFileKey = jsonObject.getString("iconFileKey");
            }

            if(jsonObject.has("isEnabled")){
                if(jsonObject.get("isEnabled") !=null){
                    this.isEnabled = jsonObject.getBoolean("isEnabled");
                }
            }
            if(jsonObject.has("isAssignable")){
                if(jsonObject.get("isAssignable") !=null){
                    this.isAssignable = jsonObject.getBoolean("isAssignable");
                }
            }

            if(jsonObject.has("parentId")){
                this.parentId  = jsonObject.getLong("parentId");
            }
        }
    }

    Long id;
    String categoryName;
    Boolean isEnabled;
    Boolean isAssignable;
    Long parentId;
    String iconFileKey;
    String iconFileName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Boolean getAssignable() {
        return isAssignable;
    }

    public void setAssignable(Boolean assignable) {
        isAssignable = assignable;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getIconFileKey() {
        return iconFileKey;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public void setIconFileName(String iconFileName) {
        this.iconFileName = iconFileName;
    }

    public void setIconFileKey(String iconFileKey) {
        this.iconFileKey = iconFileKey;
    }

    public  static AppCategoryVO buildAppCategoryVByAppCategory(AppCategory appCategory){
        AppCategoryVO appCategoryVO = new AppCategoryVO();
        if(appCategory!=null){
            appCategoryVO.setId(appCategory.getId());
            appCategoryVO.setEnabled(appCategory.getEnabled());
            appCategoryVO.setAssignable(appCategory.getAssignable());
            appCategoryVO.setCategoryName(appCategory.getCategoryName());
            if (appCategory.getParent()!=null) {
                appCategoryVO.setParentId(appCategory.getParent().getId());
            }
            if(appCategory.getIconFile()!=null){
                appCategoryVO.setIconFileKey(appCategory.getIconFile().getFilePath());
                appCategoryVO.setIconFileName(appCategory.getIconFile().getFileName());

            }
        }
        return appCategoryVO;
    }
}
