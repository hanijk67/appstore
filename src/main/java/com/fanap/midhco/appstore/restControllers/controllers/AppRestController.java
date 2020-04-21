package com.fanap.midhco.appstore.restControllers.controllers;

import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.restControllers.vos.*;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppPackageHistoryService;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.app.IAPPPackageService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by A.Moshiri on 6/3/2018.
 */
@Controller
@RequestMapping("/service/app")
public class AppRestController {


    @RequestMapping(value = "/listApps", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO listApps(@RequestParam(required = false) AppSearchVO appSearchVO, HttpServletRequest request, HttpServletResponse response) {

        ResponseVO responseVO = null;
        Session session = null;
        try {
            Integer fromIndex = PrincipalUtil.getFromIndex();
            Integer countIndex = PrincipalUtil.getCountIndex();
            Boolean getResultCount = PrincipalUtil.getResultCount();
            Boolean isAscending = PrincipalUtil.isAscending();

            responseVO = new ResponseVO();
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            UserService.Instance.checkAccessibility(requesterUser, Access.APP_LIST, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            PrincipalUtil.setCurrentUser(requesterUser);

            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            List<AppService.AppSearchResultModel> appSearchResultModelList = new ArrayList<>();
            AppService.AppSearchCriteria appCriteria = new AppService.AppSearchCriteria();
            session = HibernateUtil.getCurrentSession();
            Object resultObject = null;

            appCriteria = AppSearchVO.createCriteriaFromJson(appSearchVO, session);

            boolean allFieldsNull = BaseEntityService.checkAllFieldsNull(appCriteria);

            if (allFieldsNull) {
                appCriteria = new AppService.AppSearchCriteria();
            }

            String sortBy = PrincipalUtil.getSortBy();
            String sortProperties = null;
            if (sortBy != null) {
                try {
                    if (sortBy.equals("publishState")) {
                        sortProperties = "mainPackage.publishState";
                    } else if (sortBy.equals("versionCode")) {
                        sortProperties = "mainPackage.versionCode";
                    } else if (sortBy.equals("versionName")) {
                        sortProperties = "mainPackage.versionName";
                    } else {
                        List<String> declaredField = new App().getDeclaredField();
                        if (declaredField.contains(sortBy)) {
                            sortProperties = "app." + sortBy;
                        }
                    }
                } catch (Exception e) {
                    sortProperties = null;
                }
            }
            if (appCriteria.getPublishStates() == null) {
                List<PublishState> publishStates = new ArrayList<>();
                publishStates.add(PublishState.PUBLISHED);
                appCriteria.setPublishStates(publishStates);
            }
            if (getResultCount) {
                Long resultCount = AppService.Instance.count(appCriteria, session);
                resultObject = resultCount;
            } else {
                appSearchResultModelList = AppService.Instance.list(appCriteria, fromIndex, countIndex, sortProperties, isAscending, session);

                if (appSearchResultModelList != null && !appSearchResultModelList.isEmpty()) {
                    Stream<AppSearchVO> appUploadVOStream = appSearchResultModelList.stream().map(appSearchResultModelInList -> AppSearchVO.buildAppSearchVO(appSearchResultModelInList));
                    resultObject = appUploadVOStream.collect(Collectors.<AppSearchVO>toList());

                } else {
                    resultObject = new ArrayList<RoleVO>();
                }
            }

            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult(JsonUtil.getJson(resultObject));
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;

    }

    @RequestMapping(value = "/addApp", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO addApp(@RequestParam(required = true) AppUploadVO appUploadVO, HttpServletRequest request, HttpServletResponse response) {

        ResponseVO responseVO = new ResponseVO();
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (appUploadVO != null ) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
            } else {
                UserService.Instance.checkAccessibility(requesterUser, Access.APP_ADD, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);
                responseVO = saveAppToDataBase(appUploadVO);
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }

    @RequestMapping(value = "editApp", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO editApp(@RequestParam(required = true) AppUploadVO appUploadVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (appUploadVO == null || appUploadVO.getId() == null) {
                responseVO.setResultStatus(ResultStatus.NULL_DATA);
                responseVO.setResult(ResultStatus.NULL_DATA.toString());
            } else {
                try {
                    PrincipalUtil.setCurrentUser(requesterUser);
                    OSType osType = PrincipalUtil.getCurrentOSTYPE();
                    if(osType==null){
                        responseVO.setResult(ResultStatus.OS_TYPE_NOT_FOUND.toString());
                        responseVO.setResultStatus(ResultStatus.OS_TYPE_NOT_FOUND);
                        return responseVO;
                    }
                    appUploadVO.setOsType(osType);
                    responseVO = checkInputVo(appUploadVO, requesterUser);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }

                    UserService.Instance.checkAccessibility(requesterUser, Access.APP_EDIT, responseVO);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    responseVO = saveAppToDataBase(appUploadVO);
                } catch (Exception e) {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult(e.getMessage());
                }
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }

    @RequestMapping(value = "addPackage", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO addPackage(@RequestParam(required = true) AppUploadVO appUploadVO,   @RequestParam(required = true) boolean usePreviousFile, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (appUploadVO == null ) {
                responseVO.setResultStatus(ResultStatus.NULL_DATA);
                responseVO.setResult(ResultStatus.NULL_DATA.toString());
            } else {
                try {

                    OSType osType = PrincipalUtil.getCurrentOSTYPE();
                    if(osType==null){
                        responseVO.setResult(ResultStatus.OS_TYPE_NOT_FOUND.toString());
                        responseVO.setResultStatus(ResultStatus.OS_TYPE_NOT_FOUND);
                        return responseVO;
                    }
                    appUploadVO.setOsType(osType);
                    PrincipalUtil.setCurrentUser(requesterUser);

                    responseVO = checkInputVo(appUploadVO, requesterUser);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    UserService.Instance.checkAccessibility(requesterUser, Access.APP_ADD_PACKAGE, responseVO);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    responseVO = addOrReplacePackageToApp(appUploadVO, false, false,usePreviousFile);
                } catch (Exception e) {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult(e.getMessage());
                }
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }

    @RequestMapping(value = "replacePackage", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO replacePackage(@RequestParam(required = true) AppUploadVO appUploadVO,@RequestParam(required = true) boolean isMainPackage,
                                     @RequestParam(required = true) boolean usePreviousFile, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (appUploadVO == null) {
                responseVO.setResultStatus(ResultStatus.NULL_DATA);
                responseVO.setResult(ResultStatus.NULL_DATA.toString());
            } else {
                try {
                    OSType osType = PrincipalUtil.getCurrentOSTYPE();
                    if(osType==null){
                        responseVO.setResult(ResultStatus.OS_TYPE_NOT_FOUND.toString());
                        responseVO.setResultStatus(ResultStatus.OS_TYPE_NOT_FOUND);
                        return responseVO;
                    }
                    appUploadVO.setOsType(osType);
                    PrincipalUtil.setCurrentUser(requesterUser);

                    responseVO = checkInputVo(appUploadVO, requesterUser);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    UserService.Instance.checkAccessibility(requesterUser, Access.APP_ADD_PACKAGE, responseVO);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    responseVO = addOrReplacePackageToApp(appUploadVO, true,isMainPackage,usePreviousFile);
                } catch (Exception e) {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult(e.getMessage());
                }
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }

    @RequestMapping(value = "/publishAppPackage", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO publishAppPackage(@RequestParam(required = true) PackageVO packageVO, @RequestParam(required = true) boolean isMainPackage,
                                        HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        Transaction tx = null;
        try {
            OSType osType = PrincipalUtil.getCurrentOSTYPE();
            if (osType == null) {
                responseVO.setResultStatus(ResultStatus.OS_TYPE_NOT_FOUND);
                responseVO.setResult(ResultStatus.OS_TYPE_NOT_FOUND.toString());
                return responseVO;
            }

            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            session = HibernateUtil.getCurrentSession();

            UserService.Instance.checkAccessibility(requesterUser, Access.APP_PUBLISH_UNPUBLISH, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            User user = UserService.Instance.getUserBySSOId(requesterUser.getUserId(), session);
            if (user == null) {
                responseVO.setResultStatus(ResultStatus.USER_NOT_IN_SSO);
                return responseVO;
            }

            PrincipalUtil.setCurrentUser(user);


            responseVO = AppPackageService.Instance.checkPackageInfoForPublish(packageVO, isMainPackage);

            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
            appSearchCriteria.setPackageName(packageVO.getAppPackageName());
            appSearchCriteria.setDeleted(false);
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            appSearchCriteria.setOsType(osTypeList);

            Boolean checkExistUnDeletedApp = AppService.Instance.checkExistUnDeletedApp(appSearchCriteria, session);
            if (!checkExistUnDeletedApp) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                return responseVO;
            }

            AppPackageService.Criteria criteria = new AppPackageService.Criteria();
            criteria.versionName = packageVO.getVersionName();
            criteria.osType = osType;
            criteria.versionCode = packageVO.getVersionCode();
            criteria.isDeleted = false;

            List<AppPackageService.AppPackageSearchResult> resultList = AppPackageService.Instance.list(criteria, 0, -1, session);
            AppPackage appPackage = null;
            AppPackageService.AppPackageSearchResult appPackageSearchResult = null;
            App loadedApp = null;


            if (isMainPackage) {
                AppService.AppSearchResultModel loadedAppSearchResultModel = AppService.Instance.list(appSearchCriteria, 0, -1, null, false, session).get(0);
                loadedApp = (App) session.load(App.class, loadedAppSearchResultModel.getAppId());
                if (loadedApp != null) {
                    appPackage = loadedApp.getMainPackage();
                }
                if (appPackage == null) {
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    return responseVO;
                }
            } else if (resultList == null || resultList.isEmpty() || resultList.get(0).getAppPackage() == null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                return responseVO;
            } else {
                appPackageSearchResult = resultList.get(0);
                appPackage = appPackageSearchResult.getAppPackage();
            }
            tx = session.beginTransaction();

            PublishState publishState = appPackage.getPublishState();

            appPackage = AppPackageHistoryService.Instance.setAppPackageHistoryForAppPackage(session, appPackage);
            PublishState neededState = new PublishState(Byte.valueOf(packageVO.getPublishStateStr()));
            appPackage.setPublishState(neededState);
            Boolean publishPackage = neededState.equals(PublishState.PUBLISHED) ? true : false;
            if (isMainPackage) {
                AppPackageService.Instance.publishUnPublishPackage(session, loadedApp, appPackage, publishState, publishPackage);
            } else {
                AppPackageService.Instance.publishUnPublishPackage(session, appPackageSearchResult, appPackage, publishState, publishPackage);
            }
            tx.commit();


            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult("appPackageState changed successfully!");

        } catch (Exception ex) {
            if (tx != null)
                tx.rollback();

            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(ex.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }


    @RequestMapping(value = "/changePackageIcon", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO changePackageIcon(@RequestParam(required = true) PackageVO packageVO, @RequestParam(required = true) boolean isMainPackage,
                                        HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        Transaction tx = null;
        try {
            OSType osType = PrincipalUtil.getCurrentOSTYPE();
            if (osType == null) {
                responseVO.setResultStatus(ResultStatus.OS_TYPE_NOT_FOUND);
                responseVO.setResult(ResultStatus.OS_TYPE_NOT_FOUND.toString());
                return responseVO;
            }

            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            session = HibernateUtil.getCurrentSession();

            UserService.Instance.checkAccessibility(requesterUser, Access.Edit_APP_PACKAGE, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            User user = UserService.Instance.getUserBySSOId(requesterUser.getUserId(), session);
            if (user == null) {
                responseVO.setResultStatus(ResultStatus.USER_NOT_IN_SSO);
                return responseVO;
            }

            PrincipalUtil.setCurrentUser(user);


            responseVO = AppPackageService.Instance.checkPackageInfoForChangeIcon(packageVO, isMainPackage);

            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }


            AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
            appSearchCriteria.setPackageName(packageVO.getAppPackageName());
            appSearchCriteria.setDeleted(false);
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            appSearchCriteria.setOsType(osTypeList);

            Boolean checkExistUnDeletedApp = AppService.Instance.checkExistUnDeletedApp(appSearchCriteria, session);
            if (!checkExistUnDeletedApp) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                return responseVO;
            }

            AppPackageService.Criteria criteria = new AppPackageService.Criteria();
            criteria.versionName = packageVO.getVersionName();
            criteria.osType = osType;
            criteria.versionCode = packageVO.getVersionCode();
            criteria.isDeleted = false;

            List<AppPackageService.AppPackageSearchResult> resultList = AppPackageService.Instance.list(criteria, 0, -1, session);
            AppPackage appPackage = null;
            AppPackageService.AppPackageSearchResult appPackageSearchResult = null;
            App loadedApp = null;


            if (isMainPackage) {
                AppService.AppSearchResultModel loadedAppSearchResultModel = AppService.Instance.list(appSearchCriteria, 0, -1, null, false, session).get(0);
                loadedApp = (App) session.load(App.class, loadedAppSearchResultModel.getAppId());
                if (loadedApp != null) {
                    appPackage = loadedApp.getMainPackage();
                }
                if (appPackage == null) {
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    return responseVO;
                }
            } else if (resultList == null || resultList.isEmpty() || resultList.get(0).getAppPackage() == null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                return responseVO;
            } else {
                appPackageSearchResult = resultList.get(0);
                appPackage = appPackageSearchResult.getAppPackage();
            }
            tx = session.beginTransaction();


            appPackage = AppPackageHistoryService.Instance.setAppPackageHistoryForAppPackage(session, appPackage);
            String iconFileKey = packageVO.getIconFileKey();
            String iconFileName = FileServerService.Instance.getFileNameFromFilePath(iconFileKey);
            File iconFile = new File();
            iconFile.setStereoType(StereoType.ICON_FILE);
            iconFile.setFileName(iconFileName);
            iconFile.setFilePath(iconFileKey);
            appPackage.setIconFile(iconFile);
            BaseEntityService.Instance.saveOrUpdate(iconFile, session);
            if (!FileServerService.Instance.doesFileExistOnFileServer(iconFileKey))
                FileServerService.Instance.persistFileToServer(iconFileKey);


            AppPackageService.Instance.saveOrUpdate(appPackage, session);

            tx.commit();


            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult("icon changed successfully!");

        } catch (Exception ex) {
            if (tx != null)
                tx.rollback();

            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(ex.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }

    @RequestMapping(value = "/changePackageImages", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO changePackageImages(@RequestParam(required = true) PackageVO packageVO, @RequestParam(required = true) boolean isMainPackage,
                                          HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        Transaction tx = null;
        try {
            OSType osType = PrincipalUtil.getCurrentOSTYPE();
            if (osType == null) {
                responseVO.setResultStatus(ResultStatus.OS_TYPE_NOT_FOUND);
                responseVO.setResult(ResultStatus.OS_TYPE_NOT_FOUND.toString());
                return responseVO;
            }

            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            session = HibernateUtil.getCurrentSession();

            UserService.Instance.checkAccessibility(requesterUser, Access.Edit_APP_PACKAGE, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            User user = UserService.Instance.getUserBySSOId(requesterUser.getUserId(), session);
            if (user == null) {
                responseVO.setResultStatus(ResultStatus.USER_NOT_IN_SSO);
                return responseVO;
            }

            PrincipalUtil.setCurrentUser(user);


            responseVO = AppPackageService.Instance.checkPackageInfoForChangeImages(packageVO, isMainPackage);

            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }


            AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
            appSearchCriteria.setPackageName(packageVO.getAppPackageName());
            appSearchCriteria.setDeleted(false);
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            appSearchCriteria.setOsType(osTypeList);

            Boolean checkExistUnDeletedApp = AppService.Instance.checkExistUnDeletedApp(appSearchCriteria, session);
            if (!checkExistUnDeletedApp) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                return responseVO;
            }

            AppPackageService.Criteria criteria = new AppPackageService.Criteria();
            criteria.versionName = packageVO.getVersionName();
            criteria.osType = osType;
            criteria.versionCode = packageVO.getVersionCode();
            criteria.isDeleted = false;

            List<AppPackageService.AppPackageSearchResult> resultList = AppPackageService.Instance.list(criteria, 0, -1, session);
            AppPackage appPackage = null;
            AppPackageService.AppPackageSearchResult appPackageSearchResult = null;
            App loadedApp = null;


            if (isMainPackage) {
                AppService.AppSearchResultModel loadedAppSearchResultModel = AppService.Instance.list(appSearchCriteria, 0, -1, null, false, session).get(0);
                loadedApp = (App) session.load(App.class, loadedAppSearchResultModel.getAppId());
                if (loadedApp != null) {
                    appPackage = loadedApp.getMainPackage();
                }
                if (appPackage == null) {
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    return responseVO;
                }
            } else if (resultList == null || resultList.isEmpty() || resultList.get(0).getAppPackage() == null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                return responseVO;
            } else {
                appPackageSearchResult = resultList.get(0);
                appPackage = appPackageSearchResult.getAppPackage();
            }
            tx = session.beginTransaction();


            appPackage = AppPackageHistoryService.Instance.setAppPackageHistoryForAppPackage(session, appPackage);
            List<File> thumbFiles = AppPackageService.Instance.setImagesToPackage(packageVO.getImagesFileKey(), session);
            appPackage.setThumbImages(thumbFiles);


            AppPackageService.Instance.saveOrUpdate(appPackage, session);

            tx.commit();


            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult("icon changed successfully!");

        } catch (Exception ex) {
            if (tx != null)
                tx.rollback();

            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(ex.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }

    private ResponseVO addOrReplacePackageToApp(AppUploadVO appUploadVO, boolean forReplace, boolean isMainPackage ,boolean usePreviousFile) {

        ResponseVO responseVO = new ResponseVO();
        if (appUploadVO != null) {
            Session session = null;
            try {
                OSType osType = PrincipalUtil.getCurrentOSTYPE();
                if (osType == null) {
                    responseVO.setResultStatus(ResultStatus.OS_TYPE_NOT_FOUND);
                    responseVO.setResult(ResultStatus.OS_TYPE_NOT_FOUND.toString());
                    return responseVO;
                }

                session = HibernateUtil.getNewSession();

                responseVO = AppService.Instance.checkValidDataForAddOrReplacePackage(appUploadVO, session,forReplace, isMainPackage,usePreviousFile);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }

                AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
                appSearchCriteria.setPackageName(appUploadVO.getPackageName());

                appSearchCriteria.setDeleted(false);
                List<OSType> osTypeList = new ArrayList<>();
                osTypeList.add(appUploadVO.getOsType());
                appSearchCriteria.setOsType(osTypeList);

                App loadedApp = null;
                List<AppService.AppSearchResultModel> appSearchResultModelList = AppService.Instance.list(appSearchCriteria , 0 , -1 ,null, false,session);

                loadedApp = (App) session.get(App.class , appSearchResultModelList.get(0).getAppId());

                if (appUploadVO.getPackageName() != null && !appUploadVO.getPackageName().trim().equals("")) {
                    loadedApp.setPackageName(appUploadVO.getPackageName());
                }
                Transaction tx = session.beginTransaction();
                Map<String, String> thumbImagesMap = new HashMap<>();
                if (usePreviousFile) {
                    PackageVO appPackageVO = AppPackageService.Instance.getPreviousPackageFiles(loadedApp,session);
                    if(appPackageVO==null || appPackageVO.getThumbFiles()==null || appPackageVO.getThumbFiles().isEmpty()){
                        responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                        return responseVO;
                    }else {
                        for(File thumbFile : appPackageVO.getThumbFiles()){
                            thumbImagesMap.put(thumbFile.getFilePath(), thumbFile.getFileName());
                        }
                    }
                }else {
                    if (appUploadVO.getAppPackageVO() != null && appUploadVO.getAppPackageVO().getPackageImagesKeys() != null) {
                        for (String packageKey : appUploadVO.getAppPackageVO().getPackageImagesKeys()) {
                            String fileName = FileServerService.Instance.getFileNameFromFilePath(packageKey);
                            if (fileName != null && !fileName.trim().equals("")) {
                                thumbImagesMap.put(packageKey, fileName);
                            }
                        }
                    }
                }
                String appPackageFileName = null;
                String appPackageFileKey = null;
                String iconFileName = null;
                String iconFileKey = null;
                String versionCode = null;
                String packageName = null;

                AppPackage loadedAppPackage = null;

                if (appUploadVO.getAppPackageVO() != null) {
                    appPackageFileName = appUploadVO.getAppPackageVO().getPackageFileName();
                    appPackageFileKey = appUploadVO.getAppPackageVO().getPackageFileKey();
                    if (usePreviousFile){
                        PackageVO appPackageVO = AppPackageService.Instance.getPreviousPackageFiles(loadedApp,session);
                        if(appPackageVO==null || appPackageVO.getIconFile()==null ){
                            responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                            responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                            return responseVO;
                        }else {
                            iconFileKey = appPackageVO.getIconFile().getFilePath();
                            iconFileName = appPackageVO.getIconFile().getFileName();
                        }
                    }
                    else{
                        iconFileName = appUploadVO.getAppPackageVO().getIconFileName();
                        iconFileKey = appUploadVO.getAppPackageVO().getIconFileKey();
                    }
                    IAPPPackageService iappPackageService =
                            AppPackageService.Instance.processPackageFile(appPackageFileKey, loadedApp.getOsType());
                    versionCode = iappPackageService.getVersionCode();
                    packageName = iappPackageService.getPackage();

                    if (packageName == null) {
                        responseVO.setResult(AppStorePropertyReader.getString("appPackage.replace.package.has.null.packageName"));
                        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                        return responseVO;
                    } else if (!packageName.equals(loadedApp.getPackageName())) {
                        responseVO.setResult(AppStorePropertyReader.getString("appPackage.replace.package.has.different.name"));
                        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                        return responseVO;
                    }

                    AppPackageService.Criteria criteria = new AppPackageService.Criteria();
                    criteria.versionName = appUploadVO.getAppPackageVO().getVersionName();
                    criteria.osType = osType;
                    criteria.versionCode = appUploadVO.getAppPackageVO().getVersionCode();
                    criteria.isDeleted = false;

                    List<AppPackageService.AppPackageSearchResult> resultList = AppPackageService.Instance.list(criteria, 0, -1, session);
                    AppPackageService.AppPackageSearchResult appPackageSearchResult = null;


                    if (isMainPackage) {
                        AppService.AppSearchResultModel loadedAppSearchResultModel = AppService.Instance.list(appSearchCriteria, 0, -1, null, false, session).get(0);
                        loadedApp = (App) session.load(App.class, loadedAppSearchResultModel.getAppId());
                        if (loadedApp != null) {
                            loadedAppPackage = loadedApp.getMainPackage();
                        }
                        if (loadedAppPackage == null) {
                            responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                            responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                            return responseVO;
                        }
                    } else if (resultList == null || resultList.isEmpty() || resultList.get(0).getAppPackage() == null) {
                        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                        responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                        return responseVO;
                    } else {
                        appPackageSearchResult = resultList.get(0);
                        loadedAppPackage = appPackageSearchResult.getAppPackage();
                    }

                    if (loadedAppPackage == null) {
                        responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                        return responseVO;
                    }

                    if (forReplace) {

                        if (versionCode == null) {
                            responseVO.setResult(AppStorePropertyReader.getString("appPackage.replace.versionCode.is.null"));
                            responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                            return responseVO;
                        } else if (!versionCode.equals(loadedAppPackage.getVersionCode())) {
                            responseVO.setResult(AppStorePropertyReader.getString("appPackage.replace.versionCode.is.different.from.original"));
                            responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                            return responseVO;

                        }
                    } else if (Long.valueOf(versionCode) <= Long.valueOf(loadedAppPackage.getVersionCode())) {
                        responseVO.setResult(AppStorePropertyReader.getString("error.appPackage.add.sameVersion"));
                        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                        return responseVO;
                    } else if (AppPackageService.Instance.hasAppWithVersionCode(loadedApp.getPackageName(), iappPackageService.getVersionCode(), loadedApp.getOsType(), session)) {
                        responseVO.setResult(AppStorePropertyReader.getString("App.has.package.with.same.versionCode.as.new.package"));
                        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                        return responseVO;

                    }
                }
                if (forReplace) {
                    String changeLog = null;
                    if (appUploadVO.getAppPackageVO().getChangesLog() != null && !appUploadVO.getAppPackageVO().getChangesLog().trim().equals("")) {
                        changeLog =appUploadVO.getAppPackageVO().getChangesLog();
                    }
                    AppPackageService.Instance.replacePackage(loadedAppPackage, appPackageFileKey, appPackageFileName, iconFileKey, iconFileName, thumbImagesMap,changeLog, session);
                } else {
                    AppService.Instance.savePackageForApp(
                            loadedApp,
                            appPackageFileName,
                            appPackageFileKey,
                            iconFileName,
                            iconFileKey,
                            thumbImagesMap,
                            loadedApp.getOsType(),
                            loadedApp.getShortDescription(),
                            loadedApp.getDescription(),
                            loadedApp.getTitle(),
                            loadedApp.getAppCategory().getId(),
                            String.valueOf(loadedApp.getMainPackage().getPublishState().getState()),
                            forReplace,
                            session
                    );
                }
                tx.commit();
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                AppSearchVO convertedAppSearchVO = AppSearchVO.buildAppSearchVO(loadedApp);
                String roleString = JsonUtil.getJson(convertedAppSearchVO);
                roleString.replaceAll("\\\\", "");
                String replaceStr = roleString.replaceAll("\\\\", "").replaceAll("\\\"", "\"");
                responseVO.setResult(replaceStr);

            } catch (Exception e) {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(e.getMessage());
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
        return responseVO;
    }

    private ResponseVO checkInputVo(AppUploadVO appUploadVO, User requesterUser) {
        ResponseVO responseVO = new ResponseVO();

        responseVO.setResult(ResultStatus.UNSUCCESSFUL.toString());
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        if (appUploadVO != null ) {
            try {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                if (appUploadVO.getPackageName()==null || appUploadVO.getPackageName().trim().equals("")){
                    return responseVO;
                }
                Session session = HibernateUtil.getCurrentSession();
                AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
                appSearchCriteria.setPackageName(appUploadVO.getPackageName());

                appSearchCriteria.setDeleted(false);
                List<OSType> osTypeList = new ArrayList<>();
                osTypeList.add(appUploadVO.getOsType());
                appSearchCriteria.setOsType(osTypeList);

                Boolean checkExistUnDeletedApp = AppService.Instance.checkExistUnDeletedApp(appSearchCriteria, session);
                if (!checkExistUnDeletedApp) {
                    return responseVO;
                }
                App loadedApp = null;
                List<AppService.AppSearchResultModel> appSearchResultModelList = AppService.Instance.list(appSearchCriteria , 0 , -1 ,null, false,session);
                if(appSearchResultModelList==null || appSearchResultModelList.isEmpty()){
                    return responseVO;
                }
                loadedApp = (App) session.get(App.class , appSearchResultModelList.get(0).getAppId());
                if (loadedApp == null) {
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    responseVO.setResult(ResultStatus.INVALID_ROLE.toString());
                    return responseVO;
                }
                if (loadedApp.getDeveloper() != null && !loadedApp.getDeveloper().equals(requesterUser) && !UserService.Instance.isUserRoot(requesterUser)) {
                    responseVO.setResultStatus(ResultStatus.NOT_APP_DEVELOPER);
                    responseVO.setResult(ResultStatus.NOT_APP_DEVELOPER.toString());
                    return responseVO;
                }
                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

            } catch (Exception e) {
                responseVO.setResult(e.getMessage());
                return responseVO;
            }
        }
        return responseVO;
    }


    private ResponseVO saveAppToDataBase(AppUploadVO appUploadVO) {
        ResponseVO responseVO = new ResponseVO();
        if (appUploadVO != null) {
            Session session = null;
            try {
                User currentUser = PrincipalUtil.getCurrentUser();
                session = HibernateUtil.getNewSession();
                responseVO = AppService.Instance.checkValidData(appUploadVO, session);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                App app = null;
                if (appUploadVO.getId() != null) {
                    app = (App) session.load(App.class, appUploadVO.getId());
                } else {
                    app = new App();
                    app.setDeveloper(currentUser);
                }
                if (appUploadVO.getPackageName() == null || appUploadVO.getPackageName().trim().equals("")) {
                    if (appUploadVO.getId() == null) {
                        responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                        return responseVO;
                    } else {
                        App loadedApp = (App) session.get(App.class, appUploadVO.getId());
                        if (loadedApp == null) {
                            responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                            responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                            return responseVO;
                        } else {
                            appUploadVO.setPackageName(app.getPackageName());
                        }
                    }
                }
                AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
                appSearchCriteria.setPackageName(appUploadVO.getPackageName());
                appSearchCriteria.setId(appUploadVO.getId());

                appSearchCriteria.setDeleted(false);
                if (appUploadVO.getOsId() != null) {
                    OS os = (OS) session.get(OS.class, Long.valueOf(appUploadVO.getOsId()));
                    OSType osType = os.getOsType();
                    List<OSType> osTypeList = new ArrayList<>();
                    osTypeList.add(osType);
                    appSearchCriteria.setOsType(osTypeList);
                }
                Boolean checkExistUnDeletedApp = AppService.Instance.checkExistUnDeletedApp(appSearchCriteria, session);
                if (checkExistUnDeletedApp == null) {
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                } else if (checkExistUnDeletedApp) {
                    responseVO.setResult(ResultStatus.UNDELETED_APP_FOUND.toString());
                    responseVO.setResultStatus(ResultStatus.UNDELETED_APP_FOUND);
                    return responseVO;
                }

                if (appUploadVO.getTitle() != null && !appUploadVO.getTitle().trim().equals("")) {
                    app.setTitle(appUploadVO.getTitle());
                }
                if (appUploadVO.getOsId() != null) {
                    OS loadedOs = (OS) session.load(OS.class, appUploadVO.getOsId());
                    app.setOs(loadedOs);
                    app.setOsType(loadedOs.getOsType());
                }

                if (appUploadVO.getPackageName() != null && !appUploadVO.getPackageName().trim().equals("")) {
                    app.setPackageName(appUploadVO.getPackageName());
                }
                if (appUploadVO.getCategoryId() != null) {
                    AppCategory loadedCategory = (AppCategory) session.load(AppCategory.class, appUploadVO.getCategoryId());
                    app.setAppCategory(loadedCategory);
                }
                if (appUploadVO.getDescription() != null && !appUploadVO.getDescription().trim().equals("")) {
                    app.setDescription(appUploadVO.getDescription());
                }

                if (appUploadVO.getShortDescription() != null && !appUploadVO.getShortDescription().trim().equals("")) {
                    app.setShortDescription(appUploadVO.getShortDescription());
                }
                if (appUploadVO.getDeveloperUserId() != null) {
                    User developerUser = (User) session.load(User.class, appUploadVO.getDeveloperUserId());
                    app.setDeveloper(developerUser);
                }

                Transaction tx = session.beginTransaction();
                Map<String, String> thumbImagesMap = new HashMap<>();
                int counter = 0;
                if (appUploadVO.getAppPackageVO() != null && appUploadVO.getAppPackageVO().getPackageImagesKeys() != null) {
                    for (String packageKey : appUploadVO.getAppPackageVO().getPackageImagesKeys()) {
                        thumbImagesMap.put(packageKey, appUploadVO.getAppPackageVO().getPackageImageNames().get(counter));
                        counter++;
                    }
                }
                String appPackageFileName = null;
                String appPackageFileKey = null;
                String iconFileName = null;
                String iconFileKey = null;
                String versionCode = null;

                if (appUploadVO.getAppPackageVO() != null) {
                    appPackageFileName = appUploadVO.getAppPackageVO().getPackageFileName();
                    appPackageFileKey = appUploadVO.getAppPackageVO().getPackageFileKey();
                    iconFileName = appUploadVO.getAppPackageVO().getIconFileName();
                    iconFileKey = appUploadVO.getAppPackageVO().getIconFileKey();
                    IAPPPackageService iappPackageService =
                            AppPackageService.Instance.processPackageFile(appPackageFileKey, app.getOsType());
                    versionCode = iappPackageService.getVersionCode();
                }

                String publishStr =  (appUploadVO.getPublishState()!=null && !appUploadVO.getPublishState().trim().equals("") )?appUploadVO.getPublishState(): String.valueOf(PublishState.UNPUBLISHED.getState());

                if (app.getMainPackage() != null && app.getMainPackage().getVersionCode().equals(versionCode)) {
                    AppService.Instance.savePackageForApp(
                            app,
                            appPackageFileName,
                            appPackageFileKey,
                            iconFileName,
                            iconFileKey,
                            thumbImagesMap,
                            app.getOsType(),
                            appUploadVO.getShortDescription(),
                            appUploadVO.getDescription(),
                            appUploadVO.getTitle(),
                            null,
                            appUploadVO.getCategoryId(),
                            publishStr,
                            true,
                            session
                    );
                } else {
                    AppService.Instance.savePackageForApp(
                            app,
                            appPackageFileName,
                            appPackageFileKey,
                            iconFileName,
                            iconFileKey,
                            thumbImagesMap,
                            app.getOsType(),
                            appUploadVO.getShortDescription(),
                            appUploadVO.getDescription(),
                            appUploadVO.getTitle(),
                            appUploadVO.getCategoryId(),
                            publishStr,
                            false,
                            session
                    );
                }


                tx.commit();
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                AppSearchVO convertedAppSearchVO = AppSearchVO.buildAppSearchVO(app);
                String roleString = JsonUtil.getJson(convertedAppSearchVO);
                roleString.replaceAll("\\\\", "");
                String replaceStr = roleString.replaceAll("\\\\", "").replaceAll("\\\"", "\"");
                responseVO.setResult(replaceStr);

            } catch (Exception e) {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(e.getMessage());
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }

        return responseVO;
    }

}
