package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppElasticService;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by A.Moshiri on 7/19/2017.
 */
public class AppSearchVO {

    public AppSearchVO() {
    }

    public AppSearchVO(String inputStr) {
        JSONObject jsonObject = new JSONObject(inputStr);

        if (jsonObject.has("id") && !jsonObject.get("id").equals(JSONObject.NULL)) {
            this.id = jsonObject.getLong("id");
        }
        if (jsonObject.has("packageName") && !jsonObject.isNull("packageName")) {
            this.packageName = jsonObject.getString("packageName");
        }

        if (jsonObject.has("versionCode") && !jsonObject.isNull("versionCode")) {
            this.versionCode = jsonObject.getString("versionCode");
        }

        if (jsonObject.has("title") && !jsonObject.isNull("title")) {
            this.title = jsonObject.getString("title");
        }
        if (jsonObject.has("developerName") && !jsonObject.isNull("developerName")) {
            this.developerName = jsonObject.get("developerName").toString();
        }
        if (jsonObject.has("creatorUserName") && !jsonObject.isNull("creatorUserName")) {
            this.creatorUserName = jsonObject.get("creatorUserName").toString();
        }
        if (jsonObject.has("osId") && !jsonObject.isNull("osId")) {
            this.osId = jsonObject.get("osId").toString();
        }
        if (jsonObject.has("osName") && !jsonObject.isNull("osName")) {
            this.osName = jsonObject.get("osName").toString();
        }
        if (jsonObject.has("publishState") && !jsonObject.isNull("publishState")) {
            this.publishState = jsonObject.get("publishState").toString();
        }
        if (jsonObject.has("appCategoryName") && !jsonObject.isNull("appCategoryName")) {
            this.appCategoryName = jsonObject.get("appCategoryName").toString();
        }
        if (jsonObject.has("appCategoryId") && !jsonObject.isNull("appCategoryId")) {
            this.appCategoryId = jsonObject.get("appCategoryId").toString();
        }
        if (jsonObject.has("isDeleted")) {
            if (jsonObject.get("isDeleted") != null) {
                this.isDeleted = jsonObject.getBoolean("isDeleted");
            }
        } else {
            this.isDeleted = false;
        }

        if (jsonObject.has("keywords")) {
            JSONArray keywordJasonArray = jsonObject.getJSONArray("keywords");
            Set<String> keywordList = new HashSet<>();

            for (Object keywordJsonObj : keywordJasonArray) {
                keywordList.add(keywordJsonObj.toString());
            }
            if (keywordList.size() > 0) {
                this.keywords = new ArrayList<>(keywordList);
            }
        }

        if (jsonObject.has("publishStates")) {
            JSONArray publishStatesJasonArray = jsonObject.getJSONArray("publishStates");
            Set<PublishState> publishStatesList = new HashSet<>();
            try {
                for (Object publishStatesJsonObj : publishStatesJasonArray) {

                    if (publishStatesJsonObj != null) {
                        PublishState publishState = new PublishState(Byte.valueOf(publishStatesJsonObj.toString()));
                        publishStatesList.add(publishState);
                    }
                }
                if (publishStatesList.size() > 0) {
                    this.publishStates = publishStatesList;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (jsonObject.has("developerIds") || jsonObject.has("creatorUserIds") || jsonObject.has("osIds") || jsonObject.has("osTypeIds") || jsonObject.has("appCategoryIds")) {
            Session session = HibernateUtil.getNewSession();

            if (jsonObject.has("developerIds")) {
                JSONArray developerUserJasonArray = jsonObject.getJSONArray("developerIds");
                Set<User> developerUserList = new HashSet<>();

                for (Object developerUserJsonObj : developerUserJasonArray) {

                    User developerUser = (User) session.get(User.class, Long.valueOf(developerUserJsonObj.toString()));
                    if (developerUser != null) {
                        developerUserList.add(developerUser);
                    }
                }
                if (developerUserList.size() > 0) {
                    this.developers = developerUserList;
                }
            }

            if (jsonObject.has("creatorUsersIds")) {
                JSONArray creatorUsersJasonArray = jsonObject.getJSONArray("creatorUsersIds");
                Set<User> creatorUsersList = new HashSet<>();

                for (Object creatorUsersObj : creatorUsersJasonArray) {
                    User creatorUser = (User) session.get(User.class, Long.valueOf(creatorUsersObj.toString()));
                    if (creatorUser != null) {
                        creatorUsersList.add(creatorUser);
                    }
                }
                if (creatorUsersList.size() > 0) {
                    this.creatorUsers = creatorUsersList;
                }
            }

            if (jsonObject.has("osIds")) {
                JSONArray osJasonArray = jsonObject.getJSONArray("osIds");
                Set<OS> osList = new HashSet<>();

                for (Object osObj : osJasonArray) {
                    OS os = (OS) session.get(OS.class, Long.valueOf(osObj.toString()));
                    if (os != null) {
                        osList.add(os);
                    }
                }
                if (osList.size() > 0) {
                    this.os = osList;
                }
            }


            if (jsonObject.has("appCategoryIds")) {
                JSONArray categoryJasonArray = jsonObject.getJSONArray("appCategoryIds");
                Set<Long> categoryList = new HashSet<>();

                for (Object categoryObj : categoryJasonArray) {
                    categoryList.add(Long.valueOf(categoryObj.toString()));

                }
                if (categoryList.size() > 0) {
                    this.appCategoryIds = new ArrayList<>(categoryList);
                }
            }

            if (jsonObject.has("osTypeIds")) {
                JSONArray osTypeJasonArray = jsonObject.getJSONArray("osTypeIds");
                Set<OSType> osTypeList = new HashSet<>();

                for (Object osTypeObj : osTypeJasonArray) {
                    OSType osType = (OSType) session.get(OSType.class, Long.valueOf(osTypeObj.toString()));
                    if (osType != null) {
                        osTypeList.add(osType);
                    }
                }
                if (osTypeList.size() > 0) {
                    this.osType = osTypeList;
                }
            }
            session.close();
        }

    }

    public Long id;
    public String title;
    public String developerName;
    public String creatorUserName;
    public String versionCode;
    public String packageName;
    public String osId;
    public String osName;
    public String osTypeId;
    public String osTypeName;
    public String publishState;
    private String appCategoryName;
    private String appCategoryId;
    private Boolean isDeleted;
    public List<Long> appCategoryIds;
    public Collection<OSType> osType;
    public Collection<User> developers;
    public DateTime[] creationDateTime;
    public Collection<User> creatorUsers;
    public Collection<OS> os;
    public Collection<PublishState> publishStates;
    public List<String> keywords;

    AppPackageVO appPackageVO;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getCreatorUserName() {
        return creatorUserName;
    }

    public void setCreatorUserName(String creatorUserName) {
        this.creatorUserName = creatorUserName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getOsId() {
        return osId;
    }

    public void setOsId(String osId) {
        this.osId = osId;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getPublishState() {
        return publishState;
    }

    public void setPublishState(String publishState) {
        this.publishState = publishState;
    }

    public String getAppCategoryName() {
        return appCategoryName;
    }

    public void setAppCategoryName(String appCategoryName) {
        this.appCategoryName = appCategoryName;
    }

    public String getAppCategoryId() {
        return appCategoryId;
    }

    public void setAppCategoryId(String appCategoryId) {
        this.appCategoryId = appCategoryId;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public List<Long> getAppCategoryIds() {
        return appCategoryIds;
    }

    public void setAppCategoryIds(List<Long> appCategoryIds) {
        this.appCategoryIds = appCategoryIds;
    }

    public Collection<OSType> getOsType() {
        return osType;
    }

    public void setOsType(Collection<OSType> osType) {
        this.osType = osType;
    }

    public Collection<User> getDevelopers() {
        return developers;
    }

    public void setDevelopers(Collection<User> developers) {
        this.developers = developers;
    }

    public DateTime[] getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(DateTime[] creationDateTime) {
        this.creationDateTime = creationDateTime;
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

    public Collection<PublishState> getPublishStates() {
        return publishStates;
    }

    public void setPublishStates(Collection<PublishState> publishStates) {
        this.publishStates = publishStates;
    }

    public AppPackageVO getAppPackageVO() {
        return appPackageVO;
    }

    public void setAppPackageVO(AppPackageVO appPackageVO) {
        this.appPackageVO = appPackageVO;
    }

    public String getOsTypeId() {
        return osTypeId;
    }

    public void setOsTypeId(String osTypeId) {
        this.osTypeId = osTypeId;
    }

    public String getOsTypeName() {
        return osTypeName;
    }

    public void setOsTypeName(String osTypeName) {
        this.osTypeName = osTypeName;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public static AppService.AppSearchCriteria createCriteriaFromJson(AppSearchVO appSearchVO, Session session) {
        AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
        User currentUser = PrincipalUtil.getCurrentUser();
        User adminUser = UserService.Instance.findUser(ConfigUtil.getProperty(ConfigUtil.ADMIN_USER_NAME), session);
        User developerUser = null;
        List<String> creatorUserNameList = new ArrayList<>();
        List<String> developerNameList = new ArrayList<>();
        if (appSearchVO != null) {

            if (appSearchVO.getAppCategoryId() != null && !appSearchVO.getAppCategoryId().trim().equals("")
                    && appSearchVO.getAppCategoryIds() != null && !appSearchVO.getAppCategoryIds().isEmpty()) {
                try {
                    appSearchVO.getAppCategoryIds().add(Long.valueOf(appSearchVO.getAppCategoryId()));
                } catch (NumberFormatException e) {

                }
                appSearchVO.setAppCategoryId(null);
            }

            if (appSearchVO.getOsId() != null && !appSearchVO.getOsId().trim().equals("") && appSearchVO.getOs() != null && !appSearchVO.getOs().isEmpty()) {
                try {
                    OS loadedOs = (OS) session.get(OS.class, Long.valueOf(appSearchVO.getOsId()));
                    if (loadedOs != null) {
                        appSearchVO.getOs().add(loadedOs);
                        appSearchVO.setOsId(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (appSearchVO.getPublishState() != null && !appSearchVO.getPublishState().trim().equals("") && appSearchVO.getPublishStates() != null &&
                    !appSearchVO.getPublishStates().isEmpty()) {
                try {
                    PublishState publishState = new PublishState(Byte.valueOf(appSearchVO.getPublishState()));
                    if (publishState != null) {
                        appSearchVO.getPublishStates().add(publishState);
                        appSearchVO.setPublishState(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            criteria.title = appSearchVO.getTitle();

            criteria.packageName = appSearchVO.getPackageName();

            criteria.versionCode = appSearchVO.getVersionCode();

            if (appSearchVO.getPublishState() != null && !appSearchVO.getPublishState().trim().equals("")) {
                try {
                    List<PublishState> publishStateList = new ArrayList<>();

                    PublishState publishState = new PublishState(Byte.valueOf(appSearchVO.getPublishState()));
                    if (publishState != null) {
                        publishStateList.add(publishState);
                        criteria.setPublishStates(publishStateList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (appSearchVO.getPublishStates() != null && !appSearchVO.getPublishStates().isEmpty()) {
                criteria.publishStates = appSearchVO.getPublishStates();
            }

            if (appSearchVO.getAppCategoryName() != null && !appSearchVO.getAppCategoryName().trim().equals("")) {
                //appCategory name
                JSONArray appCategoryJsonArray = new JSONArray(appSearchVO.getAppCategoryName());
                List<String> appCategoryList = new ArrayList<>();
                for (Object obj : appCategoryJsonArray) {
                    appCategoryList.add((String) obj);
                }
                criteria.appCategoryName = appCategoryList;
            }

            if (appSearchVO.getAppCategoryId() != null && !appSearchVO.getAppCategoryId().trim().equals("")) {
                //appCategory Id
                AppCategory appCategory = (AppCategory) session.get(AppCategory.class , Long.valueOf(appSearchVO.getAppCategoryId()));

                if (appCategory!=null) {
                    List<Long> appCategoryList = new ArrayList<>();
                    appCategoryList.add(appCategory.getId());
                    criteria.appCategoryId = appCategoryList;
                }
            }

            if (appSearchVO.getAppCategoryIds() != null && !appSearchVO.getAppCategoryIds().isEmpty()) {
                //appCategory Id
                JSONArray appCategoryJsonArray = new JSONArray(appSearchVO.getAppCategoryIds());
                List<Long> appCategoryList = new ArrayList<>();
                for (Object obj : appCategoryJsonArray) {
                    try {
                        appCategoryList.add(Long.valueOf(obj.toString()));
                    } catch (NumberFormatException e) {
                        //e.printStackTrace();
                    }
                }
                criteria.appCategoryId = appCategoryList;
            }
            if (appSearchVO.getDeveloperName() != null && !appSearchVO.getDeveloperName().trim().equals("")) {
                if (appSearchVO.getId() != null) {
                    App loadedApp = (App) session.load(App.class, appSearchVO.getId());
                    developerUser = loadedApp.getDeveloper();
                }
                if (currentUser != null && developerUser != null && currentUser.equals(developerUser)) {
                    //developer name
                    JSONArray developerJsonArray = new JSONArray(appSearchVO.getDeveloperName());
                    for (Object obj : developerJsonArray) {
                        developerNameList.add((String) obj);
                    }
                } else {
                    developerNameList.add(currentUser.getUserName());
                }
            } else if (currentUser != null) {
                if (!UserService.Instance.isUserRoot(currentUser)) {
                    developerNameList.add(currentUser.getUserName());
                }
            }
            criteria.developerName = developerNameList;
            if (appSearchVO.getCreatorUserName() != null && !appSearchVO.getCreatorUserName().trim().equals("")) {
                //creator user name
                if (currentUser != null && adminUser != null && currentUser.equals(adminUser)) {
                    JSONArray creatorUserNameJsonArray = new JSONArray(appSearchVO.getCreatorUserName());
                    for (Object obj : creatorUserNameJsonArray) {
                        creatorUserNameList.add((String) obj);
                    }
                } else {
                    creatorUserNameList.add(currentUser.getUserName());
                }
            } else if (currentUser != null) {
                if (!UserService.Instance.isUserRoot(currentUser)) {
                    creatorUserNameList.add(currentUser.getUserName());
                }
            }

            criteria.creatorUserName = creatorUserNameList;

            User requesterUser = PrincipalUtil.getCurrentUser();

            if (appSearchVO.getOsName() != null && !appSearchVO.getOsName().trim().equals("")) {
                //osName
                JSONArray osNameJsonArray = new JSONArray(appSearchVO.getOsName());
                List<String> osNameList = new ArrayList<>();
                for (Object obj : osNameJsonArray) {
                    osNameList.add((String) obj);
                }
                criteria.osName = osNameList;
            }

            criteria.setDeleted(false);
            criteria.setKeyword(appSearchVO.getKeywords());
            criteria.setId(appSearchVO.getId());
        } else {
            if (currentUser != null) {
                if (!UserService.Instance.isUserRoot(currentUser)) {
                    developerNameList.add(currentUser.getUserName());
                    creatorUserNameList.add(currentUser.getUserName());
                    criteria.creatorUserName = creatorUserNameList;
                    criteria.developerName = developerNameList;
                }
            }
        }

        return criteria;
    }


    public static AppSearchVO buildAppSearchVO(AppService.AppSearchResultModel appSearchResultModel) {
        AppSearchVO appSearchVO = new AppSearchVO();
        if (appSearchResultModel != null) {
            appSearchVO.setId(appSearchResultModel.getAppId());
            appSearchVO.setDeleted(appSearchResultModel.getDeleted());
            appSearchVO.setTitle(appSearchResultModel.getTitle());
            if (appSearchResultModel.getAppCategory() != null) {
                appSearchVO.setAppCategoryId(appSearchResultModel.getAppCategory().getId().toString());
                appSearchVO.setAppCategoryName(appSearchResultModel.getAppCategory().getCategoryName());
            }
            if (appSearchResultModel.getCreationDateTime() != null) {
                DateTime[] creationDateTime = new DateTime[1];
                creationDateTime[0] = appSearchResultModel.getCreationDateTime();
                appSearchVO.setCreationDateTime(creationDateTime);
            }
            if (appSearchResultModel.getOsId() != null) {
                appSearchVO.setOsId(appSearchResultModel.getOsId().toString());
            }
            if (appSearchResultModel.getOsName() != null) {
                appSearchVO.setOsName(appSearchResultModel.getOsName().toString());
            }
            if (appSearchResultModel.getOsType() != null) {
                appSearchVO.setOsTypeId(appSearchResultModel.getOsType().getId().toString());
                appSearchVO.setOsTypeName(appSearchResultModel.getOsType().getName());
            }
            appSearchVO.setDeveloperName(appSearchResultModel.getDeveloperName());
            if (appSearchResultModel.getCreatorUser() != null) {
                appSearchVO.setCreatorUserName(appSearchResultModel.getCreatorUser().getFullName());
            }
            appSearchVO.setPackageName(appSearchResultModel.getPackageName());
            appSearchVO.setDeveloperName(appSearchResultModel.getDeveloperName());
            if (appSearchResultModel.getPublishState() != null) {
                PublishState publishState = appSearchResultModel.getPublishState();
                if (publishState.equals(PublishState.PUBLISHED)) {
                    appSearchVO.setPublishState(Byte.valueOf("2").toString());
                } else if (publishState.equals(PublishState.UNPUBLISHED)) {
                    appSearchVO.setPublishState(Byte.valueOf("1").toString());
                }
            }
            appSearchVO.setVersionCode(appSearchResultModel.getApp_mainPack_versionCode());

            AppPackageVO appPackageVO = new AppPackageVO();
            appPackageVO.setId(appSearchResultModel.getApp_mainPack_id());
            appPackageVO.setIconFileKey(appSearchResultModel.getApp_mainPack_iconPath());
            appPackageVO.setIconFileName(appSearchResultModel.getApp_mainPack_iconFile());
            appPackageVO.setPackageFileKey(appSearchResultModel.getApp_mainPack_filePath());

            appSearchVO.setAppPackageVO(appPackageVO);
            appSearchVO.setKeywords(appSearchResultModel.getKeyword());
        }


        return appSearchVO;
    }


    public static AppSearchVO buildAppSearchVO(App app) {
        AppSearchVO appSearchVO = new AppSearchVO();
        if (app != null) {
            appSearchVO.setId(app.getId());
            appSearchVO.setDeleted(app.getDeleted());
            appSearchVO.setTitle(app.getTitle());
            if (app.getAppCategory() != null) {
                appSearchVO.setAppCategoryId(app.getAppCategory().getId().toString());
                appSearchVO.setAppCategoryName(app.getAppCategory().getCategoryName());
            }
            if (app.getCreationDate() != null) {
                DateTime[] creationDateTime = new DateTime[1];
                creationDateTime[0] = app.getCreationDate();
                appSearchVO.setCreationDateTime(creationDateTime);
            }
            if (app.getOs() != null) {
                appSearchVO.setOsId(app.getOs().getId().toString());
                appSearchVO.setOsName(app.getOs().getOsName());
            }
            if (app.getOsType() != null) {
                appSearchVO.setOsTypeId(app.getOsType().getId().toString());
                appSearchVO.setOsTypeName(app.getOsType().getName());
            }
            if (app.getDeveloper() != null) {
                appSearchVO.setDeveloperName(app.getDeveloper().getFullName());
            }
            if (app.getCreatorUser() != null) {
                appSearchVO.setCreatorUserName(app.getCreatorUser().getFullName());
            }
            appSearchVO.setPackageName(app.getPackageName());
            if (app.getMainPackage() != null) {
                if (app.getMainPackage().getPublishState() != null) {
                    PublishState publishState = app.getMainPackage().getPublishState();
                    if (publishState.equals(PublishState.PUBLISHED)) {
                        appSearchVO.setPublishState(Byte.valueOf("2").toString());
                    } else if (publishState.equals(PublishState.UNPUBLISHED)) {
                        appSearchVO.setPublishState(Byte.valueOf("1").toString());
                    }
                }
                appSearchVO.setVersionCode(app.getMainPackage().getVersion().toString());
                AppPackageVO appPackageVO = new AppPackageVO();
                if (app.getMainPackage().getIconFile() != null) {
                    appPackageVO.setIconFileKey(app.getMainPackage().getIconFile().getFilePath());
                    appPackageVO.setIconFileName(app.getMainPackage().getIconFile().getFileName());
                }
                if (app.getMainPackage().getPackFile() != null) {
                    appPackageVO.setPackageFileKey(app.getMainPackage().getPackFile().getFilePath());
                }
                appPackageVO.setId(app.getMainPackage().getId());
                appSearchVO.setAppPackageVO(appPackageVO);
            }

            AppElasticService.AppKeyWordCriteria appKeyWordCriteria = new AppElasticService.AppKeyWordCriteria();
            appKeyWordCriteria.setAppPackageName(app.getPackageName());
            appKeyWordCriteria.setOsTypeId(app.getOsType().getId());
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
            appSearchVO.setKeywords(keyWordList);

        }


        return appSearchVO;
    }

}
