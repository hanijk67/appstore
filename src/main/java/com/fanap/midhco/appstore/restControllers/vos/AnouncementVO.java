package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.applicationUtils.JarUtil;
import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.AnouncementType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.environment.EnvironmentService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by admin123 on 8/28/2017.
 */
public class AnouncementVO {
    public AnouncementVO() {
    }

    public AnouncementVO(String request) {
        JSONObject jsonObject = new JSONObject(request);

        if (jsonObject.has("id")) {
            this.id = jsonObject.getLong("id");
        }

        if (jsonObject.has("announcementText")) {
            this.anouncementText = jsonObject.getString("announcementText");
        }

        if (jsonObject.has("imageFileKey")) {
            this.anouncementImageFileKey = jsonObject.getString("imageFileKey");
        }

        if (jsonObject.has("actionCategory")) {
            this.actionCategory = jsonObject.getString("actionCategory");
        }

        if (jsonObject.has("actionDescriptor")) {
            JSONObject actionDescriptorJson = jsonObject.getJSONObject("actionDescriptor");
            this.actionDescriptor = actionDescriptorJson.toString();
        }

        if (jsonObject.has("isActive")) {
            if (jsonObject.get("isActive") != null) {
                this.isActive = jsonObject.getBoolean("isActive");
            }
        }

        if (jsonObject.has("isExpired")) {
            if (jsonObject.get("isExpired") != null) {
                this.isExpired = jsonObject.getBoolean("isExpired");
            }
        }

        if (jsonObject.has("anouncementType")) {
            try {
                AnouncementType selectedAnnouncementType = new AnouncementType(Integer.valueOf(jsonObject.get("anouncementType").toString()));
                if (selectedAnnouncementType != null) {
                    this.anouncementType = selectedAnnouncementType;
                }
            } catch (Exception e) {
                this.anouncementType = null;
            }
        }


        if (jsonObject.has("types")) {
            JSONArray typesJasonArray = jsonObject.getJSONArray("types");
            Set<AnouncementType> typeSet = new HashSet<>();

            for (Object statusJsonObj : typesJasonArray) {
                AnouncementType type = new AnouncementType(Integer.valueOf(statusJsonObj.toString()));
                if (type != null && type.toString()!=null) {
                    typeSet.add(type);
                }
            }

            if (typeSet.size()>0) {
                this.anouncementTypeSet = typeSet;
            }
        }


        if (jsonObject.has("startDateTime")) {
            Long startDateLong = jsonObject.getLong("startDateTime");
            Date startDate = new Date(startDateLong);
            DateTime startDateTime = new DateTime(startDate);
            this.startDateTime = startDateTime;
        }

        if (jsonObject.has("expireDateTime")) {
            Long expireDateLong = jsonObject.getLong("expireDateTime");
            Date expireDate = new Date(expireDateLong);
            DateTime expireDateTime = new DateTime(expireDate);
            this.expireDateTime = expireDateTime;
        }

        if (jsonObject.has("osTypeIds") || jsonObject.has("organizationIds") || jsonObject.has("environmentIds")) {
            Session session = HibernateUtil.getNewSession();

            if (jsonObject.has("osTypeIds")) {
                JSONArray osTypeJasonArray = jsonObject.getJSONArray("osTypeIds");
                Set<OSType> osTypeList = new HashSet<>();
                for (Object osTypeJsonObj : osTypeJasonArray) {
                    OSType osType = (OSType) session.get(OSType.class, Long.valueOf(osTypeJsonObj.toString()));
                    if (osType != null) {
                        osTypeList.add(osType);
                    }
                }
                if (osTypeList.size() > 0) {
                    this.osTypes = osTypeList;
                }
            }

            if (jsonObject.has("organizationIds")) {
                JSONArray organizationJasonArray = jsonObject.getJSONArray("organizationIds");
                Set<Organization> organizationsList = new HashSet<>();
                for (Object organizationJsonObj : organizationJasonArray) {
                    Organization organization = (Organization) session.get(Organization.class, Long.valueOf(organizationJsonObj.toString()));
                    if (organization != null) {
                        organizationsList.add(organization);
                    }
                }
                if (organizationsList.size() > 0) {
                    this.organizations = organizationsList;
                }
            }

            if (jsonObject.has("environmentIds")) {
                JSONArray environmentJasonArray = jsonObject.getJSONArray("environmentIds");
                Set<OSEnvironment> environmentList = new HashSet<>();
                for (Object environmentJsonObj : environmentJasonArray) {
                    OSEnvironment environment = (OSEnvironment) session.get(OSEnvironment.class, Long.valueOf(environmentJsonObj.toString()));
                    if (environment != null) {
                        environmentList.add(environment);
                    }
                }
                if (environmentList.size() > 0) {
                    this.osEnvironments = environmentList;
                }
            }
            session.close();
        }
    }

    Long id;
    String anouncementImageFileKey;
    String actionCategory;
    String actionDescriptor;
    JSONObject actionDescriptorObject;
    String anouncementText;
    AnouncementType anouncementType;
    Set<AnouncementType> anouncementTypeSet;
    Set<OSType> osTypes;
    Set<OSEnvironment> osEnvironments;
    Set<Organization> organizations;
    DateTime startDateTime;
    DateTime expireDateTime;
    Boolean isActive;
    Boolean isExpired;


    Set<OSTypeVO> osTypeVOSet;
    Set<OsEnvironmentVO> osEnvironmentVOSet;
    Set<OrganizationVO> organizationVOSet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnouncementImageFileKey() {
        return anouncementImageFileKey;
    }

