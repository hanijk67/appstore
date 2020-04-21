package com.fanap.midhco.appstore.restControllers.controllers;

import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.restControllers.vos.HandlerAppVO;
import com.fanap.midhco.appstore.restControllers.vos.OSTypeVO;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.handlerApp.HandlerAppService;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by A.Moshiri on 4/10/2018.
 */
@RestController
@RequestMapping("/service/osType")

public class OsTypeRestController {

    @RequestMapping(value = "listOsType", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO listOsType(@RequestParam(required = false) OSTypeVO osTypeVO, HttpServletRequest request, HttpServletResponse response) {

        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();

            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            UserService.Instance.checkAccessibility(requesterUser, Access.OSTYPE_LIST, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            PrincipalUtil.setCurrentUser(requesterUser);
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            List<OSType> osTypeList = new ArrayList<>();
            Integer fromIndex = PrincipalUtil.getFromIndex();
            Integer countIndex = PrincipalUtil.getCountIndex();
            Boolean getResultCount = PrincipalUtil.getResultCount();
            Boolean isAscending = PrincipalUtil.isAscending();

            Object resultObject = null;
            OSTypeService.OSTypeCriteria osTypeCriteria = new OSTypeService.OSTypeCriteria();

            if (osTypeVO != null) {
                boolean hasValue = false;
                if (osTypeVO.getOsTypeID() != null) {
                    osTypeCriteria.setId(osTypeVO.getOsTypeID());
                    hasValue = true;
                }
                if (osTypeVO.getOsName() != null && !osTypeVO.getOsName().trim().equals("")) {
                    osTypeCriteria.setName(osTypeVO.getOsName().trim());
                    hasValue = true;
                }

                if (osTypeVO.getDisabled() != null) {
                    osTypeCriteria.setDisabled(osTypeVO.getDisabled());
                }

                if (!hasValue) {
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    responseVO.setResult("");
                    return responseVO;
                }
            }
            if (getResultCount) {
                Long resultCount = OSTypeService.Instance.count(osTypeCriteria, session);
                resultObject = resultCount;
            } else {
                String sortBy = PrincipalUtil.getSortBy();
                String sortProperties = null;
                if (sortBy != null) {
                    try {
                        List<String> declaredField = new OSType().getDeclaredField();
                        if (declaredField.contains(sortBy)) {
                            sortProperties = sortBy;
                        }
                    } catch (Exception e) {
                        sortProperties = null;
                    }
                }
                osTypeList = OSTypeService.Instance.list(osTypeCriteria, fromIndex, countIndex, sortProperties, isAscending, session);
                if (osTypeList != null && !osTypeList.isEmpty()) {
                    Stream<OSTypeVO> osTypeVOStream = osTypeList.stream().map(osType -> OSTypeVO.buildOsTypeVO(osType));
                    resultObject = osTypeVOStream.collect(Collectors.<OSTypeVO>toList());
                } else {
                    resultObject = new ArrayList<OSTypeVO>();
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


    @RequestMapping(value = "addOsType", headers = "accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO addOsType(@RequestParam(required = true) OSTypeVO osTypeVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (osTypeVO != null && osTypeVO.getOsTypeID() != null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
            } else {
                UserService.Instance.checkAccessibility(requesterUser, Access.OSTYPE_ADD, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);
                responseVO = saveOsTypeToDataBase(osTypeVO);
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }


    @RequestMapping(value = "editOsType", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO editOsType(@RequestParam(required = true) OSTypeVO osTypeVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();

        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (osTypeVO == null || osTypeVO.getOsTypeID() == null) {
                responseVO.setResultStatus(ResultStatus.NULL_DATA);
                responseVO.setResult(ResultStatus.NULL_DATA.toString());
            } else {
                Long osTypeId = osTypeVO.getOsTypeID();
                try {
                    PrincipalUtil.setCurrentUser(requesterUser);
                    Session session = HibernateUtil.getCurrentSession();
                    OSType loadedOsType = (OSType) session.get(OSType.class, osTypeId);
                    if (loadedOsType == null) {
                        responseVO.setResultStatus(ResultStatus.OS_TYPE_NOT_FOUND);
                        responseVO.setResult(ResultStatus.OS_TYPE_NOT_FOUND.toString());
                        return responseVO;
                    }
                    UserService.Instance.checkAccessibility(requesterUser, Access.OSTYPE_EDIT, responseVO);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    responseVO = saveOsTypeToDataBase(osTypeVO);
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

    @RequestMapping(value = "/getOSTypeListWithLauncher", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getOSTypeListWithLauncher(@RequestParam(required = false) HandlerAppVO handlerAppVO,
                                                HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();

        List<OSTypeVO> osTypeVOList = new ArrayList<>();
        try {
            session = HibernateUtil.getCurrentSession();

            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            UserService.Instance.checkAccessibility(requesterUser, Access.OSTYPE_LIST, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            PrincipalUtil.setCurrentUser(requesterUser);
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            session = HibernateUtil.getNewSession();
            OSTypeService.OSTypeCriteria osTypeCriteria = new OSTypeService.OSTypeCriteria();
            osTypeCriteria.disabled = false;
            List<OSType> osTypeListWithLauncher = OSTypeService.Instance.list(osTypeCriteria, 0, -1, null, true, session);
            Set<OSType> osTypeSet = new HashSet<>();

            OSService.OSCriteria osCriteria = new OSService.OSCriteria();
            osCriteria.osType = osTypeListWithLauncher;
            osCriteria.disabled = false;
            Organization organization = null;
            if (handlerAppVO != null) {
                Long organizationId = handlerAppVO.getOrgId();
                if (organizationId != null) {
                    organization = (Organization) session.get(Organization.class, organizationId);
                    if (organization == null) {
                        osTypeVOList = osTypeListWithLauncher.stream()
                                .map(osType -> OSTypeVO.buildOsTypeVO(osType)).collect(Collectors.toList());
                        responseVO.setResult(JsonUtil.getJson(osTypeVOList));
                        responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                        return responseVO;
                    }
                }

//                List<OS> osList = OSService.Instance.getOSForOSType(osType);
                List<OS> osList = OSService.Instance.list(osCriteria, 0, -1, null, true, session);
                for (OS os : osList) {
                    HandlerAppService.HandlerAppCriteria handlerAppCriteria = new HandlerAppService.HandlerAppCriteria();
                    if (handlerAppVO.getEnvironmentId() != null) {
                        OSEnvironment environment = (OSEnvironment) session.get(OSEnvironment.class, handlerAppVO.getEnvironmentId());
                        if (environment != null) {
                            handlerAppCriteria.setOsEnvironment(environment);
                        }
                    }
                    List<Long> osIdList = new ArrayList<>();
                    osIdList.add(os.getId());
                    handlerAppCriteria.setOsIds(osIdList);
                    handlerAppCriteria.setActive(true);
                    handlerAppCriteria.setOrganization(organization);
                    List<HandlerAppService.HandlerAppSearchResultModel> handlerAppSearchResultModels = HandlerAppService.Instance.list(handlerAppCriteria, 0, -1, null, true, session);
                    if (handlerAppSearchResultModels != null && !handlerAppSearchResultModels.isEmpty()) {
                        osTypeSet.add(os.getOsType());
                    }
                }
            }
            osTypeVOList = osTypeSet.stream()
                    .map(osType -> OSTypeVO.buildOsTypeVO(osType)).collect(Collectors.toList());

            responseVO.setResult(JsonUtil.getJson(osTypeVOList));
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            return responseVO;

        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            return responseVO;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private ResponseVO saveOsTypeToDataBase(OSTypeVO osTypeVO) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        if (osTypeVO != null) {
            try {
                session = HibernateUtil.getNewSession();

                OSType osType = null;
                if (osTypeVO.getOsTypeID() != null) {
                    osType = (OSType) session.load(OSType.class, osTypeVO.getOsTypeID());
                } else {
                    responseVO = OSTypeService.Instance.checkInputOsTypeVo(osTypeVO);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    osType = new OSType();
                }

                if (osTypeVO.getOsName() != null && !osTypeVO.getOsName().trim().equals("")) {
                    osType.setName(osTypeVO.getOsName().trim());
                }

                if (osTypeVO.getDisabled() != null) {
                    osType.setDisabled(osTypeVO.getDisabled());
                }
                if (osTypeVO.getOsCompareScriptFileKey() != null) {

                    String tempLocation = System.getProperty("java.io.tmpdir");

                    String tempFileName = tempLocation + "/" + osTypeVO.getOsCompareScriptFileKey();
                    FileOutputStream out = new FileOutputStream(tempFileName);
                    FileServerService.Instance.downloadFileFromServer(osTypeVO.getOsCompareScriptFileKey(), out);

                    StringBuffer stringBuffer = new StringBuffer();

                    InputStreamReader reader = new InputStreamReader(new FileInputStream(tempFileName), "UTF-8");
                    int character;

                    while ((character = reader.read()) != -1) {
                        stringBuffer.append((char) character);
                    }

                    String osTypeScript = stringBuffer.toString();
                    String convertedOsTypeScript = osTypeScript.replaceAll("\\\\r\\\\n", " ");
                    ResponseVO wrongScriptResponseVO = OSTypeService.Instance.checkScrtip(convertedOsTypeScript);
                    if (!wrongScriptResponseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return wrongScriptResponseVO;
                    }
                    osType.setOsCompareScript(convertedOsTypeScript);
                }

                Transaction tx = session.beginTransaction();
                OSTypeService.Instance.saveOrUpdate(osType, session);
                tx.commit();
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);


                OSTypeVO convertedOSTypeVO = OSTypeVO.buildOsTypeVO(osType);
                String osTypeString = JsonUtil.getJson(convertedOSTypeVO);
                osTypeString.replaceAll("\\\\", "");
                String replaceStr = osTypeString.replaceAll("\\\\", "").replaceAll("\\\"", "\"");
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
