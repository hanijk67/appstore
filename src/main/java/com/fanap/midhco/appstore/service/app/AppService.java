package com.fanap.midhco.appstore.service.app;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DayTime;
import com.fanap.midhco.appstore.restControllers.vos.AppUploadVO;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.ui.access.PrincipalUtil;
import io.searchbox.client.JestResult;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by admin123 on 6/29/2016.
 */
public class AppService {
    public static Logger logger = Logger.getLogger(AppService.class);

    public static AppService Instance = new AppService();

    private AppService() {
    }

    public ResponseVO checkValidData(AppUploadVO appUploadVO, Session session) {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
        responseVO.setResult(ResultStatus.INVALID_DATA.toString());
        User currentUser = PrincipalUtil.getCurrentUser();

        try {
            if (appUploadVO != null) {
                if (appUploadVO.getId() == null) {
                    if (appUploadVO.getCategoryId() == null || appUploadVO.getShortDescription() == null || appUploadVO.getShortDescription().trim().equals("") ||
                            appUploadVO.getDescription() == null || appUploadVO.getDescription().trim().equals("") || appUploadVO.getOsId() == null || appUploadVO.getTitle() == null ||
                            appUploadVO.getPackageName() != null && appUploadVO.getPackageName().trim().equals("") || appUploadVO.getAppPackageVO() == null ||
                            appUploadVO.getAppPackageVO().getIconFileName() == null || appUploadVO.getAppPackageVO().getPackageFileName().trim().equals("") ||
                            appUploadVO.getAppPackageVO().getPackageFileName() == null || appUploadVO.getAppPackageVO().getPackageFileName().trim().equals("") ||
                            appUploadVO.getAppPackageVO().getPackageFileKey() == null) {
                        return responseVO;
                    }
                }

                if (appUploadVO.getCategoryId() != null) {
                    AppCategory loadedCategory = (AppCategory) session.get(AppCategory.class, appUploadVO.getCategoryId());
                    if (loadedCategory == null) {
                        return responseVO;
                    } else {
                        if (!loadedCategory.getAssignable()) {
                            return responseVO;
                        }
                    }
                }
                if (appUploadVO.getDeveloperUserId() != null) {
                    User developerUser = (User) session.get(User.class, appUploadVO.getDeveloperUserId());
                    if (developerUser == null) {
                        return responseVO;
                    }
                    if (appUploadVO.getId() != null) {
                        App loadedApp = (App) session.load(App.class, appUploadVO.getId());
                        if (!loadedApp.getDeveloper().equals(developerUser)) {
                            if (!UserService.Instance.isUserRoot(currentUser)) {
                                return responseVO;
                            }
                        }
                    }
                }

                if (appUploadVO.getPublishState() != null && !appUploadVO.getPublishState().trim().equals("")) {
                    List<PublishState> publishStateList = PublishState.listAll();
                    List<String> publishStateListByteValue = new ArrayList<>();
                    for (PublishState publishStateInList : publishStateList) {
                        publishStateListByteValue.add(String.valueOf(publishStateInList.getState()));
                    }
                    if (!publishStateListByteValue.contains(appUploadVO.getPublishState())) {
                        return responseVO;
                    }
                }
                String appPackageName = null;
                if (appUploadVO.getOsId() != null && appUploadVO.getAppPackageVO() != null && appUploadVO.getAppPackageVO().getPackageFileKey() != null &&
                        !appUploadVO.getAppPackageVO().getPackageFileKey().trim().equals("")) {
                    OS os = (OS) session.get(OS.class, Long.valueOf(appUploadVO.getOsId()));
                    if (os == null) {
                        return responseVO;
                    }
                    OSType osType = os.getOsType();
                    IAPPPackageService iappPackageService = AppPackageService.Instance.processPackageFile(appUploadVO.getAppPackageVO().getPackageFileKey(), osType);
                    appPackageName = iappPackageService.getPackage();

                    if (!appPackageName.equals(appUploadVO.getPackageName())) {
                        responseVO.setResult("package name should be equal to name of package from package file Key");
                        return responseVO;
                    }
                    responseVO.setResult(appPackageName);
                }
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }

        return responseVO;
    }

    public ResponseVO checkValidDataForAddOrReplacePackage(AppUploadVO appUploadVO, Session session, boolean forReplace, Boolean isMainPackage, boolean usePreviousFile) {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
        responseVO.setResult(ResultStatus.INVALID_DATA.toString());

        try {
            if (appUploadVO != null) {
                if (appUploadVO.getOsId() == null || appUploadVO.getPackageName() != null && appUploadVO.getPackageName().trim().equals("") || appUploadVO.getAppPackageVO() == null ||
                        appUploadVO.getAppPackageVO().getPackageFileName() == null || appUploadVO.getAppPackageVO().getPackageFileName().trim().equals("") ||
                        appUploadVO.getAppPackageVO().getPackageFileKey() == null) {
                    return responseVO;
                }
                if (!(forReplace || usePreviousFile) && (appUploadVO.getAppPackageVO().getIconFileName() == null || appUploadVO.getAppPackageVO().getIconFileName().trim().equals("") ||
                        appUploadVO.getAppPackageVO().getPackageImagesKeys() == null || appUploadVO.getAppPackageVO().getPackageImagesKeys().isEmpty())) {
                    return responseVO;
                }

                if (forReplace) {
                    if (!isMainPackage) {
                        if (appUploadVO.getAppPackageVO().getVersionCode() == null || appUploadVO.getAppPackageVO().getVersionCode().trim().equals("") ||
                                appUploadVO.getAppPackageVO().getVersionName() == null || appUploadVO.getAppPackageVO().getVersionName().trim().equals("")) {
                            return responseVO;
                        }
                    }
                }
                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

                return checkPackageInformation(appUploadVO, session, responseVO);

            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }

        return responseVO;
    }

    private ResponseVO checkPackageInformation(AppUploadVO appUploadVO, Session session, ResponseVO responseVO) {
        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
        responseVO.setResult(ResultStatus.INVALID_DATA.toString());
        try {
            String appPackageName = null;
            if (appUploadVO.getOsId() != null && appUploadVO.getAppPackageVO() != null && appUploadVO.getAppPackageVO().getPackageFileKey() != null &&
                    !appUploadVO.getAppPackageVO().getPackageFileKey().trim().equals("")) {
                OS os = (OS) session.get(OS.class, Long.valueOf(appUploadVO.getOsId()));
                if (os == null) {
                    return responseVO;
                }
                OSType osType = os.getOsType();
                IAPPPackageService iappPackageService = AppPackageService.Instance.processPackageFile(appUploadVO.getAppPackageVO().getPackageFileKey(), osType);
                appPackageName = iappPackageService.getPackage();

                if (!appPackageName.equals(appUploadVO.getPackageName())) {
                    responseVO.setResult("package name should be equal to name of package from package file Key");
                    return responseVO;
                }
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                responseVO.setResult(appPackageName);
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }


    public static class AppSearchCriteria implements Serializable {
        public Long id;
        public String packageName;
        public String title;
        public Collection<User> developers;
        public List<String> developerName;
        public String versionName;
        public String versionCode;
        public DateTime[] creationDateTime;
        public DateTime[] mainPackageModificationDate;
        public List<String> creatorUserName;
        public Collection<User> creatorUsers;
        public Collection<OS> os;
        public List<String> osName;
        public Collection<PublishState> publishStates;
        public Collection<OSType> osType;
        public Collection<AppCategory> appCategory;
        public List<String> appCategoryName;
        public List<Long> appCategoryId;
        public List<String> packageNames;
        public Boolean getNewApp;
        public Boolean getUpdatedApp;
        public Boolean isDeleted;
        public Boolean ignoreDeveloperInSearch;
        public List<String> keyword;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public Collection<User> getDevelopers() {
            return developers;
        }

        public void setDevelopers(Collection<User> developers) {
            this.developers = developers;
        }

        public List<String> getDeveloperName() {
            return developerName;
        }

        public void setDeveloperName(List<String> developerName) {
            this.developerName = developerName;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(String versionCode) {
            this.versionCode = versionCode;
        }

        public DateTime[] getCreationDateTime() {
            return creationDateTime;
        }

        public void setCreationDateTime(DateTime[] creationDateTime) {
            this.creationDateTime = creationDateTime;
        }

        public DateTime[] getMainPackageModificationDate() {
            return mainPackageModificationDate;
        }

        public void setMainPackageModificationDate(DateTime[] mainPackageModificationDate) {
            this.mainPackageModificationDate = mainPackageModificationDate;
        }

        public List<String> getCreatorUserName() {
            return creatorUserName;
        }

        public void setCreatorUserName(List<String> creatorUserName) {
            this.creatorUserName = creatorUserName;
        }

        public Collection<User> getCreatorUsers() {
            return creatorUsers;
        }

        public void setCreatorUsers(Collection<User> creatorUsers) {
            this.creatorUsers = creatorUsers;
        }

        public Collection<OS> getOs() {
            return os;
        }

        public void setOs(Collection<OS> os) {
            this.os = os;
        }

        public List<String> getOsName() {
            return osName;
        }

        public void setOsName(List<String> osName) {
            this.osName = osName;
        }

        public Collection<PublishState> getPublishStates() {
            return publishStates;
        }

        public void setPublishStates(Collection<PublishState> publishStates) {
            this.publishStates = publishStates;
        }

        public Collection<OSType> getOsType() {
            return osType;
        }

        public void setOsType(Collection<OSType> osType) {
            this.osType = osType;
        }

        public Collection<AppCategory> getAppCategory() {
            return appCategory;
        }

        public void setAppCategory(Collection<AppCategory> appCategory) {
            this.appCategory = appCategory;
        }

        public List<String> getAppCategoryName() {
            return appCategoryName;
        }

        public void setAppCategoryName(List<String> appCategoryName) {
            this.appCategoryName = appCategoryName;
        }

        public List<Long> getAppCategoryId() {
            return appCategoryId;
        }

        public void setAppCategoryId(List<Long> appCategoryId) {
            this.appCategoryId = appCategoryId;
        }

        public List<String> getPackageNames() {
            return packageNames;
        }

        public void setPackageNames(List<String> packageNames) {
            this.packageNames = packageNames;
        }

        public Boolean getGetNewApp() {
            return getNewApp;
        }

        public void setGetNewApp(Boolean getNewApp) {
            this.getNewApp = getNewApp;
        }

        public Boolean getGetUpdatedApp() {
            return getUpdatedApp;
        }

        public void setGetUpdatedApp(Boolean getUpdatedApp) {
            this.getUpdatedApp = getUpdatedApp;
        }

        public List<String> getKeyword() {
            return keyword;
        }

        public void setKeyword(List<String> keyword) {
            this.keyword = keyword;
        }

        public Boolean getDeleted() {
            return isDeleted;
        }

        public void setDeleted(Boolean deleted) {
            isDeleted = deleted;
        }

        public Boolean getIgnoreDeveloperInSearch() {
            return ignoreDeveloperInSearch;
        }

        public void setIgnoreDeveloperInSearch(Boolean ignoreDeveloperInSearch) {
            this.ignoreDeveloperInSearch = ignoreDeveloperInSearch;
        }
    }

    private void applyCriteria(HQLBuilder builder, AppSearchCriteria criteria) throws Exception {
        if (criteria.id != null)
            builder.addClause("and app.id = :id_", "id_", criteria.id);
        if (criteria.packageName != null && !criteria.packageName.trim().isEmpty())
            builder.addClause("and app.packageName=:packName_", "packName_", criteria.packageName);
        if (criteria.title != null && !criteria.title.isEmpty())
            builder.addClause("and app.title like (:title_)", "title_", HQLBuilder.like(criteria.title));
        if (criteria.developers != null && !criteria.developers.isEmpty())
            builder.addClause("and app.developer in (:developers_)", "developers_", criteria.developers);
        if (criteria.versionName != null && !criteria.versionName.isEmpty())
            builder.addClause("and mainPackage.versionName like :versionName_", "versionName_", HQLBuilder.like(criteria.versionName));
        if (criteria.versionCode != null && !criteria.versionCode.isEmpty())
            builder.addClause("and mainPackage.versionCode like :versionCode_", "versionCode_", HQLBuilder.like(criteria.versionCode));
        if (criteria.creationDateTime != null)
            builder.addDateTimeRange("app", "creationDate", "uAppCreationDate", "lAppCreationDate", criteria.creationDateTime);
        if (criteria.mainPackageModificationDate != null)
            builder.addDateTimeRange("app", "mainPackageModificationDate", "uAppModifyMainPackDate", "lAppModifyMainPackDate", criteria.mainPackageModificationDate);
        if (criteria.getNewApp != null && criteria.getNewApp)
            builder.addClause("and app.mainPackageModificationDate is null");
        if (criteria.getUpdatedApp != null && criteria.getUpdatedApp)
            builder.addClause("and app.mainPackageModificationDate is not null");
        if (criteria.creatorUsers != null && !criteria.creatorUsers.isEmpty())
            builder.addClause("and app.creatorUser = :creatorUser_", "creatorUser_", criteria.creatorUsers);
        if (criteria.os != null && !criteria.os.isEmpty())
            builder.addClause("and app.os in (:os_)", "os_", criteria.os);
        if (criteria.publishStates != null && !criteria.publishStates.isEmpty())
            builder.addClause("and mainPackage.publishState in (:pubStates_)", "pubStates_", criteria.publishStates);
        if (criteria.osType != null && !criteria.osType.isEmpty())
            builder.addClause("and app.osType in (:osTypes_)", "osTypes_", criteria.osType);
        if (criteria.appCategory != null && !criteria.appCategory.isEmpty())
            builder.addClause("and app.appCategory in (:appCategory_)", "appCategory_", criteria.appCategory);

        if (criteria.isDeleted != null && criteria.isDeleted) {
            builder.addClause("and app.isDeleted = :isDeleted_", "isDeleted_", criteria.isDeleted);
        } else {
            builder.addClause(" and (app.isDeleted is null or app.isDeleted = :isDeleted_ )", "isDeleted_", false);
        }

        if (criteria.creatorUserName != null && !criteria.creatorUserName.isEmpty()) {
            builder.addClause("and (");
            int counter = 0;
            for (String creatorUserName : criteria.creatorUserName) {
                builder.addClause("(app.creatorUser.userName like (:creatorUserName" + counter + "_)) ", "creatorUserName" + counter + "_", HQLBuilder.like(creatorUserName));
                counter++;
                if (counter < criteria.creatorUserName.size())
                    builder.addClause(" or ");
            }

            builder.addClause(")");
        }

        if (criteria.developerName != null && !criteria.developerName.isEmpty()) {
            builder.addClause("and (");
            int counter = 0;
            for (String developerUserName : criteria.developerName) {
                builder.addClause("(app.developer.userName like (:developerUserName" + counter + "_)) ", "developerUserName" + counter + "_", HQLBuilder.like(developerUserName));
                counter++;
                if (counter < criteria.developerName.size())
                    builder.addClause(" or ");
            }

            builder.addClause(")");
        }

        if (criteria.osName != null && !criteria.osName.isEmpty()) {
            builder.addClause("and (");
            int counter = 0;
            for (String osName : criteria.osName) {
                builder.addClause("(app.os.osName like (:osName" + counter + "_)) ", "osName" + counter + "_", HQLBuilder.like(osName));
                counter++;
                if (counter < criteria.osName.size())
                    builder.addClause(" or ");
            }

            builder.addClause(")");
        }

        if (criteria.appCategoryName != null && !criteria.appCategoryName.isEmpty()) {
            builder.addClause("and (");
            int counter = 0;
            for (String categoryName : criteria.appCategoryName) {
                builder.addClause("(app.appCategory.categoryName like (:appCategory" + counter + "_)) ", "appCategory" + counter + "_", HQLBuilder.like(categoryName));
                counter++;
                if (counter < criteria.appCategoryName.size())
                    builder.addClause(" or ");
            }

            builder.addClause(")");
        }


        if (criteria.packageNames != null && !criteria.packageNames.isEmpty())
            builder.addClause("and app.packageName in (:packageNames_)", "packageNames_", criteria.packageNames);

        if (criteria.appCategoryId != null && !criteria.appCategoryId.isEmpty())
            builder.addClause("and app.appCategory.id in (:appCategoryIds_)", "appCategoryIds_", criteria.appCategoryId);


        if (criteria.keyword != null && !criteria.keyword.isEmpty()) {
            List<Long> appIdList = new ArrayList<>();

            AppElasticService.AppKeyWordCriteria appKeyWordCriteria = new AppElasticService.AppKeyWordCriteria();
            appKeyWordCriteria.keyWordList = (criteria.keyword);

            List<AppElasticService.AppKeyWordVO> keyWordVOs = AppElasticService.Instance.listAppKeyWord(appKeyWordCriteria, 0, 1000);
            if (keyWordVOs != null && !keyWordVOs.isEmpty()) {
                for (AppElasticService.AppKeyWordVO keyWordVO : keyWordVOs) {
                    appIdList.add(keyWordVO.getAppId());
                }
                builder.addClause("and app.id in (:appIds_)", "appIds_", appIdList);
            } else {
                builder.addClause("and app.id = :appIds_", "appIds_", -1l);
            }

        }
    }

    public static class AppSearchResultModel implements Serializable {
        Long appId;
        String packageName;
        String title;
        String shortDescription;
        String osName;
        PublishState publishState;
        OSType osType;
        String developerName;
        AppCategory appCategory;

        String app_mainPack_versionCode;
        String app_mainPack_versionName;
        String app_mainPack_fileName;
        String app_mainPack_filePath;
        Long app_mainPack_fileSize;
        String app_mainPack_iconFile;
        String app_mainPack_iconPath;
        String app_mainPack_minSDK;
        String app_mainPack_targetSDK;
        String app_mainPack_changeLog;
        Long app_mainPack_id;

        String app_Pack_versionCode;
        String app_Pack_versionName;
        String app_Pack_fileName;
        String app_Pack_filePath;
        String app_Pack_fileSize;
        String app_Pack_iconFile;
        String app_Pack_iconPath;
        String app_Pack_minSDK;
        String app_Pack_targetSDK;


        String thumbNailFilePath;
        DateTime creationDateTime;
        DateTime lastModifyDate;
        User creatorUser;
        Long osId;
        Long mainPackageFileSize;
        List<String> permissionDetailList;

        Boolean isDeleted;
        public List<String> keyword;


        public String getApp_mainPack_filePath() {
            return app_mainPack_filePath;
        }

        public void setApp_mainPack_filePath(String app_mainPack_filePath) {
            this.app_mainPack_filePath = app_mainPack_filePath;
        }

        public String getApp_mainPack_iconPath() {
            return app_mainPack_iconPath;
        }

        public void setApp_mainPack_iconPath(String app_mainPack_iconPath) {
            this.app_mainPack_iconPath = app_mainPack_iconPath;
        }

        public Long getAppId() {
            return appId;
        }

        public void setAppId(Long appId) {
            this.appId = appId;
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

        public String getShortDescription() {
            return shortDescription;
        }

        public void setShortDescription(String shortDescription) {
            this.shortDescription = shortDescription;
        }

        public String getApp_mainPack_versionCode() {
            return app_mainPack_versionCode;
        }

        public void setApp_mainPack_versionCode(String app_mainPack_versionCode) {
            this.app_mainPack_versionCode = app_mainPack_versionCode;
        }

        public String getApp_mainPack_versionName() {
            return app_mainPack_versionName;
        }

        public void setApp_mainPack_versionName(String app_mainPack_versionName) {
            this.app_mainPack_versionName = app_mainPack_versionName;
        }

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public PublishState getPublishState() {
            return publishState;
        }

        public void setPublishState(PublishState publishState) {
            this.publishState = publishState;
        }

        public OSType getOsType() {
            return osType;
        }

        public void setOsType(OSType osType) {
            this.osType = osType;
        }

        public AppCategory getAppCategory() {
            return appCategory;
        }

        public void setAppCategory(AppCategory appCategory) {
            this.appCategory = appCategory;
        }

        public String getDeveloperName() {
            return developerName;
        }

        public void setDeveloperName(String developerName) {
            this.developerName = developerName;
        }

        public String getApp_mainPack_fileName() {
            return app_mainPack_fileName;
        }

        public void setApp_mainPack_fileName(String app_mainPack_fileName) {
            this.app_mainPack_fileName = app_mainPack_fileName;
        }

        public Long getApp_mainPack_fileSize() {
            return app_mainPack_fileSize;
        }

        public void setApp_mainPack_fileSize(Long app_mainPack_fileSize) {
            this.app_mainPack_fileSize = app_mainPack_fileSize;
        }

        public String getApp_mainPack_iconFile() {
            return app_mainPack_iconFile;
        }

        public void setApp_mainPack_iconFile(String app_mainPack_iconFile) {
            this.app_mainPack_iconFile = app_mainPack_iconFile;
        }

        public String getApp_mainPack_minSDK() {
            return app_mainPack_minSDK;
        }

        public void setApp_mainPack_minSDK(String app_mainPack_minSDK) {
            this.app_mainPack_minSDK = app_mainPack_minSDK;
        }

        public String getApp_mainPack_targetSDK() {
            return app_mainPack_targetSDK;
        }

        public void setApp_mainPack_targetSDK(String app_mainPack_targetSDK) {
            this.app_mainPack_targetSDK = app_mainPack_targetSDK;
        }

        public String getThumbNailFilePath() {
            return thumbNailFilePath;
        }

        public void setThumbNailFilePath(String thumbNailFilePath) {
            this.thumbNailFilePath = thumbNailFilePath;
        }

        public DateTime getCreationDateTime() {
            return creationDateTime;
        }

        public void setCreationDateTime(DateTime creationDateTime) {
            this.creationDateTime = creationDateTime;
        }

        public User getCreatorUser() {
            return creatorUser;
        }

        public void setCreatorUser(User creatorUser) {
            this.creatorUser = creatorUser;
        }

        public Long getOsId() {
            return osId;
        }

        public void setOsId(Long osId) {
            this.osId = osId;
        }

        public String getApp_Pack_versionCode() {
            return app_Pack_versionCode;
        }

        public void setApp_Pack_versionCode(String app_Pack_versionCode) {
            this.app_Pack_versionCode = app_Pack_versionCode;
        }

        public String getApp_Pack_versionName() {
            return app_Pack_versionName;
        }

        public void setApp_Pack_versionName(String app_Pack_versionName) {
            this.app_Pack_versionName = app_Pack_versionName;
        }

        public String getApp_Pack_fileName() {
            return app_Pack_fileName;
        }

        public void setApp_Pack_fileName(String app_Pack_fileName) {
            this.app_Pack_fileName = app_Pack_fileName;
        }

        public String getApp_Pack_filePath() {
            return app_Pack_filePath;
        }

        public void setApp_Pack_filePath(String app_Pack_filePath) {
            this.app_Pack_filePath = app_Pack_filePath;
        }

        public String getApp_Pack_fileSize() {
            return app_Pack_fileSize;
        }

        public void setApp_Pack_fileSize(String app_Pack_fileSize) {
            this.app_Pack_fileSize = app_Pack_fileSize;
        }

        public String getApp_Pack_iconFile() {
            return app_Pack_iconFile;
        }

        public void setApp_Pack_iconFile(String app_Pack_iconFile) {
            this.app_Pack_iconFile = app_Pack_iconFile;
        }

        public String getApp_Pack_iconPath() {
            return app_Pack_iconPath;
        }

        public void setApp_Pack_iconPath(String app_Pack_iconPath) {
            this.app_Pack_iconPath = app_Pack_iconPath;
        }

        public String getApp_Pack_minSDK() {
            return app_Pack_minSDK;
        }

        public void setApp_Pack_minSDK(String app_Pack_minSDK) {
            this.app_Pack_minSDK = app_Pack_minSDK;
        }

        public String getApp_Pack_targetSDK() {
            return app_Pack_targetSDK;
        }

        public void setApp_Pack_targetSDK(String app_Pack_targetSDK) {
            this.app_Pack_targetSDK = app_Pack_targetSDK;
        }

        public String getApp_mainPack_changeLog() {
            return app_mainPack_changeLog;
        }

        public void setApp_mainPack_changeLog(String app_mainPack_changeLog) {
            this.app_mainPack_changeLog = app_mainPack_changeLog;
        }

        public Long getMainPackageFileSize() {
            return mainPackageFileSize;
        }

        public void setMainPackageFileSize(Long mainPackageFileSize) {
            this.mainPackageFileSize = mainPackageFileSize;
        }

        public List<String> getPermissionDetailList() {
            return permissionDetailList;
        }

        public void setPermissionDetailList(List<String> permissionDetailList) {
            this.permissionDetailList = permissionDetailList;
        }

        public Boolean getDeleted() {
            return isDeleted;
        }

        public void setDeleted(Boolean deleted) {
            isDeleted = deleted;
        }

        public List<String> getKeyword() {
            return keyword;
        }

        public void setKeyword(List<String> keyword) {
            this.keyword = keyword;
        }

        public DateTime getLastModifyDate() {
            return lastModifyDate;
        }

        public void setLastModifyDate(DateTime lastModifyDate) {
            this.lastModifyDate = lastModifyDate;
        }

        public Long getApp_mainPack_id() {
            return app_mainPack_id;
        }

        public void setApp_mainPack_id(Long app_mainPack_id) {
            this.app_mainPack_id = app_mainPack_id;
        }
    }

    public Long count(AppSearchCriteria criteria, Session session) throws Exception {
        HQLBuilder builder = new HQLBuilder(session, "select count(app.id) ", " from App app left outer join app.os os " +
                " left outer join app.mainPackage mainPackage ");

        applyCurrentDeveloperCriteria(builder);

        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }

    public String getAppDescription(String packageName, OSType osType, Session session) {
        String queryString = "select app.description from App app where app.packageName = :packageName_ and app.osType = :osType_ ";
        Query query = session.createQuery(queryString);
        query.setParameter("packageName_", packageName);
        query.setParameter("osType_", osType);
        String appDesc = (String) query.uniqueResult();
        return appDesc;
    }

    private static void applyCurrentDeveloperCriteria(HQLBuilder builder) {
        if (PrincipalUtil.isCurrentUserDeveloper() != null && PrincipalUtil.isCurrentUserDeveloper()) {
            builder.addClause("and app.developer = :developer_", "developer_", PrincipalUtil.getCurrentUser());
        }

    }

    public List<AppSearchResultModel> list(AppSearchCriteria criteria, int first, int count, String sortProp, boolean isAsc, Session session) throws Exception {
        HQLBuilder builder = new HQLBuilder(session, "select app, mainPackage, os, packFile ", " from App app " +
                " left outer join app.os os " +
                " left outer join app.mainPackage mainPackage " +
                " left outer join mainPackage.packFile packFile "
        );

        if (criteria != null && (criteria.ignoreDeveloperInSearch == null || !criteria.ignoreDeveloperInSearch)) {
            applyCurrentDeveloperCriteria(builder);
        }

        if (criteria != null)
            applyCriteria(builder, criteria);

        if (sortProp != null)
            builder.addOrder(sortProp, isAsc);

        Query query = builder.createQuery();


        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);

        List<Object[]> resultObjects = query.list();
        Stream<AppSearchResultModel> searchResultModelStream =
                resultObjects.stream().map(new Function<Object[], AppSearchResultModel>() {
                    @Override
                    public AppSearchResultModel apply(Object[] objects) {
                        AppSearchResultModel searchResultModel = new AppSearchResultModel();
                        App app = (App) objects[0];
                        AppPackage mainPackage = (AppPackage) objects[1];
                        OS os = (OS) objects[2];
                        File packFile = (File) objects[3];
                        searchResultModel.appId = app.getId();
                        searchResultModel.osName = os != null ? os.getOsName() : "";
                        searchResultModel.osId = os != null ? os.getId() : null;

                        User appDeveloper = app.getDeveloper();
                        searchResultModel.developerName = appDeveloper != null ? appDeveloper.getFullName() : "";

                        if (mainPackage != null) {
                            searchResultModel.app_mainPack_versionCode = mainPackage.getVersionCode();
                            searchResultModel.app_mainPack_versionName = mainPackage.getVersionName();
                            searchResultModel.app_mainPack_minSDK = mainPackage.getMinSDK();
                            searchResultModel.app_mainPack_targetSDK = mainPackage.getTargetSDK();
                            searchResultModel.publishState = mainPackage.getPublishState();
                            searchResultModel.app_mainPack_changeLog = mainPackage.getChangeLog();
                            searchResultModel.app_mainPack_id = mainPackage.getId();
                        }
                        searchResultModel.osType = app.getOsType();
                        searchResultModel.appCategory = app.getAppCategory();
                        searchResultModel.packageName = app.getPackageName();
                        searchResultModel.title = app.getTitle();
                        searchResultModel.shortDescription = app.getShortDescription();
                        searchResultModel.setApp_mainPack_fileName(packFile.getFileName());
                        searchResultModel.setApp_mainPack_fileSize(packFile.getFileSize());
                        searchResultModel.setDeleted(app.getDeleted());

                        searchResultModel.app_mainPack_filePath = packFile.getFilePath();

                        searchResultModel.setApp_mainPack_iconFile(
                                mainPackage.getIconFile() != null ? mainPackage.getIconFile().getFileName() : null);

                        searchResultModel.setApp_mainPack_iconPath(
                                mainPackage.getIconFile() != null ? mainPackage.getIconFile().getFilePath() : null);

                        searchResultModel.setCreationDateTime(app.getCreationDate());

                        searchResultModel.setCreatorUser(app.getCreatorUser());
                        File mainPackageFile = mainPackage.getPackFile();
                        searchResultModel.setMainPackageFileSize(mainPackageFile.getFileSize());

                        String appMainPackagePermissionDetailAsString = mainPackage.getPermissionDetail();
                        if (appMainPackagePermissionDetailAsString != null && !appMainPackagePermissionDetailAsString.trim().isEmpty()) {
                            String[] permissionDetailAsArray = appMainPackagePermissionDetailAsString.split(",");
                            List<String> permissionDetailList = new ArrayList<>();
                            for (String permissionDetail : permissionDetailAsArray) {
                                permissionDetailList.add(permissionDetail.trim());
                            }
                            searchResultModel.setPermissionDetailList(permissionDetailList);
                        }
                        AppElasticService.AppKeyWordCriteria appKeyWordCriteria = new AppElasticService.AppKeyWordCriteria();
                        appKeyWordCriteria.appPackageName = app.getPackageName();
                        appKeyWordCriteria.osTypeId = app.getOsType().getId();
                        List<String> keyWordList = null;

                        try {
                            List<AppElasticService.AppKeyWordVO> keyWordVOList =
                                    AppElasticService.Instance.listAppKeyWord(appKeyWordCriteria, 0, 1);

                            if (!keyWordVOList.isEmpty()) {
                                AppElasticService.AppKeyWordVO appKeyWordVO = keyWordVOList.get(0);
                                keyWordList = appKeyWordVO.splitAppKeyWord();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        searchResultModel.setKeyword(keyWordList);
                        searchResultModel.setLastModifyDate(app.getLastModifyDate());
                        searchResultModel.setCreationDateTime(app.getCreationDate());
                        return searchResultModel;
                    }
                });

        List<AppSearchResultModel> searchResultModelList =
                searchResultModelStream.collect(Collectors.<AppSearchResultModel>toList());

        return searchResultModelList;
    }

    public List<AppSearchResultModel> searchByPackages(AppSearchCriteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select app, appPackage, os ", " from App app " +
                " ,AppPackage appPackage " +
                " left outer join app.os os ");

        applyCurrentDeveloperCriteria(builder);

        builder.addClause("and appPackage in elements(app.appPackages)");

        if (criteria != null) {
            if (criteria.osType != null && !criteria.osType.isEmpty())
                builder.addClause("and app.osType in (:osTypes_)", "osTypes_", criteria.osType);

            if (criteria.versionCode != null)
                builder.addClause("and appPackage.versionCode = :versionCode_", "versionCode_", criteria.versionCode);

            if (criteria.packageName != null && !criteria.packageName.isEmpty())
                builder.addClause("and app.packageName = :packageName_", "packageName_", criteria.packageName);
        }

        Query query = builder.createQuery();

        List<Object[]> resultObjects = query.list();
        Stream<AppSearchResultModel> searchResultModelStream =
                resultObjects.stream().map(new Function<Object[], AppSearchResultModel>() {
                    @Override
                    public AppSearchResultModel apply(Object[] objects) {
                        AppSearchResultModel searchResultModel = new AppSearchResultModel();
                        App app = (App) objects[0];
                        AppPackage appPackage = (AppPackage) objects[1];
                        OS os = (OS) objects[2];
                        searchResultModel.appId = app.getId();
                        searchResultModel.osName = os != null ? os.getOsName() : "";
                        searchResultModel.osId = os != null ? os.getId() : null;

                        User appDeveloper = app.getDeveloper();
                        searchResultModel.developerName = appDeveloper != null ? appDeveloper.getFullName() : "";

                        if (appPackage != null) {

                            searchResultModel.app_Pack_versionCode = appPackage.getVersionCode();
                            searchResultModel.app_Pack_versionName = appPackage.getVersionName();
                            searchResultModel.app_Pack_minSDK = appPackage.getMinSDK();
                            searchResultModel.app_Pack_targetSDK = appPackage.getTargetSDK();
                            searchResultModel.publishState = appPackage.getPublishState();
                        }
                        searchResultModel.osType = app.getOsType();
                        searchResultModel.packageName = app.getPackageName();
                        searchResultModel.title = app.getTitle();
                        searchResultModel.shortDescription = app.getShortDescription();
                        searchResultModel.setApp_Pack_filePath(appPackage.getPackFile().getFilePath());
                        searchResultModel.setApp_Pack_fileName(appPackage.getPackFile().getFileName());

                        searchResultModel.setApp_Pack_iconFile(
                                appPackage.getIconFile() != null ? appPackage.getIconFile().getFileName() : null);

                        searchResultModel.setApp_Pack_iconPath(
                                appPackage.getIconFile() != null ? appPackage.getIconFile().getFilePath() : null);

                        searchResultModel.setCreationDateTime(app.getCreationDate());
                        searchResultModel.setLastModifyDate(app.getLastModifyDate());

                        searchResultModel.setCreatorUser(app.getCreatorUser());

                        searchResultModel.setCreatorUser(app.getCreatorUser());
                        AppPackage mainPackage = app.getMainPackage();
                        File mainPackageFile = mainPackage.getPackFile();

                        searchResultModel.setMainPackageFileSize(mainPackageFile.getFileSize());


                        AppElasticService.AppKeyWordCriteria appKeyWordCriteria = new AppElasticService.AppKeyWordCriteria();
                        appKeyWordCriteria.appPackageName = app.getPackageName();
                        appKeyWordCriteria.osTypeId = app.getOsType().getId();
                        List<String> keyWordList = null;

                        try {
                            List<AppElasticService.AppKeyWordVO> keyWordVOList =
                                    AppElasticService.Instance.listAppKeyWord(appKeyWordCriteria, 0, 1);

                            if (!keyWordVOList.isEmpty()) {
                                AppElasticService.AppKeyWordVO appKeyWordVO = keyWordVOList.get(0);
                                keyWordList = appKeyWordVO.splitAppKeyWord();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        searchResultModel.setKeyword(keyWordList);

                        return searchResultModel;
                    }
                });

        List<AppSearchResultModel> searchResultModelList =
                searchResultModelStream.collect(Collectors.<AppSearchResultModel>toList());

        return searchResultModelList;
    }

    public void saveOrUpdate(App app, Session session) throws Exception {
        if (app.getId() == null) {
            app.setCreationDate(DateTime.now());
            app.setCreatorUser(PrincipalUtil.getCurrentUser());
            session.saveOrUpdate(app);

        } else {
            AppHistory appHistory = AppHistoryService.Instance.setAppHistoryByApp(session, app);
//            App appForSaveOrUpdate  = AppHistoryService.Instance.setAppHistoryToApp(session ,app);

            if (appHistory != null) {
                if (appHistory != null && appHistory.getId() != null) {
                    if (appHistory != null) {
                        List<AppHistory> appHistories = null;
                        appHistories = app.getHistories();
                        if (appHistories == null)
                            appHistories = new ArrayList<AppHistory>();
                        appHistories.add(appHistory);
                        app.setHistories(appHistories);
                    }
                }
            }

            app.setLastModifyDate(DateTime.now());
            app.setLastModifyUser(PrincipalUtil.getCurrentUser());

            app.getHistories();
            session.saveOrUpdate(app);
        }
    }

    public App load(Long appId, Session session) {
        App app = (App) session.load(App.class, appId);
        return app;
    }

    public boolean doesPackageExists(String packageName, OSType osType, Session session) {
        AppSearchCriteria criteria = new AppSearchCriteria();
        criteria.packageName = packageName;
        criteria.osType = new ArrayList<>();
        criteria.osType.add(osType);
        long resultCount = 0l;
        criteria.setDeleted(false);
        Boolean existPackage = null;
        try {
            existPackage = checkExistUnDeletedApp(criteria, session);
        } catch (Exception e) {
            return false;
        }
        return existPackage;
    }

    public List<AppSearchResultModel> getUpdatableProducts(Map<String, String> packageName2VersionCodeMap, OSType osType, Session session) throws Exception {
        List<AppSearchResultModel> finalModels = new ArrayList<>();

        session.refresh(osType);
        Comparator versionComparator = OSTypeService.Instance.getVersionComparatorForOSType(osType);

        int first = 0;
        int chunkLength = 999;
        int size = packageName2VersionCodeMap.size();

        List<String> packageNames = new ArrayList<>(packageName2VersionCodeMap.keySet());

        while (true) {
            if (first >= size)
                break;

            if ((first + chunkLength) > size)
                chunkLength = size - first;

            List<String> subPackageNameList = packageNames.subList(first, first + chunkLength);

            AppSearchCriteria appSearchCriteria = new AppSearchCriteria();
            appSearchCriteria.packageNames = subPackageNameList;
            appSearchCriteria.osType = new ArrayList<>();
            appSearchCriteria.osType.add(osType);
            appSearchCriteria.publishStates = new ArrayList<>();
            appSearchCriteria.publishStates.add(PublishState.PUBLISHED);

            appSearchCriteria.setDeleted(false);
            List<AppSearchResultModel> resultList = list(appSearchCriteria, 0, -1, null, true, session);

            for (AppSearchResultModel searchResultModel : resultList) {
                String client_versionCode = packageName2VersionCodeMap.get(searchResultModel.getPackageName());
                int compareResult = versionComparator.compare(client_versionCode, searchResultModel.getApp_mainPack_versionCode());
                if (compareResult > 0) {
                    finalModels.add(searchResultModel);
                }
            }

            first = first + chunkLength;
        }

        return finalModels;
    }


    public List<App> listAll() {
        Session session = HibernateUtil.getCurrentSession();
        HQLBuilder builder = new HQLBuilder(session, "select app ", "from App app ");
        builder.addClause(" and (app.isDeleted is null or app.isDeleted = :isDeleted_)", "isDeleted_", false);

        Query query = builder.createQuery();
        return query.list();
    }

    public App getAppByPackageName(OSType osType, String packageName, Boolean isDeleted, Session session) {

        HQLBuilder builder = new HQLBuilder(session, "select app ", "from App app ");
        builder.addClause(" and app.packageName = :packageName_", "packageName_", packageName);
        builder.addClause(" and app.osType = :osType_", "osType_", osType);
        if (!isDeleted) {
            builder.addClause(" and ( app.isDeleted is null or app.isDeleted = :isDeleted_)", "isDeleted_", isDeleted);
        } else {
            builder.addClause(" and app.isDeleted = :isDeleted_", "isDeleted_", isDeleted);

        }
        Query query = builder.createQuery();


        List<App> appList = query.list();
        if (!appList.isEmpty())
            return appList.get(0);
        return null;
    }

    public void savePackageForApp(
            App app,
            String packageFileName,
            String packageFileKey,
            String iconFileName,
            String iconFileKey,
            Map<String, String> thumbFileInfoMap,
            OSType osType,
            String description,
            String title,
            Long selectedAppCategoryId,
            Session session) throws Exception {

        savePackageForApp(app, packageFileName, packageFileKey, iconFileName, iconFileKey,
                thumbFileInfoMap, osType, description, title, null, null, selectedAppCategoryId, session);
    }

    public void savePackageForApp(
            App app,
            String packageFileName,
            String packageFileKey,
            String iconFileName,
            String iconFileKey,
            Map<String, String> thumbFileInfoMap,
            OSType osType,
            String shortDescription,
            String description,
            String title,
            Long selectedAppCategoryId,
            Session session) throws Exception {

        savePackageForApp(app, packageFileName, packageFileKey, iconFileName, iconFileKey,
                thumbFileInfoMap, osType, description, title, shortDescription, null, selectedAppCategoryId, session);
    }

    public void savePackageForApp(
            App app,
            String packageFileName,
            String packageFileKey,
            String iconFileName,
            String iconFileKey,
            Map<String, String> thumbFileInfoMap,
            OSType osType,
            String shortDescription,
            String description,
            String title,
            Long selectedAppCategoryId,
            String publishStateStr,
            Boolean forReplacement,
            Session session) throws Exception {

        savePackageForApp(app, packageFileName, packageFileKey, iconFileName, iconFileKey,
                thumbFileInfoMap, osType, description, title, shortDescription, null, selectedAppCategoryId, publishStateStr, forReplacement, session);
    }


    public void savePackageForApp(
            App app,
            String packageFileName,
            String packageFileKey,
            String iconFileName,
            String iconFileKey,
            Map<String, String> thumbFileInfoMap,
            OSType osType,
            String description,
            String title,
            String shortDescription,
            Long packageFileSize,
            Long selectedAppCategoryId,
            Session session) throws Exception {

        savePackageForApp(app, packageFileName, packageFileKey, iconFileName, iconFileKey, thumbFileInfoMap, osType, description, title, shortDescription, packageFileSize, selectedAppCategoryId, null, false, session);

    }


    public void savePackageForApp(
            App app,
            String packageFileName,
            String packageFileKey,
            String iconFileName,
            String iconFileKey,
            Map<String, String> thumbFileInfoMap,
            OSType osType,
            String description,
            String title,
            String shortDescription,
            Long packageFileSize,
            Long selectedAppCategoryId,
            String publishStateStr,
            Boolean forReplacement,
            Session session) throws Exception {

        try {
        AppPackage appPackage;
        if (packageFileKey != null) {
            IAPPPackageService iappPackageService =
                    AppPackageService.Instance.processPackageFile(packageFileKey, osType);
            AppPackageService.NewPackageInfo packageInfo =
                    AppPackageService.Instance.validateNewAppPackage(app, iappPackageService, forReplacement);

            appPackage = new AppPackage();
            //in insert package can't insert deleted package
            appPackage.setDeleted(false);
            if (app.getId() == null) {
                app.setPackageName(iappPackageService.getPackage().trim());
            }

            if (iappPackageService.getPermissions() != null) {
                String newPermissionDetail = String.join(",", iappPackageService.getPermissions());
                appPackage.setPermissionDetail(newPermissionDetail);
            }
            appPackage.setDeleted(false);
            appPackage.setVersionCode(iappPackageService.getVersionCode().trim());
            appPackage.setVersionName(iappPackageService.getVersionName().trim());
            appPackage.setMinSDK(iappPackageService.getMinSDK());
            appPackage.setTargetSDK(iappPackageService.getTargetSDK());

            List<String> permissionDetailAsList = iappPackageService.getPermissions();
            if (permissionDetailAsList != null) {
                appPackage.setPermissionDetail(String.join(",", permissionDetailAsList));
            }

            if (packageInfo.getCertificate() == null) {
                throw new Exception("certificationNotValid");
            }

            byte[] encodedCertBytes = packageInfo.getCertificate().getEncoded();
            appPackage.setCertificateInfo(org.apache.xerces.impl.dv.util.Base64.encode(encodedCertBytes));

            if (packageFileKey != null) {
                File packFile = new File();
                packFile.setFilePath(packageFileKey);
                packFile.setFileName(packageFileName);
                packFile.setStereoType(StereoType.MAIN_APP_PACK_FILE);
                BaseEntityService.Instance.saveOrUpdate(packFile, session);
                if (packageFileKey != null && !FileServerService.Instance.doesFileExistOnFileServer(packageFileKey)) {
                    FileServerService.Instance.persistFileToServer(packageFileKey);
                }
                Long fileSize = FileServerService.Instance.getFileSizeByFileKey(packageFileKey);
                packFile.setFileSize(fileSize);
                appPackage.setPackFile(packFile);

            }
        } else {
            appPackage = app.getMainPackage();
        }

        if (iconFileKey != null) {
            File iconFile = new File();
            iconFile.setStereoType(StereoType.ICON_FILE);
            iconFile.setFileName(iconFileName);
            iconFile.setFilePath(iconFileKey);
            appPackage.setIconFile(iconFile);
            BaseEntityService.Instance.saveOrUpdate(iconFile, session);
            if (!FileServerService.Instance.doesFileExistOnFileServer(iconFileKey))
                FileServerService.Instance.persistFileToServer(iconFileKey);
        }

        if (thumbFileInfoMap != null && !thumbFileInfoMap.isEmpty()) {
            List<String> inputThumbFiles = new ArrayList(thumbFileInfoMap.keySet());
            List<File> thumbFiles = AppPackageService.Instance.setImagesToPackage(inputThumbFiles, session);
            appPackage.setThumbImages(thumbFiles);
        }

        if (packageFileKey != null && !forReplacement) {
            List<AppPackage> appPackages = null;
            if (app.getId() != null) {
                appPackages = app.getAppPackages();
            }

            if (appPackages == null)
                appPackages = new ArrayList<AppPackage>();

            appPackages.add(appPackage);

            if (app.getId() == null) {
                app.setMainPackage(appPackage);
            }

               /* app.setMainPackage(appPackage);*/

            app.setAppPackages(appPackages);
        }

        if (publishStateStr != null && !publishStateStr.trim().equals("")) {
            PublishState publishState = new PublishState(Byte.valueOf(publishStateStr));
            appPackage.setPublishState(publishState);
        }
        boolean needUpdateAppPackage = false;
        if (appPackage.getId() != null) {
            AppPackage loadedAppPackage = (AppPackage) session.load(AppPackage.class, appPackage.getId());
            if (appPackage.getIconFile() != null) {
                if (!appPackage.getIconFile().getFilePath().equals(loadedAppPackage.getIconFile().getFilePath())) {
                    needUpdateAppPackage = true;
                }
            }
            if (!needUpdateAppPackage && appPackage.getPackFile() != null) {
                if (!appPackage.getPackFile().getFilePath().equals(loadedAppPackage.getPackFile().getFilePath())) {
                    needUpdateAppPackage = true;
                }
            }
            if (!needUpdateAppPackage && appPackage.getThumbImages() != null) {
                List<File> appPackageThumbFileList = appPackage.getThumbImages();
                List<File> loadedAppPackageThumbFileList = loadedAppPackage.getThumbImages();
                for (File appPackageThumbFile : appPackageThumbFileList) {
                    if (!loadedAppPackageThumbFileList.contains(appPackageThumbFile)) {
                        needUpdateAppPackage = true;
                    }
                }
                for (File loadedAppPackageThumbFile : loadedAppPackageThumbFileList) {
                    if (!appPackageThumbFileList.contains(loadedAppPackageThumbFile)) {
                        needUpdateAppPackage = true;
                    }
                }
            }
        }
        if (appPackage.getId() == null || needUpdateAppPackage) {
            AppPackageService.Instance.saveOrUpdate(appPackage, session);
            if (app.getMainPackage() != null && app.getMainPackage().equals(appPackage)) {
                app.setMainPackage(appPackage);
            } else {
                List<AppPackage> appPackageList = app.getAppPackages() != null && !app.getAppPackages().isEmpty() ? app.getAppPackages() : new ArrayList<>();
                if (appPackage.getId() != null) {
                    AppPackage loadedPackage = (AppPackage) session.get(AppPackage.class, appPackage.getId());
                    if (loadedPackage != null) {
                        appPackageList.remove(loadedPackage);
                        appPackageList.add(appPackage);
                        app.setAppPackages(appPackageList);
                    }
                }
            }
        }

        app.setDescription(description);
        app.setTitle(title);
        if (shortDescription != null && !shortDescription.trim().equals("")) {
            app.setShortDescription(shortDescription);
        }

        AppCategory selectedAppCategory = (AppCategory) session.load(AppCategory.class, selectedAppCategoryId);
        app.setAppCategory(selectedAppCategory);
        if (app.getDeleted() != null && app.getDeleted()) {
            AppService.Instance.deleteApp(app, session);
        } else {
            AppService.Instance.saveOrUpdate(app, session);
        }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }


    public void deleteApp(App app, Session session) throws Exception {
        AppPackage mainPackage = app.getMainPackage();

        for (AppPackage packageInList : app.getAppPackages()) {
            packageInList.setDeleted(true);
            AppPackageService.Instance.saveOrUpdate(packageInList, session);
        }
        if (!mainPackage.getDeleted()) {
            mainPackage.setDeleted(true);
            AppPackageService.Instance.saveOrUpdate(mainPackage, session);
        }
        app.setDeleted(true);
        try {
            AppService.Instance.saveOrUpdate(app, session);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }


    public Long getRelatedAppsCount(App app) throws Exception {
        Integer relevencyRateThreshold = Integer.parseInt(
                ConfigUtil.getProperty(ConfigUtil.APP_RELEVENCYRATE_RATIO));
        AppElasticService.AppRelevancyRateCriteria appRelevancyRateCriteria =
                new AppElasticService.AppRelevancyRateCriteria();
        appRelevancyRateCriteria.appId = app.getId();
        appRelevancyRateCriteria.relevancyRateFrom = relevencyRateThreshold;

        Long relatedAppCount = AppElasticService.Instance.countAppRelevencyRate(appRelevancyRateCriteria);

        return relatedAppCount;
    }

    public List<App> getRelatedApps(App app, int from, int count, Session session) throws Exception {
        List<App> relatedAppList = new ArrayList<>();

        Integer relevencyRateThreshold = Integer.parseInt(
                ConfigUtil.getProperty(ConfigUtil.APP_RELEVENCYRATE_RATIO));
        AppElasticService.AppRelevancyRateCriteria appRelevancyRateCriteria =
                new AppElasticService.AppRelevancyRateCriteria();
        appRelevancyRateCriteria.appId = app.getId();
        appRelevancyRateCriteria.relevancyRateFrom = relevencyRateThreshold;
        appRelevancyRateCriteria.excludedAppIds = Arrays.asList(app.getId());

        Integer relatedAppCount = AppElasticService.Instance.countAppRelevencyRate(appRelevancyRateCriteria).intValue();

        if (relatedAppCount > 0) {
            count = count - from;

            while (true) {
                List<AppElasticService.AppRelevancyRateVO> appRelevancyRateVOList =
                        AppElasticService.Instance.listAppRelevencyRate(appRelevancyRateCriteria, from, count, "relevancyRate", false);

                for (AppElasticService.AppRelevancyRateVO relevancyRateVO : appRelevancyRateVOList) {
                    try {
                        App intendedApp = AppService.Instance.load(relevancyRateVO.appComparedToId, session);
                        relatedAppList.add(intendedApp);
                    } catch (Exception ex) {
                        logger.error("error occured acquiring related app ", ex);
                    }
                }

                if (from + 10 > relatedAppCount)
                    from = from + (relatedAppCount - 10);
                else
                    from = from + 10;

                if (from >= relatedAppCount || from <= 0)
                    break;

            }
        }

        return relatedAppList;
    }

    public static class RelatedAppCalculatorJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Session session = HibernateUtil.getCurrentSession();
            try {
                Date date = new Date();
                Calendar calendar = Calendar.getInstance();
                Integer lastCheckRelevencySpan = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_LAST_CHECK_RELEVENCY_SPAN));
                date = DateUtils.addDays(date, lastCheckRelevencySpan);

                calendar.add(Calendar.MINUTE, lastCheckRelevencySpan);
                DateTime threshHoldDateTime = new DateTime(date);
                threshHoldDateTime.setDayTime(DayTime.MIN_DAY_TIME);

                HQLBuilder countHqlBuilder = new HQLBuilder(session, "select count(app.id) ", " from App app ");
                countHqlBuilder.addClause(
                        "and ((app.relatedAppCalculationDate.dayDate <= :relatedAppCalculationDate_) ",
                        "relatedAppCalculationDate_", threshHoldDateTime.getDayDate());
                countHqlBuilder.addClause(" or (app.relatedAppCalculationDate is null))");
                countHqlBuilder.addClause("and app.mainPackage.publishState = :publishState_", "publishState_", PublishState.PUBLISHED);

                Query countQuery = countHqlBuilder.createQuery();
                Long count = (Long) countQuery.uniqueResult();

                if (count > 0) {
                    HQLBuilder hqlBuilder = new HQLBuilder(session, "select app ", " from App app ");
                    hqlBuilder.addClause(
                            "and ((app.relatedAppCalculationDate.dayDate <= :relatedAppCalculationDate_) ",
                            "relatedAppCalculationDate_", threshHoldDateTime.getDayDate());
                    hqlBuilder.addClause(" or (app.relatedAppCalculationDate is null))");
                    hqlBuilder.addClause("and app.mainPackage.publishState = :publishState_", "publishState_", PublishState.PUBLISHED);

                    Query query = hqlBuilder.createQuery();

                    int to = 0;

                    Transaction tx = null;

                    while (true) {
                        tx = session.beginTransaction();

                        query.setFirstResult(0).setMaxResults(10);
                        List<App> partialAppList = query.list();

                        for (App app : partialAppList) {
                            Instance.calculateRelatedApps(app, session);

                            app.setRelatedAppCalculationDate(DateTime.now());
                            session.saveOrUpdate(app);
                        }

                        tx.commit();

                        if (to == count.intValue())
                            break;

                        if (to + 10 > count)
                            to = to + (count.intValue() - to);
                        else
                            to = to + 10;

                        if (to >= count || to < 0)
                            break;
                    }
                }

            } catch (Exception ex) {
                logger.error("Error occured in RelatedApp Job! ", ex);
            } finally {
                session.close();
            }
        }
    }

    public void calculateRelatedApps(App app, Session session) throws Exception {
        AppElasticService.Instance.deleteAllAppRelevencies(app.getId());

        String countOfAppsQueryString = "select count(app.id) from App app inner join app.osType osType where osType=:osType_ and app.mainPackage.publishState = :publishState_";
        String appsListQueryString = "select app from App app inner join app.osType osType where osType=:osType_ and app.mainPackage.publishState = :publishState_";

        Query countQuery = session.createQuery(countOfAppsQueryString);
        countQuery.setParameter("osType_", app.getOsType());
        countQuery.setParameter("publishState_", PublishState.PUBLISHED);
        Long countOfApps = (Long) countQuery.uniqueResult();

        Query listQuery = session.createQuery(appsListQueryString);
        listQuery.setParameter("osType_", app.getOsType());
        listQuery.setParameter("publishState_", PublishState.PUBLISHED);


        AppElasticService.AppKeyWordCriteria appKeyWordCriteria = new AppElasticService.AppKeyWordCriteria();
        appKeyWordCriteria.appPackageName = app.getPackageName();
        appKeyWordCriteria.osTypeId = app.getOsType().getId();

        List<AppElasticService.AppKeyWordVO> keyWordVOList =
                AppElasticService.Instance.listAppKeyWord(appKeyWordCriteria, 0, 1);

        List<String> keyWordList = null;
        if (!keyWordVOList.isEmpty()) {
            AppElasticService.AppKeyWordVO appKeyWordVO = keyWordVOList.get(0);
            keyWordList = appKeyWordVO.splitAppKeyWord();
        }

        if (countOfApps > 0) {
            int from = 0;
            int to = 10;

            while (true) {
                listQuery.setFirstResult(from).setMaxResults(to);
                List<App> partialAppList = listQuery.list();

                for (App tempApp : partialAppList) {
                    int relevencyRate = 0;
                    if (tempApp.getAppCategory() != null && tempApp.getAppCategory().equals(app.getAppCategory()))
                        relevencyRate += 10;

                    if (tempApp.getDeveloper().equals(app.getDeveloper()))
                        relevencyRate += 10;

                    List<AppElasticService.AppKeyWordVO> appsWithSameKeywordList = null;
                    if (keyWordList != null) {
                        appKeyWordCriteria = new AppElasticService.AppKeyWordCriteria();
                        appKeyWordCriteria.appId = tempApp.getId();

                        for (int i = 0; i < keyWordList.size(); i++) {
                            appKeyWordCriteria.keyWordList = Arrays.asList(keyWordList.get(i));
                            Integer countOfSameKeyWordApps = AppElasticService.Instance.countAppKeyWord(appKeyWordCriteria).intValue();

                            if (countOfSameKeyWordApps > 0)
                                relevencyRate += 10;
                        }

                        appKeyWordCriteria.keyWordList = keyWordList;
                        Integer countOfSameKeyWordApps = AppElasticService.Instance.countAppKeyWord(appKeyWordCriteria).intValue();

                        if (countOfSameKeyWordApps > 0)
                            relevencyRate += 10;
                    }

                    AppElasticService.AppRelevancyRateVO appRelevancyRateVO = new AppElasticService.AppRelevancyRateVO();
                    appRelevancyRateVO.appId = app.getId();
                    appRelevancyRateVO.appComparedToId = tempApp.getId();
                    appRelevancyRateVO.relevancyRate = relevencyRate;

                    AppElasticService.Instance.insertAppRelevancyRateVO(appRelevancyRateVO);

                }

                from = to;
                if (to >= countOfApps)
                    break;

                if (to + 10 > countOfApps)
                    to = to + (countOfApps.intValue() - to);
                else
                    to = to + 10;

            }
        }
    }

    public Boolean checkExistUnDeletedApp(AppSearchCriteria appSearchCriteria, Session session) throws Exception {
        if (appSearchCriteria != null) {
            appSearchCriteria.setIgnoreDeveloperInSearch(true);
            List<AppSearchResultModel> appSearchResultModelList = AppService.Instance.list(appSearchCriteria, 0, -1, null, false, session);
            if (appSearchResultModelList != null && !appSearchResultModelList.isEmpty()) {
                if (appSearchCriteria.getId() != null) {
                    for (AppSearchResultModel appSearchResultModel : appSearchResultModelList) {
                        if (!appSearchResultModel.getAppId().equals(appSearchCriteria.getId())) {
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            }
            return false;
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        /*
        HQLBuilder builder = new HQLBuilder(session, "select app ", " from App app " +
                " left outer join app.os os " +
                " left outer join app.appPackages appPackages "
        );

        applyCurrentDeveloperCriteria(builder);

        builder.addClause("and appPackages.versionCode = :versionCode", "versionCode", "3");

        Query query = builder.createQuery();

        List ls = query.list();

        System.out.println("");
        */

//        Session session = HibernateUtil.getCurrentSession();
//        try {
//            List<App> appList = AppService.Instance.listAll();
//
//            for (App app : appList) {
//                Transaction tx = session.beginTransaction();
//                String fileQueryServerPath = FileServerService.FILE_EXIST_QUERY_SERVER_PACKAGE_FILE_SIZE;
//                AppPackage mainPackage = app.getMainPackage();
//                File mainPackageFile = mainPackage.getPackFile();
//                fileQueryServerPath = fileQueryServerPath.replace("${key}", mainPackageFile.getFilePath());
//                HttpClient client = HttpClientBuilder.create().build();
//                HttpGet httpGet = new HttpGet(fileQueryServerPath);
//
//                HttpResponse response = client.execute(httpGet);
//
//                String json_string = EntityUtils.toString(response.getEntity());
//                mainPackageFile.setFileSize(Long.valueOf(json_string));
//                session.saveOrUpdate(mainPackageFile);
//                tx.commit();
//    }
//
//        } catch (Exception ex) {
//            throw new RuntimeException();
//        } finally {
//            if (session.isOpen()) {
//                session.close();
//            }
//        }
//
//        Session session = HibernateUtil.getCurrentSession();
//        App app = (App)session.load(App.class, 1071l);
//
//        Instance.calculateRelatedApps(app, session);
//
//        AppService.Instance.getRelatedApps(app, 0, 10, session);


        Session session = HibernateUtil.getCurrentSession();
        try {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            Integer lastCheckRelevencySpan = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_LAST_CHECK_RELEVENCY_SPAN));
            date = DateUtils.addDays(date, lastCheckRelevencySpan);

            calendar.add(Calendar.MINUTE, lastCheckRelevencySpan);
            DateTime threshHoldDateTime = new DateTime(date);
            threshHoldDateTime.setDayTime(DayTime.MIN_DAY_TIME);

            HQLBuilder countHqlBuilder = new HQLBuilder(session, "select count(app.id) ", " from App app ");
            countHqlBuilder.addClause(
                    "and ((app.relatedAppCalculationDate.dayDate <= :relatedAppCalculationDate_) ",
                    "relatedAppCalculationDate_", threshHoldDateTime.getDayDate());
            countHqlBuilder.addClause(" or (app.relatedAppCalculationDate is null))");
            countHqlBuilder.addClause("and app.mainPackage.publishState = :publishState_", "publishState_", PublishState.PUBLISHED);

//            countHqlBuilder.addClause("and app.id=:id_", "id_", 1040l);

            Query countQuery = countHqlBuilder.createQuery();
            Long count = (Long) countQuery.uniqueResult();

            if (count > 0) {
                HQLBuilder hqlBuilder = new HQLBuilder(session, "select app ", " from App app ");
                hqlBuilder.addClause(
                        "and ((app.relatedAppCalculationDate.dayDate <= :relatedAppCalculationDate_) ",
                        "relatedAppCalculationDate_", threshHoldDateTime.getDayDate());
                hqlBuilder.addClause(" or (app.relatedAppCalculationDate is null))");
                hqlBuilder.addClause("and app.mainPackage.publishState = :publishState_", "publishState_", PublishState.PUBLISHED);
//                hqlBuilder.addClause("and app.id=:id_", "id_", 1040l);

                Query query = hqlBuilder.createQuery();

                int to = 0;

                Transaction tx = null;

                while (true) {
                    tx = session.beginTransaction();

                    query.setFirstResult(0).setMaxResults(10);
                    List<App> partialAppList = query.list();

                    for (App app : partialAppList) {
                        Instance.calculateRelatedApps(app, session);

                        app.setRelatedAppCalculationDate(DateTime.now());
                        session.saveOrUpdate(app);
                    }

                    tx.commit();

                    if (to == count.intValue())
                        break;

                    if (to + 10 > count)
                        to = to + (count.intValue() - to);
                    else
                        to = to + 10;

                    if (to >= count || to < 0)
                        break;
                }
            }

        } catch (Exception ex) {
            logger.error("Error occured in RelatedApp Job! ", ex);
        } finally {
            session.close();
        }


        System.exit(0);


    }


}