    public void setAnouncementImageFileKey(String anouncementImageFileKey) {
        this.anouncementImageFileKey = anouncementImageFileKey;
    }

    public String getActionCategory() {
        return actionCategory;
    }

    public void setActionCategory(String actionCategory) {
        this.actionCategory = actionCategory;
    }

    public String getActionDescriptor() {
        return actionDescriptor;
    }

    public void setActionDescriptor(String actionDescriptor) {
        this.actionDescriptor = actionDescriptor;
    }

    public String getAnouncementText() {
        return anouncementText;
    }

    public void setAnouncementText(String anouncementText) {
        this.anouncementText = anouncementText;
    }

    public AnouncementType getAnouncementType() {
        return anouncementType;
    }

    public void setAnouncementType(AnouncementType anouncementType) {
        this.anouncementType = anouncementType;
    }

    public Set<OSType> getOsTypes() {
        return osTypes;
    }

    public void setOsTypes(Set<OSType> osTypes) {
        this.osTypes = osTypes;
    }

    public Set<OSEnvironment> getOsEnvironments() {
        return osEnvironments;
    }

    public void setOsEnvironments(Set<OSEnvironment> osEnvironments) {
        this.osEnvironments = osEnvironments;
    }

    public Set<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Set<Organization> organizations) {
        this.organizations = organizations;
    }

    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(DateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public DateTime getExpireDateTime() {
        return expireDateTime;
    }

    public void setExpireDateTime(DateTime expireDateTime) {
        this.expireDateTime = expireDateTime;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getExpired() {
        return isExpired;
    }

    public void setExpired(Boolean expired) {
        isExpired = expired;
    }

    public Set<OSTypeVO> getOsTypeVOSet() {
        return osTypeVOSet;
    }

    public void setOsTypeVOSet(Set<OSTypeVO> osTypeVOSet) {
        this.osTypeVOSet = osTypeVOSet;
    }

    public Set<OsEnvironmentVO> getOsEnvironmentVOSet() {
        return osEnvironmentVOSet;
    }

    public void setOsEnvironmentVOSet(Set<OsEnvironmentVO> osEnvironmentVOSet) {
        this.osEnvironmentVOSet = osEnvironmentVOSet;
    }

    public Set<OrganizationVO> getOrganizationVOSet() {
        return organizationVOSet;
    }

    public void setOrganizationVOSet(Set<OrganizationVO> organizationVOSet) {
        this.organizationVOSet = organizationVOSet;
    }

    public Set<AnouncementType> getAnouncementTypeSet() {
        return anouncementTypeSet;
    }

    public void setAnouncementTypeSet(Set<AnouncementType> anouncementTypeSet) {
        this.anouncementTypeSet = anouncementTypeSet;
    }

    public JSONObject getActionDescriptorObject() {
        return actionDescriptorObject;
    }

    public void setActionDescriptorObject(JSONObject actionDescriptorObject) {
        this.actionDescriptorObject = actionDescriptorObject;
    }

    public static AnouncementVO buildVO(Anouncement anouncement) {
        AnouncementVO anouncementVO = new AnouncementVO();

        anouncementVO.id = anouncement.getId();
        anouncementVO.actionCategory = anouncement.getActionCategory();
        if(anouncement.getActionDescriptor()!=null){
            String actionDescriptorString = anouncement.getActionDescriptor();
            JSONObject actionDescriptorObj = new JSONObject(actionDescriptorString );
            anouncementVO.setActionDescriptorObject(actionDescriptorObj);
        }
        anouncementVO.anouncementText = anouncement.getAnouncementText();
        anouncementVO.anouncementType = anouncement.getAnouncementType();

        Set<Organization> orgSet = new HashSet<>(anouncement.getOrganizations());
        Set<OSType> osTypeSet = new HashSet<>(anouncement.getOsTypes());
        Set<OSEnvironment> osEnvironmentSet = new HashSet<>(anouncement.getOsEnvironments());


        Stream<OsEnvironmentVO> environmentVOStream = osEnvironmentSet.stream().map(environment -> OsEnvironmentVO.buildEnvironmentVOByEnvironment(environment));
        List<OsEnvironmentVO> environmentVOList = environmentVOStream.collect(Collectors.<OsEnvironmentVO>toList());
        Set<OsEnvironmentVO> environmentVOSet = new HashSet<OsEnvironmentVO>(environmentVOList);


        // for not show script in announcement Vo
        for(OSType osTypeInSet : osTypeSet){
            osTypeInSet.setOsCompareScript(null);
        }
        Stream<OSTypeVO> osTypeVOStream = osTypeSet.stream().map(osType -> OSTypeVO.buildOsTypeVO(osType));
        List<OSTypeVO> osTypeVOList = osTypeVOStream.collect(Collectors.<OSTypeVO>toList());
        Set<OSTypeVO> osTypeVOSet = new HashSet<OSTypeVO>(osTypeVOList);

        Stream<OrganizationVO> organizationVOStream = orgSet.stream().map(organization -> OrganizationVO.buildOrganizationVO(organization));
        List<OrganizationVO> organizationVOList = organizationVOStream.collect(Collectors.<OrganizationVO>toList());
        Set<OrganizationVO> organizationVOSet = new HashSet<OrganizationVO>(organizationVOList);

        anouncementVO.setOrganizationVOSet(organizationVOSet);
        anouncementVO.setOsTypeVOSet(osTypeVOSet);
        anouncementVO.setOsEnvironmentVOSet(environmentVOSet);


        anouncementVO.setAnouncementImageFileKey(
                FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}",
                        anouncement.getAnouncementImageFileKey()));

        return anouncementVO;
    }
}
