package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.HandlerApp;
import com.fanap.midhco.appstore.entities.OS;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.service.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.mapping.Collection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by admin123 on 2/7/2017.
 */
public class OSVO {

    public OSVO() {
    }

    public OSVO(String request) {
        JSONObject jsonObject = new JSONObject(request);
        if (jsonObject.has("osId")) {
            this.osId = jsonObject.getLong("osId");
        }

        if (jsonObject.has("osName")) {
            this.osName = jsonObject.getString("osName");
        }

        if (jsonObject.has("osCode")) {
            this.osCode = jsonObject.getString("osCode");
        }

        if (jsonObject.has("osVersion")) {
            this.osVersion = jsonObject.getString("osVersion");
        }
        if (jsonObject.has("disabled")) {
            if (jsonObject.get("disabled") != null) {
                this.disabled = jsonObject.getBoolean("disabled");
            }
        }

        if (jsonObject.has("osTypeIds") || jsonObject.has("osTypeId")) {

            Session session = HibernateUtil.getNewSession();
            if (jsonObject.has("osTypeIds")) {
                JSONArray osTypeAppJasonArray = jsonObject.getJSONArray("osTypeIds");
                Set<OSType> osTypeList = new HashSet<>();

                for (Object osTypeJsonObj : osTypeAppJasonArray) {

                    OSType osType = (OSType) session.get(OSType.class, Long.valueOf(osTypeJsonObj.toString()));
                    if (osType != null) {
                        osTypeList.add(osType);
                    }
                }
                if (osTypeList.size() > 0) {
                    this.osTypes = osTypeList;
                }
            }

            if (jsonObject.has("osTypeId")) {
                OSType osType = (OSType) session.get(OSType.class, Long.valueOf(jsonObject.getLong("osTypeId")));
                if (osType != null && osType.getDisabled() == false) {
                    this.osType = osType;
                }
            }

            session.close();
        }
    }

    Long osId;
    private Set<OSType> osTypes;
    String osName;
    String osCode;
    String osVersion;
    List<HandlerAppVO> handlerAppVOs;
    OSTypeVO osTypeVO;
    OSType osType;
    Long osTypeId;
    List<Long> handlerAppsId;
    String osTypeName;
    Boolean disabled;

    public Long getOsId() {
        return osId;
    }

    public void setOsId(Long osId) {
        this.osId = osId;
    }

    public Set<OSType> getOsTypes() {
        return osTypes;
    }

    public void setOsTypes(Set<OSType> osTypes) {
        this.osTypes = osTypes;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public List<HandlerAppVO> getHandlerAppVOs() {
        return handlerAppVOs;
    }

    public void setHandlerAppVOs(List<HandlerAppVO> handlerAppVOs) {
        this.handlerAppVOs = handlerAppVOs;
    }

    public OSTypeVO getOsTypeVO() {
        return osTypeVO;
    }

    public void setOsTypeVO(OSTypeVO osTypeVO) {
        this.osTypeVO = osTypeVO;
    }

    public OSType getOsType() {
        return osType;
    }

    public void setOsType(OSType osType) {
        this.osType = osType;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getOsCode() {
        return osCode;
    }

    public void setOsCode(String osCode) {
        this.osCode = osCode;
    }

    public Long getOsTypeId() {
        return osTypeId;
    }

    public void setOsTypeId(Long osTypeId) {
        this.osTypeId = osTypeId;
    }

    public String getOsTypeName() {
        return osTypeName;
    }

    public void setOsTypeName(String osTypeName) {
        this.osTypeName = osTypeName;
    }

    public List<Long> getHandlerAppsId() {
        return handlerAppsId;
    }

    public void setHandlerAppsId(List<Long> handlerAppsId) {
        this.handlerAppsId = handlerAppsId;
    }

    public static OSVO buildOSVO(OS os) {
        OSVO osvo = new OSVO();
        if (os != null) {
            osvo.setOsId(os.getId());
            osvo.setOsName(os.getOsName());
            if (os.getOsType() != null) {
//                osvo.setOsTypeVO(OSTypeVO.buildOsTypeVO(os.getOsType()));
                osvo.setOsTypeId(os.getOsType().getId());
                osvo.setOsTypeName(os.getOsType().getName());
            }

            List<HandlerAppVO> handlerAppVOList = null;
            List<Long> handlerAppIdList = null;
            if (os.getHandlerApps() != null) {
                List<HandlerApp> handlerAppList = os.getHandlerApps();
                handlerAppVOList = new ArrayList<>();
                handlerAppIdList = new ArrayList<>();
                if (handlerAppList != null && handlerAppList.size() > 0) {
                    for (HandlerApp handlerApp : handlerAppList) {
                        if (handlerApp.getActive()) {
                            handlerAppVOList.add(HandlerAppVO.buildHandlerAppVO(handlerApp));
                            handlerAppIdList.add(handlerApp.getId());
                        }
                    }
                    Collections.sort(handlerAppVOList);
                }
            }
            osvo.setOsCode(os.getOsCode());
            osvo.setHandlerAppVOs(handlerAppVOList);
//            osvo.setOsType(os.getOsType());
            osvo.setHandlerAppsId(handlerAppIdList);
            osvo.setOsVersion(os.getOsVersion());
            osvo.setDisabled(os.getDisabled());
        }

        return osvo;
    }
}
