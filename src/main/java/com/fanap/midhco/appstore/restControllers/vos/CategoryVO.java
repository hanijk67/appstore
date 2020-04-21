package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.AppCategory;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import org.json.JSONObject;

/**
 * Created by admin123 on 2/6/2017.
 */
public class CategoryVO {


    public CategoryVO() {
    }

    public CategoryVO(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        if (jsonObject != null) {
            if (jsonObject.has("id")) {
                this.id = jsonObject.getLong("id");
            }
            if(jsonObject.has("categoryName")){
                this.categoryName = jsonObject.getString("categoryName");
            }

            if(jsonObject.has("iconPath")){
                this.iconPath = jsonObject.getString("iconPath");
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
    Boolean isEnabled;
    Long parentId;
    String iconFileName;
    String categoryName;
    String parentName;
    Boolean isAssignable;
    String iconPath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public void setIconFileName(String iconFileName) {
        this.iconFileName = iconFileName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Boolean getAssignable() {
        return isAssignable;
    }

    public void setAssignable(Boolean assignable) {
        isAssignable = assignable;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public static CategoryVO buildCategoryVO(AppCategory appCategory) {
        CategoryVO categoryVO = new CategoryVO();
        categoryVO.setId(appCategory.getId());
        categoryVO.setCategoryName(appCategory.getCategoryName());
        if (appCategory.getParent()!=null) {
            categoryVO.setParentName(appCategory.getParent().getCategoryName());
            categoryVO.setParentId(appCategory.getParent().getId());
        }
        categoryVO.setAssignable(appCategory.getAssignable());
        categoryVO.setEnabled(appCategory.getEnabled());
        if (appCategory.getIconFile()!=null) {
            String iconDownloadPath =
                    FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}",appCategory.getIconFile().getFilePath());
            categoryVO.setIconPath(iconDownloadPath);
            categoryVO.setIconFileName(appCategory.getIconFile().getFileName());

        }
        return categoryVO;
    }
}
