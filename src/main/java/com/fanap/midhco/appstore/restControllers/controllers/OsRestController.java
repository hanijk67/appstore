package com.fanap.midhco.appstore.restControllers.controllers;

import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.exceptions.PreProcessorException;
import com.fanap.midhco.appstore.restControllers.vos.*;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.handlerApp.HandlerAppService;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by A.Moshiri on 4/10/2018.
 */
@RestController
@RequestMapping("/service/os")
public class OsRestController {
    static private final Logger logger = Logger.getLogger(OsRestController.class);

    @RequestMapping(value = "listOs", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO listOs(@RequestParam(required = false) OSVO osVO,HttpServletRequest request, HttpServletResponse response) {

        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();

                User requesterUser = null;
                requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
                if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                UserService.Instance.checkAccessibility(requesterUser, Access.OS_LIST, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);



            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            List<OS> osList = new ArrayList<>();
            OSService.OSCriteria osCriteria = new OSService.OSCriteria();
            Integer fromIndex = PrincipalUtil.getFromIndex();
            Integer countIndex = PrincipalUtil.getCountIndex();
            Boolean getResultCount = PrincipalUtil.getResultCount();
            Boolean isAscending = PrincipalUtil.isAscending();

            Object resultObject = null;

            if (osVO != null) {
                boolean hasValue = false;
                if (osVO.getOsId() != null) {
                    osCriteria.setOsId(osVO.getOsId());
                    hasValue = true;
                }
                if (osVO.getOsName() != null && !osVO.getOsName().trim().equals("")) {
                    osCriteria.setOsName(osVO.getOsName().trim());
                    hasValue = true;
                }

                if (osVO.getOsCode() != null && !osVO.getOsCode().trim().equals("")) {
                    osCriteria.setOsCode(osVO.getOsCode().trim());
                    hasValue = true;
                }

                if (osVO.getOsTypes() != null) {
                    List<OSType> osTypeList = new ArrayList<>(osVO.getOsTypes());
                    osCriteria.setOsType(osTypeList);
                    hasValue = true;
                }
                if (osVO.getOsType() != null) {
                    List<OSType> osTypeList = (osVO.getOsTypes() != null) ? new ArrayList<>(osVO.getOsTypes()) : new ArrayList<>();
                    osTypeList.add(osVO.getOsType());
                    osCriteria.setOsType(osTypeList);
                    hasValue = true;
                }
                if (osVO.getOsVersion() != null && !osVO.getOsVersion().trim().equals("")) {
                    osCriteria.setOsVersion(osVO.getOsVersion().trim());
                    hasValue = true;
                }

                if (osVO.getDisabled() != null) {
                    osCriteria.setDisabled(osVO.getDisabled());
                }
                if (!hasValue) {
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    responseVO.setResult("");
                    return responseVO;
                }
            }

            String sortBy = PrincipalUtil.getSortBy();
            String sortProperties = null;
            if (sortBy != null) {
                try {
                    List<String> declaredField = new OS().getDeclaredField();
                    if (declaredField.contains(sortBy)) {
                        sortProperties = sortBy;
                    }
                } catch (Exception e) {
                    sortProperties = null;
                }
            }
            if (getResultCount) {
                Long resultCount = OSService.Instance.count(osCriteria, session);
                resultObject = resultCount;
            } else {
                osList = OSService.Instance.list(osCriteria, fromIndex, countIndex, sortProperties, isAscending, session);
                if (osList != null && !osList.isEmpty()) {
                    Stream<OSVO> osVOStream = osList.stream().map(os -> OSVO.buildOSVO(os ));
                    resultObject = osVOStream.collect(Collectors.<OSVO>toList());
                } else {
                    resultObject = new ArrayList<OSVO>();
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


    @RequestMapping(value = "addOs", headers = "accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO addOs(@RequestParam(required = true) OSVO osVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (osVO != null && osVO.getOsId() != null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
            } else {
                UserService.Instance.checkAccessibility(requesterUser, Access.OS_ADD, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);
                responseVO = saveOsToDataBase(osVO);
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }


    @RequestMapping(value = "editOs", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO editOs(@RequestParam(required = true) OSVO osVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();

        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (osVO == null || osVO.getOsId() == null) {
                responseVO.setResultStatus(ResultStatus.NULL_DATA);
                responseVO.setResult(ResultStatus.NULL_DATA.toString());
            } else {
                Long osId = osVO.getOsId();

                try {
                    PrincipalUtil.setCurrentUser(requesterUser);
                    Session session = HibernateUtil.getCurrentSession();
                    OS loadedOs = (OS) session.get(OS.class, osId);
                    if (loadedOs == null) {
                        responseVO.setResultStatus(ResultStatus.OS_NOT_FOUND);
                        responseVO.setResult(ResultStatus.OS_NOT_FOUND.toString());
                        return responseVO;
                    }
                    UserService.Instance.checkAccessibility(requesterUser, Access.OS_EDIT, responseVO);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    responseVO = saveOsToDataBase(osVO);
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


    private ResponseVO saveOsToDataBase(OSVO osVO) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        if (osVO != null) {
            try {
                session = HibernateUtil.getNewSession();

                OS os = null;
                if (osVO.getOsId() != null) {
                    os = (OS) session.load(OS.class, osVO.getOsId());
                } else {
                    responseVO = OSService.Instance.checkInputOsVo(osVO);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    os = new OS();
                }
                if (osVO.getDisabled() != null) {
                    os.setDisabled(osVO.getDisabled());
                }
                if (osVO.getOsName() != null && !osVO.getOsName().trim().equals("")) {
                    os.setOsName(osVO.getOsName().trim());
                }
                if (osVO.getOsVersion() != null && !osVO.getOsVersion().trim().equals("")) {
                    os.setOsVersion(osVO.getOsVersion());
                }
                // todo check existence of enabled os if it is necessary
                if (osVO.getOsType() != null) {
                    os.setOsType(osVO.getOsType());
                }

                // we shouldn't set luncher to os on edit and create os
              /*  if(osVO.getHandlerAppVOs()!=null && osVO.getHandlerAppList().size()>0){
                    os.setHandlerApps(osVO.getHandlerAppList());
                }
*/
                Transaction tx = session.beginTransaction();
                OSService.Instance.saveOrUpdate(os, session);
                tx.commit();
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

                OSVO convertedOsVO = OSVO.buildOSVO(os);
                String osString = JsonUtil.getJson(convertedOsVO);
                osString.replaceAll("\\\\", "");
                String replaceStr = osString.replaceAll("\\\\", "").replaceAll("\\\"", "\"");
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


    @RequestMapping(value = "/listHandlerApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO listHandlerApp(@RequestParam(required = false) HandlerAppVO handlerAppVO,
                                     HttpServletRequest request, HttpServletResponse response) {

        Session session = null;
        ResponseVO responseVO = new ResponseVO();

        try {
            Integer fromIndex = PrincipalUtil.getFromIndex();
            Integer countIndex = PrincipalUtil.getCountIndex();
            Boolean getResultCount = PrincipalUtil.getResultCount();
            Boolean isAscending = PrincipalUtil.isAscending();

            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            UserService.Instance.checkAccessibility(requesterUser, Access.OS_EDIT, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            PrincipalUtil.setCurrentUser(requesterUser);

            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            session = HibernateUtil.getCurrentSession();

            Boolean oldVersion =( handlerAppVO.getDefault()==null || handlerAppVO.getDefault().equals(false))?true:false;

            if (getResultCount) {
                HandlerAppService.HandlerAppCriteria criteria = HandlerAppService.Instance.getHandlerAppCriteria(handlerAppVO, session);

                if (oldVersion != null) {
                    criteria.setDefault(!oldVersion);
                } else {
                    criteria.setDefault(true);
                }


                Object jsonObject = Long.valueOf(0);
                if (criteria != null) {
                    Long count = HandlerAppService.Instance.count(criteria, session);

                    //todo delete this line after toufan's version
                    // to get all set default or not set default handler app
                    if (count==null || count<Long.valueOf(1)) {
                        criteria.setDefault(null);
                        count = HandlerAppService.Instance.count(criteria, session);
                    }

                    jsonObject = count;
                }
                responseVO.setResult(JsonUtil.getJson(jsonObject));

            } else {
                String sortBy = PrincipalUtil.getSortBy();
                String sortProperties = null;
                if (sortBy != null) {
                    try {
                        List<String> declaredField = new HandlerApp().getDeclaredField();
                        if (declaredField.contains(sortBy)) {
                            sortProperties = sortBy;
                        }
                    } catch (Exception e) {
                        sortProperties = null;
                    }
                }
                List<HandlerAppVO> handlerAppVOList = getHandlerAppList(handlerAppVO, session, fromIndex, countIndex, sortProperties,isAscending,oldVersion );

                responseVO.setResult(JsonUtil.getJson(handlerAppVOList));
            }

            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

        } catch (Exception ex) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            if (ex instanceof PreProcessorException) {
                responseVO.setResult(ex.getMessage());
            } else {
                responseVO.setResult(ErrorPhrases.GENERAL_ERROR.getMessage());
            }
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }


    @RequestMapping(value = "/setHandlerAppToOS", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO setHandlerAppToOS(@RequestParam(required = true) HandlerAppVO handlerAppVO,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {

        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        User requesterUser = null;
        Transaction tx = null;
        try {
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (handlerAppVO != null && handlerAppVO.getId() != null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
            } else {
                List<Access> accessList = new ArrayList<>();
                accessList.add(Access.HANDLERAPP_ADD);
                accessList.add(Access.OS_EDIT);
                UserService.Instance.checkAccessibility(requesterUser, accessList, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);
                responseVO = HandlerAppService.Instance.checkInputHandlerApp(handlerAppVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                session = HibernateUtil.getCurrentSession();
                Boolean oldVersion =( handlerAppVO.getDefault()==null || handlerAppVO.getDefault().equals(false))?true:false;

                List<HandlerAppVO> handlerAppVOList = getHandlerAppList(handlerAppVO, session, 0, -1 , null, false, oldVersion);
                if (handlerAppVOList != null && !handlerAppVOList.isEmpty()) {
                    responseVO.setResultStatus(ResultStatus.DUPLICATE_HANDLER_APP);
                    responseVO.setResult(ResultStatus.DUPLICATE_HANDLER_APP.toString());
                    return responseVO;
                }
                tx = session.beginTransaction();
                OS os = (OS) session.get(OS.class, handlerAppVO.getOsId());
                HandlerApp handlerApp = HandlerAppService.Instance.saveHandlerAppToDataBase(session, handlerAppVO, os);
                if (handlerApp != null) {
                    List<HandlerApp> handlerAppList = HandlerAppService.Instance.getAndSetHandlerAppList(session, handlerApp, os);
                    os.setHandlerApps(handlerAppList);
                    OSService.Instance.saveOrUpdate(os, session);
                    tx.commit();
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    responseVO.setResult(JsonUtil.getJson(HandlerAppVO.buildHandlerAppVO(handlerApp)));
                } else {
                    tx.rollback();
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult(AppStorePropertyReader.getString("error.generalErr"));
                }
            }
        } catch (Exception e) {
            tx.rollback();
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;

    }


    private List<HandlerAppVO> getHandlerAppList(HandlerAppVO handlerAppVO, Session session, int fromIndex, int count, String sortProperties, Boolean isAscending, Boolean oldVersion) {
        List<HandlerAppVO> handlerAppVOList = new ArrayList<>();

        try {
            List<HandlerAppService.HandlerAppSearchResultModel> handlerAppSearchResultModels = null;
            HandlerAppService.HandlerAppCriteria criteria = HandlerAppService.Instance.getHandlerAppCriteria(handlerAppVO, session);
            if (criteria != null) {

                if (oldVersion != null) {
                    criteria.setDefault(!oldVersion);
                }
                handlerAppSearchResultModels = HandlerAppService.Instance.list(criteria, fromIndex, count, sortProperties, true, session);


                //todo delete this line after toufan's version
                if (handlerAppSearchResultModels == null || handlerAppSearchResultModels.isEmpty()) {
                    // to get all set default or not set default handler app
                    criteria.setDefault(null);
                    handlerAppSearchResultModels = HandlerAppService.Instance.list(criteria, fromIndex, count, sortProperties, true, session);
                }


                if (handlerAppSearchResultModels != null && !handlerAppSearchResultModels.isEmpty()) {
                    Stream<HandlerAppVO> handlerAppVOStream = handlerAppSearchResultModels.stream().map(handlerApp -> HandlerAppVO.buildHandlerAppVO(handlerApp));
                    handlerAppVOList = handlerAppVOStream.collect(Collectors.<HandlerAppVO>toList());
                }
            }
        } catch (HibernateException e) {
        }
        return handlerAppVOList;
    }

    @RequestMapping(value = "/editHandlerApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO editHandlerApp(
            @RequestParam(required = true) HandlerAppVO handlerAppVO,
            HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        User requesterUser = null;
        HandlerApp loadedHandlerApp = null;
        Organization organization = null;
        OSEnvironment environment = null;
        ResponseVO responseVO = new ResponseVO();
        Transaction tx = null;

        try {
            boolean needToCheck = false;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (handlerAppVO == null || handlerAppVO.getId() == null) {
                responseVO.setResultStatus(ResultStatus.NULL_DATA);
                responseVO.setResult(ResultStatus.NULL_DATA.toString());
                return responseVO;
            }
            session = HibernateUtil.getCurrentSession();
            loadedHandlerApp = (HandlerApp) session.get(HandlerApp.class, handlerAppVO.getId());
            if (loadedHandlerApp == null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                return responseVO;
            }
            UserService.Instance.checkAccessibility(requesterUser, Access.HANDLERAPP_EDIT, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (handlerAppVO.getActive() != null) {
                loadedHandlerApp.setActive(handlerAppVO.getActive());
            }
            if (handlerAppVO.getVersionCode() != null) {
                loadedHandlerApp.setVersionCode(handlerAppVO.getVersionCode());
                needToCheck = true;
            }

            if (handlerAppVO.getOrgId() != null) {
                organization = (Organization) session.get(Organization.class, handlerAppVO.getOrgId());
                if (organization == null) {
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    return responseVO;
                }
                loadedHandlerApp.setOrganization(organization);
                needToCheck = true;
            }

            if (handlerAppVO.getEnvironmentId() != null) {
                environment = (OSEnvironment) session.get(OSEnvironment.class, handlerAppVO.getEnvironmentId());
                if (environment == null) {
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    return responseVO;
                }
                loadedHandlerApp.setOsEnvironment(environment);
                needToCheck = true;
            }
            Boolean activity = handlerAppVO.getActive();
            if (needToCheck) {
                handlerAppVO.setActive(null);
                Boolean oldVersion =( handlerAppVO.getDefault()==null || handlerAppVO.getDefault().equals(false))?true:false;
                List<HandlerAppVO> handlerAppList = getHandlerAppList(handlerAppVO, session, 0, -1 , null, false, oldVersion);
                if (handlerAppList != null && !handlerAppList.isEmpty()) {
                    for (HandlerAppVO handlerAppVOInList : handlerAppList) {
                        if (!handlerAppVOInList.getId().equals(loadedHandlerApp.getId())) {
                            responseVO.setResultStatus(ResultStatus.DUPLICATE_HANDLER_APP);
                            responseVO.setResult(ResultStatus.DUPLICATE_HANDLER_APP.toString());
                            return responseVO;
                        }
                    }
                }
            }
            if (handlerAppVO.getFileHandlerAppKey() != null && handlerAppVO.getFileHandlerAppKey().trim().equals("")) {
                File handlerAppFile = new File();
                handlerAppFile.setStereoType(StereoType.LAUNCHER_FILE);
                handlerAppFile.setFilePath(handlerAppVO.getFileHandlerAppKey());
                handlerAppFile.setFileName(FileServerService.Instance.getFileNameFromFilePath(handlerAppVO.getFileHandlerAppKey()));
                BaseEntityService.Instance.saveOrUpdate(handlerAppFile, session);
                loadedHandlerApp.setHandlerFile(handlerAppFile);
                loadedHandlerApp.setUploadedFileDate(DateTime.now());
            }

            if (handlerAppVO.getTestFileHandlerAppKey() != null && handlerAppVO.getTestFileHandlerAppKey().trim().equals("")) {
                File handlerAppTestFile = new File();
                handlerAppTestFile.setStereoType(StereoType.LAUNCHER_FILE);
                handlerAppTestFile.setFilePath(handlerAppVO.getTestFileHandlerAppKey());
                handlerAppTestFile.setFileName(FileServerService.Instance.getFileNameFromFilePath(handlerAppVO.getTestFileHandlerAppKey()));
                BaseEntityService.Instance.saveOrUpdate(handlerAppTestFile, session);
                loadedHandlerApp.setTestHandlerFile(handlerAppTestFile);
                if (loadedHandlerApp.getTestHandlerFile() != null) {
                    loadedHandlerApp.setUploadedTestFileDate(DateTime.now());
                }

            }

            PrincipalUtil.setCurrentUser(requesterUser);
            tx = session.beginTransaction();
            loadedHandlerApp.setActive(activity == null ? false : true);
            HandlerAppService.Instance.saveOrUpdate(loadedHandlerApp, session);
            tx.commit();
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult(JsonUtil.getJson(HandlerAppVO.buildHandlerAppVO(loadedHandlerApp)));

        } catch (Exception ex) {
            if (tx != null)
                tx.rollback();
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(ex.getMessage());

            logger.error("Error occured in activateDeactivateHandlerApp webService ", ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return responseVO;
    }
}

