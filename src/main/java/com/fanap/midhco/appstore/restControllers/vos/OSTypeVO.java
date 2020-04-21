package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.OSType;
import org.json.*;

/**
 * Created by admin123 on 2/6/2017.
 */
public class OSTypeVO {
    Long osTypeID;
    String osName;
    Boolean disabled;
    String osCompareScriptFileKey;
    String osCompareScript;

    public OSTypeVO() {}

    public OSTypeVO(String request) {
        JSONObject jsonObject = new JSONObject(request);

        if(jsonObject.has("osTypeID")) {
            this.osTypeID = jsonObject.getLong("osTypeID");
        }

        if(jsonObject.has("osName")) {
            this.osName = jsonObject.getString("osName");
        }

        if (jsonObject.has("disabled")) {
            if (jsonObject.get("disabled") != null) {
                this.disabled = jsonObject.getBoolean("disabled");
            }
        }

        if(jsonObject.has("osCompareScriptFileKey") ) {
            this.osCompareScriptFileKey = jsonObject.getString("osCompareScriptFileKey");
        }

    }

    public Long getOsTypeID() {
        return osTypeID;
    }

    public void setOsTypeID(Long osTypeID) {
        this.osTypeID = osTypeID;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getOsCompareScriptFileKey() {
        return osCompareScriptFileKey;
    }

    public void setOsCompareScriptFileKey(String osCompareScriptFileKey) {
        this.osCompareScriptFileKey = osCompareScriptFileKey;
    }

    public String getOsCompareScript() {
        return osCompareScript;
    }

    public void setOsCompareScript(String osCompareScript) {
        this.osCompareScript = osCompareScript;
    }

    public static OSTypeVO buildOsTypeVO(OSType osType) {
        OSTypeVO osTypeVO = new OSTypeVO();
        osTypeVO.setOsTypeID(osType.getId());
        osTypeVO.setOsName(osType.getName());
        osTypeVO.setDisabled(osType.getDisabled());
        osTypeVO.setOsCompareScript(osType.getOsCompareScript());
        return osTypeVO;
    }
}
