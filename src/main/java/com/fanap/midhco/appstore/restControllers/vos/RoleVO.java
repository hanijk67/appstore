package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.Role;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import org.json.JSONObject;

/**
 * Created by A.Moshiri on 3/26/2018.
 */
public class RoleVO {
    private Long id;
    private String name;
    private String accessCodes;
    private Boolean isEditable;
    private int roleVoStatusInt;
    private String roleVoStatusDesc;

    public RoleVO() {
    }

    public RoleVO(String request) {
        JSONObject jsonObject = new JSONObject(request);
        this.roleVoStatusInt = ResultStatus.UNSUCCESSFUL.getState();

        if (jsonObject.has("id")) {
            this.id = jsonObject.getLong("id");
        }
        if (jsonObject.has("name") && jsonObject.getString("name") != null && !jsonObject.getString("name").trim().equals("")) {
            this.name = jsonObject.getString("name");
        }

        if (jsonObject.has("accessCodes") && jsonObject.getString("accessCodes") != null && !jsonObject.getString("accessCodes").trim().equals("")) {
            this.accessCodes = jsonObject.getString("accessCodes");
        }

        if (jsonObject.has("isEditable")) {
            if (jsonObject.get("isEditable") != null) {
                this.isEditable = jsonObject.getBoolean("isEditable");
            }
        }

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessCodes() {
        return accessCodes;
    }

    public void setAccessCodes(String accessCodes) {
        this.accessCodes = accessCodes;
    }

    public Boolean getEditable() {
        return isEditable;
    }

    public void setEditable(Boolean editable) {
        isEditable = editable;
    }

    public int getRoleVoStatusInt() {
        return roleVoStatusInt;
    }

    public void setRoleVoStatusInt(int roleVoStatusInt) {
        this.roleVoStatusInt = roleVoStatusInt;
    }

    public String getRoleVoStatusDesc() {
        return roleVoStatusDesc;
    }

    public void setRoleVoStatusDesc(String roleVoStatusDesc) {
        this.roleVoStatusDesc = roleVoStatusDesc;
    }

    public static RoleVO buildRoleVOByRole(Role role) {
        RoleVO roleVO = new RoleVO();
        if (role != null) {
            roleVO.setId(role.getId());
            roleVO.setName(role.getName());
            roleVO.setAccessCodes(role.getAccessCodes());
            roleVO.setEditable(role.getEditable());
        }
        return roleVO;

    }
}
