package com.fanap.midhco.appstore.service.handlerApp;

import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.vos.HandlerAppVO;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.environment.EnvironmentService;
import com.fanap.midhco.appstore.service.org.OrgService;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by A.Moshiri on 8/14/2017.
 */


public class HandlerAppService {

    public static HandlerAppService Instance = new HandlerAppService();

    private HandlerAppService() {
    }

    public void saveOrUpdate(HandlerApp handlerApp, Session session) {
        if (handlerApp.getId() == null) {
            handlerApp.setCreationDate(DateTime.now());
            handlerApp.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            handlerApp.setLastModifyDate(DateTime.now());
            handlerApp.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(handlerApp);
    }


    public static class HandlerAppCriteria implements Serializable {
        Long id;
        Long versionCode;
        Boolean isActive;
        Boolean isDefault;
        Organization organization;
        //        Integer osEnvironment;
        OSEnvironment osEnvironment;
        List<Long> osIds = new ArrayList<>();

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(Long versionCode) {
            this.versionCode = versionCode;
        }

        public List<Long> getOsIds() {
            return osIds;
        }

        public void setOsIds(List<Long> osIds) {
            this.osIds = osIds;
        }

        public Boolean getActive() {
            return isActive;
        }

        public void setActive(Boolean active) {
            isActive = active;
        }

        public Boolean getDefault() {
            return isDefault;
        }

        public void setDefault(Boolean aDefault) {
            isDefault = aDefault;
        }

        public Organization getOrganization() {
            return organization;
        }

        public void setOrganization(Organization organization) {
            this.organization = organization;
        }

        public OSEnvironment getOsEnvironment() {
            return osEnvironment;
        }

        public void setOsEnvironment(OSEnvironment osEnvironment) {
            this.osEnvironment = osEnvironment;
        }
    }

    public static class HandlerAppSearchResultModel implements Serializable {
        Long id;
        Long versionCode;
        Boolean isActive;
        Boolean isDefault;
        DateTime uploadedFileDate;
        DateTime uploadedTestFileDate;
        String fileHandlerAppKey;
        String testFileHandlerAppKey;

        String fileHandlerAppKey32Bit;
        String testFileHandlerAppKey32Bit;
        String nickName;
        String osName;
        OSEnvironment osEnvironment;
        Organization organization;
        Long osId;
        Long organizationId;

        public Long getOsId() {
            return osId;
        }

        public void setOsId(Long osId) {
            this.osId = osId;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(Long versionCode) {
            this.versionCode = versionCode;
        }

        public Boolean getActive() {
            return isActive;
        }

        public void setActive(Boolean active) {
            isActive = active;
        }

        public Boolean getDefault() {
            return isDefault;
        }

        public void setDefault(Boolean aDefault) {
            isDefault = aDefault;
        }

        public DateTime getUploadedFileDate() {
            return uploadedFileDate;
        }

        public void setUploadedFileDate(DateTime uploadedFileDate) {
            this.uploadedFileDate = uploadedFileDate;
        }

        public DateTime getUploadedTestFileDate() {
            return uploadedTestFileDate;
        }

        public void setUploadedTestFileDate(DateTime uploadedTestFileDate) {
            this.uploadedTestFileDate = uploadedTestFileDate;
        }

        public String getFileHandlerAppKey() {
            return fileHandlerAppKey;
        }

        public void setFileHandlerAppKey(String fileHandlerAppKey) {
            this.fileHandlerAppKey = fileHandlerAppKey;
        }

        public String getTestFileHandlerAppKey() {
            return testFileHandlerAppKey;
        }

        public void setTestFileHandlerAppKey(String testFileHandlerAppKey) {
            this.testFileHandlerAppKey = testFileHandlerAppKey;
        }

        public Long getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(Long organizationId) {
            this.organizationId = organizationId;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public OSEnvironment getOsEnvironment() {
            return osEnvironment;
        }

        public void setOsEnvironment(OSEnvironment osEnvironment) {
            this.osEnvironment = osEnvironment;
        }

        public Organization getOrganization() {
            return organization;
        }

        public void setOrganization(Organization organization) {
            this.organization = organization;
        }

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public String getFileHandlerAppKey32Bit() {
            return fileHandlerAppKey32Bit;
        }

        public void setFileHandlerAppKey32Bit(String fileHandlerAppKey32Bit) {
            this.fileHandlerAppKey32Bit = fileHandlerAppKey32Bit;
        }

        public String getTestFileHandlerAppKey32Bit() {
            return testFileHandlerAppKey32Bit;
        }

        public void setTestFileHandlerAppKey32Bit(String testFileHandlerAppKey32Bit) {
            this.testFileHandlerAppKey32Bit = testFileHandlerAppKey32Bit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HandlerAppSearchResultModel that = (HandlerAppSearchResultModel) o;

            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }


    public Long count(HandlerAppCriteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(handlerApp.id) ", "from HandlerApp handlerApp inner join handlerApp.parentOS ent inner join ent.osType osType ");
        builder.addClause("and handlerApp member of ent.handlerApps");
        builder.addClause("and ent.disabled = :osDisabled_", "osDisabled_", false);
        builder.addClause("and osType.disabled = :osTypeDisabled_", "osTypeDisabled_", false);
        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }


    public List<HandlerAppSearchResultModel> list(HandlerAppCriteria criteria, int first, int count, String sortProp, boolean isAscending, Session session) {

        HQLBuilder builder = new HQLBuilder(session, "select handlerApp ,ent", "from HandlerApp handlerApp inner join handlerApp.parentOS ent ");

        if (criteria != null)
            applyCriteria(builder, criteria);

        if (sortProp != null) {
            builder.addOrder(sortProp, isAscending);
        } else {
            builder.addOrder("handlerApp.id", true);
        }

        Query query = builder.createQuery();
        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);

        List<Object[]> resultObjects = query.list();

        Stream<HandlerAppSearchResultModel> searchResultModelStream =
                resultObjects.stream().map(new Function<Object[], HandlerAppSearchResultModel>() {
                    @Override
                    public HandlerAppSearchResultModel apply(Object[] objects) {
                        HandlerApp handlerApp = (HandlerApp) objects[0];
                        OS os = (OS) objects[1];
                        if (os != null && !os.getDisabled()) {
                            OSType osType = os.getOsType();
                            if (osType != null && !osType.getDisabled()) {
                                HandlerAppSearchResultModel searchResultModel = setHandlerAppInfo(handlerApp, os);
                                return searchResultModel;
                            }
                        }

                        return null;

                    }
                });

        List<HandlerAppSearchResultModel> tmpSearchResultModelList =
                searchResultModelStream.collect(Collectors.<HandlerAppSearchResultModel>toList());

//        List<HandlerAppSearchResultModel> searchResultModelList = new ArrayList<>();
//
//        return searchResultModelList;
        return tmpSearchResultModelList;

    }

    private HandlerAppSearchResultModel setHandlerAppInfo(HandlerApp handlerApp, OS os) {
        HandlerAppSearchResultModel searchResultModel = new HandlerAppSearchResultModel();

        searchResultModel.id = handlerApp.getId();
        if (handlerApp.getHandlerFile() != null && handlerApp.getHandlerFile().getFilePath() != null) {
            searchResultModel.uploadedFileDate = handlerApp.getUploadedFileDate();
            searchResultModel.fileHandlerAppKey = handlerApp.getHandlerFile().getFilePath();
        }

        if (handlerApp.getHandlerFile32bit() != null && handlerApp.getHandlerFile32bit().getFilePath() != null) {
            searchResultModel.uploadedFileDate = handlerApp.getUploadedFileDate();
            searchResultModel.fileHandlerAppKey32Bit = handlerApp.getHandlerFile32bit().getFilePath();
        }

        if (handlerApp.getTestHandlerFile() != null && handlerApp.getTestHandlerFile().getFilePath() != null) {
            searchResultModel.uploadedTestFileDate = handlerApp.getUploadedTestFileDate();
            searchResultModel.testFileHandlerAppKey = handlerApp.getTestHandlerFile().getFilePath();
        }
        if (handlerApp.getTestHandlerFile32bit() != null && handlerApp.getTestHandlerFile32bit().getFilePath() != null) {
            searchResultModel.uploadedTestFileDate = handlerApp.getUploadedTestFileDate();
            searchResultModel.testFileHandlerAppKey32Bit = handlerApp.getTestHandlerFile32bit().getFilePath();
        }
        searchResultModel.versionCode = handlerApp.getVersionCode();
        searchResultModel.isActive = handlerApp.getActive();
        searchResultModel.isDefault = handlerApp.getDefaultForOrganization();
        searchResultModel.osId = os.getId();
        searchResultModel.osName = os.getOsName();
        searchResultModel.nickName = handlerApp.getOrganization() != null ? handlerApp.getOrganization().getNickName() : null;
        searchResultModel.osEnvironment = handlerApp.getOsEnvironment() != null ? handlerApp.getOsEnvironment() : null;
        searchResultModel.organizationId = handlerApp.getOrganization() != null ? handlerApp.getOrganization().getId() : null;
        return searchResultModel;
    }


    private void applyCriteria(HQLBuilder builder, HandlerAppCriteria criteria) {
        if (criteria.id != null)
            builder.addClause("and handlerApp.id = :id_", "id_", criteria.id);
        if (criteria.versionCode != null)
            builder.addClause("and handlerApp.versionCode = :versionCode_", "versionCode_", criteria.versionCode);
        if (criteria.isActive != null)
            builder.addClause("and handlerApp.isActive = :isActive_", "isActive_", criteria.isActive);
        if (criteria.isDefault != null)
            builder.addClause("and handlerApp.isDefaultForOrganization = :isDefault_", "isDefault_", criteria.isDefault);

        if (criteria.osIds != null && !criteria.osIds.isEmpty())
            builder.addClause("and ent.id in (:osIds_)", "osIds_", criteria.osIds);

        if (criteria.organization != null)
            builder.addClause("and handlerApp.organization in (:organization_)", "organization_", criteria.organization);

        if (criteria.osEnvironment != null)
            builder.addClause("and handlerApp.osEnvironment in (:environment_)", "environment_", criteria.osEnvironment);
    }


    public List<HandlerApp> getAndSetHandlerAppList(Session newSession, HandlerApp handlerApp, OS os) {

        OS loadedOs = (os != null && os.getId() != null) ? (OS) newSession.load(OS.class, os.getId()) : new OS();
        loadedOs.getHandlerApps();
        List<HandlerApp> handlerApps = loadedOs.getHandlerApps();
        handlerApps.add(handlerApp);
        return handlerApps;
    }

    public HandlerApp saveHandlerAppToDataBase(Session newSession, HandlerAppVO handlerAppVO, OS os) {

        try {
            File handlerFile = new File();
            handlerFile.setStereoType(StereoType.LAUNCHER_FILE);
            handlerFile.setFileName("launcherFileName");
            handlerFile.setFilePath(handlerAppVO.getFileHandlerAppKey());
            newSession.saveOrUpdate(handlerFile);

            HandlerApp handlerApp = new HandlerApp();

            File testHandlerFile = new File();
            testHandlerFile.setStereoType(StereoType.LAUNCHER_FILE);
            testHandlerFile.setFileName("launcherTestFileName");
            testHandlerFile.setFilePath(handlerAppVO.getTestFileHandlerAppKey());
            newSession.saveOrUpdate(testHandlerFile);

            handlerApp.setVersionCode(handlerAppVO.getVersionCode());
            handlerApp.setUploadedFileDate(DateTime.now());
            handlerApp.setHandlerFile(handlerFile);

            handlerApp.setUploadedTestFileDate(DateTime.now());
            handlerApp.setTestHandlerFile(testHandlerFile);
            handlerApp.setActive(handlerAppVO.getActive() == null ? false : true);
            OSEnvironment environment = (OSEnvironment) newSession.get(OSEnvironment.class, handlerAppVO.getEnvironmentId());
            handlerApp.setOsEnvironment(environment);
            Organization organization = (Organization) newSession.get(Organization.class, handlerAppVO.getOrgId());
            handlerApp.setOrganization(organization);
            handlerApp.setParentOS(os);

            if (handlerAppVO.getDefault()) {
                HandlerAppService.HandlerAppCriteria handlerAppCriteria = new HandlerAppService.HandlerAppCriteria();
                handlerAppCriteria.setOrganization(handlerApp.getOrganization());
                handlerAppCriteria.setDefault(handlerAppVO.getDefault());
                List<HandlerAppService.HandlerAppSearchResultModel> handlerAppSearchResultModelList = HandlerAppService.Instance.list(handlerAppCriteria, 0, -1, null, false, newSession);
                for (HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel : handlerAppSearchResultModelList) {
                    HandlerApp handlerAppInList = (HandlerApp) newSession.load(HandlerApp.class, handlerAppSearchResultModel.getId());
                    if (!handlerAppInList.getId().equals(handlerApp.getId())) {
                        handlerAppInList.setDefaultForOrganization(false);
                        HandlerAppService.Instance.saveOrUpdate(handlerAppInList, newSession);
                    }
                }
            }
            saveOrUpdate(handlerApp, newSession);
            return handlerApp;
        } catch (Exception e) {
            return null;
        }
    }

    public void getAndSetHandlerAppList2(Session newSession, Long versionCode, boolean active, String fileKey, String testFileKey, OS os) {
        if (fileKey != null && !fileKey.trim().equals("")) {
            File handlerFile = new File();
            handlerFile.setStereoType(StereoType.LAUNCHER_FILE);
            handlerFile.setFileName("launcherFileName");
            int ix = fileKey.indexOf("key");
            fileKey = fileKey.substring(ix + 4);
            handlerFile.setFilePath(fileKey);
            newSession.saveOrUpdate(handlerFile);

            File testHandlerFile = new File();
            testHandlerFile.setStereoType(StereoType.LAUNCHER_FILE);
            testHandlerFile.setFileName("launcherTestFileName");
            testHandlerFile.setFilePath(testFileKey);
            newSession.saveOrUpdate(testHandlerFile);

            HandlerApp handlerApp = new HandlerApp();
            handlerApp.setVersionCode(versionCode);
            handlerApp.setUploadedFileDate(DateTime.now());
            handlerApp.setHandlerFile(handlerFile);

            handlerApp.setUploadedTestFileDate(DateTime.now());
            handlerApp.setTestHandlerFile(testHandlerFile);

            handlerApp.setUploadedFileDate(DateTime.now());
            handlerApp.setHandlerFile(handlerFile);
            handlerApp.setActive(active);
            handlerApp.setDefaultForOrganization(false);
            handlerApp.setParentOS(os);
            saveOrUpdate(handlerApp, newSession);
            OS loadedOs = (os != null && os.getId() != null) ? (OS) newSession.load(OS.class, os.getId()) : new OS();
            List<HandlerApp> handlerApps = new ArrayList<>();
            handlerApps.add(handlerApp);
            os.setHandlerApps(handlerApps);
        }
        newSession.saveOrUpdate(os);

    }

    public ResponseVO checkInputHandlerApp(HandlerAppVO handlerAppVO) {
        ResponseVO responseVO = new ResponseVO();

        if (handlerAppVO != null && handlerAppVO.getActive() != null && handlerAppVO.getOrgId() != null && handlerAppVO.getEnvironmentId() != null && handlerAppVO.getFileHandlerAppKey() != null &&
                !handlerAppVO.getFileHandlerAppKey().trim().equals("") && handlerAppVO.getVersionCode() != null) {
            responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        } else {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
        }
        return responseVO;
    }


    public List<HandlerAppSearchResultModel> getDefaultHandlerApp(Organization organization, Session session) {
        HandlerAppService.HandlerAppCriteria handlerAppCriteria = new HandlerAppService.HandlerAppCriteria();
        handlerAppCriteria.setOrganization(organization);
        handlerAppCriteria.setDefault(true);
        HQLBuilder builder = new HQLBuilder(session, "select handlerApp ,ent", "from HandlerApp handlerApp inner join handlerApp.parentOS ent ");

        applyCriteria(builder, handlerAppCriteria);
        builder.addOrder("handlerApp.id", true);


        Query query = builder.createQuery();
        query.setFirstResult(0);

        List<Object[]> resultObjects = query.list();

        Stream<HandlerAppSearchResultModel> searchResultModelStream =
                resultObjects.stream().map(new Function<Object[], HandlerAppSearchResultModel>() {
                    @Override
                    public HandlerAppSearchResultModel apply(Object[] objects) {
                        HandlerApp handlerApp = (HandlerApp) objects[0];
                        OS os = (OS) objects[1];
                        OSType osType = os.getOsType();
                        HandlerAppSearchResultModel searchResultModel = setHandlerAppInfo(handlerApp, os);
                        return searchResultModel;
                    }
                });

        List<HandlerAppSearchResultModel> tmpSearchResultModelList =
                searchResultModelStream.collect(Collectors.<HandlerAppSearchResultModel>toList());

        return tmpSearchResultModelList;
    }

    public HandlerAppCriteria getHandlerAppCriteria(HandlerAppVO handlerAppVO, Session session) {
        HandlerAppCriteria criteria = new HandlerAppCriteria();
        if (handlerAppVO != null) {
            boolean hasValue = false;
            OS os = null;
            OSEnvironment osEnvironment = null;
            Organization organization = null;
            if (handlerAppVO.getOsId() != null) {
                os = (OS) session.get(OS.class, handlerAppVO.getOsId());
            }

            if (handlerAppVO.getEnvironmentId() != null) {
                osEnvironment = (OSEnvironment) session.get(OSEnvironment.class, handlerAppVO.getEnvironmentId());
            } else {
                osEnvironment = EnvironmentService.Instance.getDefaultEnvironment();
            }
            if (handlerAppVO.getOrgId() != null) {
                organization = (Organization) session.get(Organization.class, handlerAppVO.getOrgId());
            } else {
                Organization defaultOrganization = OrgService.Instance.getDefaultOrganization(session);
                if (defaultOrganization != null) {
                    organization = defaultOrganization;
                }
            }

            if (os == null && osEnvironment == null && organization == null) {
                return null;
            }
            if (handlerAppVO.getActive() != null) {
                criteria.setActive(handlerAppVO.getActive());
                hasValue = true;
            }
            criteria.setVersionCode(handlerAppVO.getVersionCode());
            if (os != null) {
                List<Long> osIds = new ArrayList<>();
                osIds.add(handlerAppVO.getOsId());
                criteria.setOsIds(osIds);
                hasValue = true;
            }
            if (osEnvironment != null) {
                criteria.setOsEnvironment(osEnvironment);
                hasValue = true;
            }
            if (organization != null) {
                criteria.setOrganization(organization);
                hasValue = true;
            }
            if (!hasValue) {
                return null;

            }
        }
        return criteria;
    }


}
