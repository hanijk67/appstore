package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.Organization;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by A.Moshiri on 4/9/2018.
 */
public class OrganizationVO implements Serializable{
    public OrganizationVO() {
    }

    public OrganizationVO(String request) {
        JSONObject jsonObject = new JSONObject(request);
        if (jsonObject.has("id")) {
            this.id = jsonObject.getLong("id");
        }

        if (jsonObject.has("parentId") && !jsonObject.isNull("parentId")) {
            this.parentId = jsonObject.getLong("parentId");
        }
        if (jsonObject.has("nickName")) {
            this.nickName = jsonObject.getString("nickName");
        }

        if (jsonObject.has("fullName")) {
            this.fullName = jsonObject.getString("fullName");
        }

        if (jsonObject.has("englishFullName")) {
            this.englishFullName = jsonObject.getString("englishFullName");
        }

        if (jsonObject.has("iconFilePath")) {
            this.iconFilePath = jsonObject.getString("iconFilePath");
        }

        if (jsonObject.has("titleFa") && !jsonObject.isNull("titleFa")) {
            this.titleFa = jsonObject.getString("titleFa");
        }

    }

    Long id;
    String nickName;
    String fullName;
    String englishFullName;
    String iconFilePath;
    String iconFileName;
    String titleFa;
    OrganizationVO parent;
    Long parentId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEnglishFullName() {
        return englishFullName;
    }

    public void setEnglishFullName(String englishFullName) {
        this.englishFullName = englishFullName;
    }

    public String getIconFilePath() {
        return iconFilePath;
    }

    public void setIconFilePath(String iconFilePath) {
        this.iconFilePath = iconFilePath;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public void setIconFileName(String iconFileName) {
        this.iconFileName = iconFileName;
    }

    public OrganizationVO getParent() {
        return parent;
    }

    public void setParent(OrganizationVO parent) {
        this.parent = parent;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getTitleFa() {
        return titleFa;
    }

    public void setTitleFa(String titleFa) {
        this.titleFa = titleFa;
    }

    public static OrganizationVO buildOrganizationVO(Organization organization) {
        OrganizationVO organizationVO = new OrganizationVO();
        if (organization != null) {
            organizationVO.setId(organization.getId());
            organizationVO.setFullName(organization.getFullName());
            organizationVO.setNickName(organization.getNickName());
            organizationVO.setEnglishFullName(organization.getEnglishFullName());
            if (organization.getIconFile() != null) {
                organizationVO.setIconFilePath(organization.getIconFile().getFilePath());
                organizationVO.setIconFileName(FileServerService.Instance.getFileNameFromFilePath(organizationVO.getIconFilePath()));
            }
        }

        return organizationVO;
    }
}
