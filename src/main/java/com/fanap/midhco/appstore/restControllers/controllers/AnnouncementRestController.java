package com.fanap.midhco.appstore.restControllers.controllers;

import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.AnouncementType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.vos.AnouncementVO;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.anouncement.AnouncementService;
import com.fanap.midhco.appstore.service.anouncement.ITaskResult;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by A.Moshiri on 4/23/2018.
 */
@RestController
@RequestMapping("/service/announcement")
public class AnnouncementRestController {

    @RequestMapping(value = "/listAnnouncement", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO listAnnouncement(@RequestParam(required = false) AnouncementVO announcementVO) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();

            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            UserService.Instance.checkAccessibility(requesterUser, Access.ANOUNCEMENT_LIST, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            PrincipalUtil.setCurrentUser(requesterUser);
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            List<Anouncement> announcementList = new ArrayList<>();
            AnouncementService.AnouncmentCriteria criteria = new AnouncementService.AnouncmentCriteria();
            Integer fromIndex = PrincipalUtil.getFromIndex();
            Integer countIndex = PrincipalUtil.getCountIndex();
            Boolean getResultCount = PrincipalUtil.getResultCount();
            Boolean isAscending = PrincipalUtil.isAscending();

            Object resultObject = null;

            if (announcementVO != null) {

                boolean hasValue = false;
                if (announcementVO.getId() != null) {
                    criteria.setId(announcementVO.getId());
                    hasValue = true;
                }
                if (announcementVO.getAnouncementText() != null && !announcementVO.getAnouncementText().trim().equals("")) {
                    criteria.setAnouncementText(announcementVO.getAnouncementText().trim());
                    hasValue = true;
                }

                if (announcementVO.getOsTypes() != null) {
                    List<OSType> osTypeList = new ArrayList<>(announcementVO.getOsTypes());
                    criteria.setOsTypes(osTypeList);
                    hasValue = true;
                }

                if (announcementVO.getStartDateTime() != null) {
                    DateTime[] startDate = new DateTime[2];
                    startDate[0] = DateTime.beforeFrom(announcementVO.getStartDateTime(), 1);
                    startDate[1] = announcementVO.getStartDateTime();
                    criteria.setStartDateTime(startDate);
                    hasValue = true;
                }

                if (announcementVO.getExpireDateTime() != null) {
                    DateTime[] expireDate = new DateTime[2];
                    expireDate[0] = announcementVO.getExpireDateTime();
                    expireDate[1] = DateTime.afterFrom(announcementVO.getExpireDateTime(), 1);
                    criteria.setExpireDateTime(expireDate);
                    hasValue = true;
                }

                if (announcementVO.getOrganizations() != null) {
                    List<Organization> organizationList = new ArrayList<>(announcementVO.getOrganizations());
                    criteria.setOrganizations(organizationList);
                    hasValue = true;
                }

                if (announcementVO.getOsEnvironments() != null) {
                    List<OSEnvironment> environmentList = new ArrayList<>(announcementVO.getOsEnvironments());
                    criteria.setOsEnvironments(environmentList);
                    hasValue = true;
                }


                if (announcementVO.getActive() != null) {
                    criteria.setActive(announcementVO.getActive());
                    hasValue = true;
                }

                if (announcementVO.getExpired() != null) {
                    criteria.setExpired(announcementVO.getExpired());
                    hasValue = true;
                }
                if (announcementVO.getAnouncementTypeSet() != null) {
                    criteria.setAnouncementType(announcementVO.getAnouncementTypeSet());
                    hasValue = true;
                }

                if (!hasValue) {
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    responseVO.setResult("");
                    return responseVO;
                }
            }
            String sortProperties[] = new String[2];

            if (getResultCount) {
                Long resultCount = AnouncementService.Instance.count(criteria, session);
                resultObject = resultCount;
            } else {
                String inputSortProperties[] = (PrincipalUtil.getSortBy() != null) ? PrincipalUtil.getSortBy().split(",") : new String[2];
                Boolean hasInvalidValue = false;
                if (inputSortProperties != null && inputSortProperties.length == 2) {
                    try {
                        List<String> declaredField = new Anouncement().getDeclaredField();
                        for (int i = 0; i < inputSortProperties.length; i++) {
                            if (declaredField.contains(inputSortProperties[i])) {
                                sortProperties[i] = inputSortProperties[i];
                            } else {
                                hasInvalidValue = true;
                            }
                        }
                    } catch (Exception e) {
                        sortProperties = null;
                    }
                } else {
                sortProperties[0] = "ent.startDateTime.dayDate";
                sortProperties[1] = "ent.startDateTime.dayTime";
                }
                if (hasInvalidValue) {
                    sortProperties[0] = "ent.startDateTime.dayDate";
                    sortProperties[1] = "ent.startDateTime.dayTime";
                }
                announcementList = AnouncementService.Instance.list(criteria, fromIndex, countIndex, sortProperties, isAscending, session);
                if (announcementList != null && !announcementList.isEmpty()) {
                    Stream<AnouncementVO> announcementVOStream = announcementList.stream().map(announcement -> AnouncementVO.buildVO(announcement));
                    resultObject = announcementVOStream.collect(Collectors.<AnouncementVO>toList());
                } else {
                    resultObject = new ArrayList<AnouncementVO>();
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


    @RequestMapping(value = "addAnnouncement", headers = "accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO addAnnouncement(@RequestParam(required = true) AnouncementVO announcementVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (announcementVO != null && announcementVO.getId() != null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
            } else {
                UserService.Instance.checkAccessibility(requesterUser, Access.ANOUNCEMENT_ADD, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);
                responseVO = saveAnnouncementToDataBase(announcementVO);
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }


    @RequestMapping(value = "editAnnouncement", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO editAnnouncement(@RequestParam(required = true) AnouncementVO announcementVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();

        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (announcementVO == null || announcementVO.getId() == null) {
                responseVO.setResultStatus(ResultStatus.NULL_DATA);
                responseVO.setResult(ResultStatus.NULL_DATA.toString());
            } else {
                Long announcementId = announcementVO.getId();

                try {
                    PrincipalUtil.setCurrentUser(requesterUser);
                    Session session = HibernateUtil.getCurrentSession();
                    Anouncement loadedAnouncement = (Anouncement) session.get(Anouncement.class, announcementId);
                    if (loadedAnouncement == null) {
                        responseVO.setResultStatus(ResultStatus.ANNOUNCEMENT_NOT_FOUND);
                        responseVO.setResult(ResultStatus.ANNOUNCEMENT_NOT_FOUND.toString());
                        return responseVO;
                    }
                    UserService.Instance.checkAccessibility(requesterUser, Access.ANOUNCEMENT_EDIT, responseVO);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    responseVO = saveAnnouncementToDataBase(announcementVO);
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


    @RequestMapping(value = "/callAnnouncement", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO callAnnouncement(
            @RequestParam(required = true, value = "announcementId") Long announcementId
            , @RequestParam(required = false) String paramsJsonString
            , HttpServletResponse response
            , HttpServletRequest request) {

        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {

            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            UserService.Instance.checkAccessibility(requesterUser, Access.ANOUNCEMENT_CALL, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            session = HibernateUtil.getCurrentSession();

            AnouncementService.AnouncmentCriteria anouncmentCriteria = new AnouncementService.AnouncmentCriteria();
            anouncmentCriteria.setId(announcementId);

            List<Anouncement> anouncementList = AnouncementService.Instance.list(anouncmentCriteria, 0, -1, null, true, session);
            if (!anouncementList.isEmpty()) {
                Anouncement intendedAnouncement = anouncementList.get(0);
                responseVO = AnouncementService.Instance.checkAnouncement(intendedAnouncement);

                if (responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    Map<String, String> parameterMap = new HashMap<>();
                    if (paramsJsonString != null && !paramsJsonString.trim().equals("")) {
                        JSONObject jsonObject = new JSONObject(paramsJsonString.trim());
                        for (String key : (Set<String>) jsonObject.keySet()) {
                            parameterMap.put(key, jsonObject.getString(key));
                        }
                    }
                    ITaskResult taskResult = AnouncementService.Instance.launchAnouncement(intendedAnouncement, parameterMap);
                    Object result = taskResult.getResult();
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    responseVO.setResult(result.toString());

                }

            } else {
                responseVO.setResult("No such announcement exists!");
            }

            return responseVO;
        } catch (Exception ex) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(ex.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return responseVO;
    }


    private ResponseVO saveAnnouncementToDataBase(AnouncementVO announcementVO) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        if (announcementVO != null) {
            try {
                session = HibernateUtil.getNewSession();

                Anouncement announcement = null;
                if (announcementVO.getId() != null) {
                    announcement = (Anouncement) session.load(Anouncement.class, announcementVO.getId());
                } else {
                    announcement = new Anouncement();
                }
                responseVO = AnouncementService.Instance.checkInputAnnouncementVO(announcementVO, session);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                if (announcementVO.getAnouncementText() != null) {
                    announcement.setAnouncementText(announcementVO.getAnouncementText());
                }
                if (announcementVO.getOrganizations() != null) {
                    announcement.setOrganizations(new ArrayList<>(announcementVO.getOrganizations()));
                }
                if (announcementVO.getAnouncementImageFileKey() != null) {
                    announcement.setAnouncementImageFileKey(announcementVO.getAnouncementImageFileKey());
                }
                if (announcementVO.getOsTypes() != null) {
                    announcement.setOsTypes(new ArrayList<>(announcementVO.getOsTypes()));
                }
                if (announcementVO.getActive() != null) {
                    announcement.setActive(announcementVO.getActive());
                }
                if (announcementVO.getExpired() != null) {
                    announcement.setExpired(announcementVO.getExpired());
                }
                if (announcementVO.getActionCategory() != null) {
                    announcement.setActionCategory(announcementVO.getActionCategory());
                }
                if (announcementVO.getActionDescriptor() != null) {
                    announcement.setActionDescriptor(announcementVO.getActionDescriptor());
                }
                if (announcementVO.getExpireDateTime() != null) {
                    announcement.setExpireDateTime(announcementVO.getExpireDateTime());
                }
                if (announcementVO.getStartDateTime() != null) {
                    announcement.setStartDateTime(announcementVO.getStartDateTime());
                }
                if (announcementVO.getAnouncementType() != null) {
                    announcement.setAnouncementType(announcementVO.getAnouncementType());
                }
                if (announcementVO.getOsEnvironments() != null) {
                    announcement.setOsEnvironments(new ArrayList<>(announcementVO.getOsEnvironments()));
                }

                if (announcement.getAnouncementType().equals(AnouncementType.VOID)) {
                    announcement.setActionCategory(null);
                    announcement.setActionDescriptor(null);
                }


                //todo set data

                Transaction tx = session.beginTransaction();
                AnouncementService.Instance.saveOrUpdate(announcement, session);
                tx.commit();
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

                AnouncementVO convertedAnnouncementVO = AnouncementVO.buildVO(announcement);
                JSONObject convertedAnnouncementObject = new JSONObject(convertedAnnouncementVO);
                String convertedAnnouncementObjectString = convertedAnnouncementObject.toString();
                convertedAnnouncementObjectString.replaceAll("\\\\", "");
                String replaceStr = convertedAnnouncementObjectString.replaceAll("\\\\", "").replaceAll("\\\"", "\"");

                String announcementString = JsonUtil.getJson(convertedAnnouncementVO);
                announcementString.replaceAll("\\\\", "");
//                String replaceStr = announcementString.replaceAll("\\\\", "").replaceAll("\\\"", "\"");


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


    public static void main(String[] args) {
////        String x = new String("ab");
//        StringBuffer x = new StringBuffer("ab");
//        String sentences = new String("salam ali man injam");
//        String reverse =  reverseString(sentences);
//        System.out.println("reverse = " + reverse);
//        change(x);
//        System.out.println(x);
    }


    public static String reverseString(String str){
        if (str.length() == 1) {
            return str;
        }
        return reverseString(str.substring(1)) + str.charAt(0);
    }
    public static void change(StringBuffer x) {
        x.delete(0,x.length());
        x.append("xd");
    }

}


