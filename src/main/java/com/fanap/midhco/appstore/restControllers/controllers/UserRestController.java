package com.fanap.midhco.appstore.restControllers.controllers;

import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.applicationUtils.UserJsonSerializer;
import com.fanap.midhco.appstore.entities.Contact;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.restControllers.vos.UserStatusVO;
import com.fanap.midhco.appstore.restControllers.vos.UserVO;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by A.Moshiri on 3/25/2018.
 */
@RestController
@RequestMapping("/service/user")
public class UserRestController {

    static private final Logger logger = Logger.getLogger(UserRestController.class);

    @RequestMapping(value = "/listUser", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO listUser(@RequestParam(required = false) UserVO userVO, HttpServletRequest request, HttpServletResponse response) {

        ResponseVO responseVO = new ResponseVO();
        if (checkInvalidInputUserVO(userVO, responseVO)) {
            return responseVO;
        }
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        Session session = null;
        try {
            Integer fromIndex = PrincipalUtil.getFromIndex();
            Integer countIndex = PrincipalUtil.getCountIndex();
            Boolean getResultCount = PrincipalUtil.getResultCount();
            Boolean isAscending = PrincipalUtil.isAscending();

            List<User> userList = new ArrayList<>();

            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            UserService.Instance.checkAccessibility(requesterUser, Access.USER_LIST, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                if (userVO==null || userVO.getUserId()==null || !userVO.getUserId().equals(requesterUser.getId()) ){
                return responseVO;
            }
            }
            PrincipalUtil.setCurrentUser(requesterUser);
            UserService.UserCriteria userCriteria = new UserService.UserCriteria();
            session = HibernateUtil.getNewSession();

            if (userVO != null) {
                boolean hasValue = false;
                if (userVO.getId() != null) {
                    userCriteria.id = userVO.getId();
                    hasValue = true;
                }

                if (userVO.getUserId() != null) {
                    userCriteria.userId = userVO.getUserId();
                    hasValue = true;
                }
                if (userVO.getUserName() != null && !userVO.getUserName().trim().equals("")) {
                    userCriteria.userName = userVO.getUserName();
                    hasValue = true;
                }

                if (userVO.getUserNameContain() != null && !userVO.getUserNameContain().trim().equals("")) {
                    userCriteria.userNameContain = userVO.getUserNameContain();
                    hasValue = true;
                }
                if (userVO.getFirstName() != null && !userVO.getFirstName().trim().equals("")) {
                    userCriteria.firstName = userVO.getFirstName();
                    hasValue = true;
                }
                if (userVO.getLastName() != null && !userVO.getLastName().trim().equals("")) {
                    userCriteria.lastName = userVO.getLastName();
                    hasValue = true;
                }

                if (userVO.getLastNameContain() != null && !userVO.getLastNameContain().trim().equals("")) {
                    userCriteria.lastNameContain = userVO.getLastNameContain();
                    hasValue = true;
                }
                if (userVO.getStatuses() != null) {
                    userCriteria.statuses = userVO.getStatuses();
                    hasValue = true;
                }
                if (userVO.getCreatorUsers() != null) {
                    userCriteria.creatorUsers = userVO.getCreatorUsers();
                    hasValue = true;
                }
                if (userVO.getRoles() != null) {
                    userCriteria.roles = userVO.getRoles();
                    hasValue = true;
                }

                if (userVO.getCreationMinDate() != null || userVO.getCreationMaxDate() != null) {
                    DateTime minDateTime = null;
                    DateTime maxDateTime = null;
                    DateTime[] creationDateTimeArray = new DateTime[0];
                    if (userVO.getCreationMinDate() != null ) {
                        Date minDate = new Date(Long.valueOf(userVO.getCreationMinDate()));
                        minDateTime = new DateTime(minDate);
                    } else {

                        minDateTime = DateTime.MIN_DATE_TIME;
                    }

                    if (userVO.getCreationMaxDate() != null ) {
                        Date maxDate = new Date(Long.valueOf(userVO.getCreationMaxDate()));
                        maxDateTime = new DateTime(maxDate);
                    } else {

                        maxDateTime = DateTime.MAX_DATE_TIME;
                    }
                    creationDateTimeArray = new DateTime[]{minDateTime, maxDateTime};
                    userCriteria.creationDateTime = creationDateTimeArray;
                    hasValue = true;
                }

                if (userVO.getLastLoginMaxDate() != null || userVO.getLastLoginMinDate() != null) {
                    DateTime lastLoginMinDateTime = null;
                    DateTime lastLoginMaxDateTime = null;
                    DateTime[] lastLoginDateTimeArray = new DateTime[0];
                    if (userVO.getLastLoginMinDate() != null ) {
                        Date minDate = new Date(Long.valueOf(userVO.getLastLoginMinDate()));
                        lastLoginMinDateTime = new DateTime(minDate);
                    } else {

                        lastLoginMinDateTime = DateTime.MIN_DATE_TIME;
                    }
                    if (userVO.getLastLoginMaxDate() != null ) {
                        Date minDate = new Date(Long.valueOf(userVO.getLastLoginMaxDate()));
                        lastLoginMaxDateTime = new DateTime(minDate);
                    } else {

                        lastLoginMaxDateTime = DateTime.MAX_DATE_TIME;
                    }


                    lastLoginDateTimeArray = new DateTime[]{lastLoginMinDateTime, lastLoginMaxDateTime};

                    userCriteria.lastLoginDateTime = lastLoginDateTimeArray;
                    hasValue = true;
                }
                if (userVO.getLastName() != null && !userVO.getLastName().trim().equals("")) {
                    userCriteria.lastName = userVO.getLastName();
                    hasValue = true;
                }
                if (userVO.getLastIp() != null && !userVO.getLastIp().trim().equals("")) {
                    userCriteria.lastIp = userVO.getLastIp();
                    hasValue = true;
                }

                if (userVO.getLogged() != null) {
                    userCriteria.logged = userVO.getLogged();
                    hasValue = true;
                }

                if (!hasValue) {
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    responseVO.setResult("");
                    return responseVO;
                }
            }

            String sortBy = PrincipalUtil.getSortBy();
            String sortProperties = null;
            if(sortBy!=null){
                try {
                    if(sortBy.equals("lastName")){
                        sortProperties = "contact.lastName";
                    }else {
                        List<String> declaredField = new User().getDeclaredField();
                        if(declaredField.contains(sortBy)){
                            sortProperties = sortBy;
                        }
                    }
                } catch (Exception e) {
                    sortProperties = null;
                }
            }

            if (getResultCount) {
                Long count = UserService.Instance.count(userCriteria, sortProperties, false, session);
                responseVO.setResult(JsonUtil.getJson(count));
            } else {
                userList = UserService.Instance.list(userCriteria, fromIndex, countIndex, sortProperties, isAscending, session);
                List<UserVO> userVOS = new ArrayList<>();
                Stream<UserVO> userVOStream = userList.stream().map(userInList -> UserVO.buildUserVo(userInList));
                userVOS = userVOStream.collect(Collectors.<UserVO>toList());
                String jsonResult = JsonUtil.getJson(userVOS);
                responseVO.setResult(jsonResult);
            }
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);


        } catch (
                Exception e)

        {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());

        } finally

        {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }


    @RequestMapping(value = "/addUser", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO addUser(@RequestParam(required = true) UserVO userVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        if (checkInvalidInputUserVO(userVO, responseVO)) {
            return responseVO;
        }
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (userVO != null && userVO.getId() != null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
            } else {

                UserService.Instance.checkAccessibility(requesterUser, Access.USER_CREATE, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);

                responseVO = saveUserToDataBase(userVO);
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }

    @RequestMapping(value = "/editUser", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO editUser(@RequestParam(required = true) UserVO userVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();

        if (checkInvalidInputUserVO(userVO, responseVO)) {
            return responseVO;
        }
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        Long userId = userVO.getId();
        if (userId == null) {
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
        } else {
            try {

                User requesterUser = null;
                requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
                if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);
                Session session = HibernateUtil.getCurrentSession();
                User user = (User) session.get(User.class, userId);
                if (user == null) {
                    responseVO.setResultStatus(ResultStatus.USER_NOT_DEFINED_IN_APPSTORE);
                    responseVO.setResult(ResultStatus.USER_NOT_DEFINED_IN_APPSTORE.toString());
                }

                UserService.Instance.checkAccessibility(requesterUser, Access.USER_EDIT, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                responseVO = saveUserToDataBase(userVO);
            } catch (Exception e) {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(e.getMessage());

            }
        }
        return responseVO;
    }

    @RequestMapping(value = "/userInfo", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO userInfo(HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            UserVO userVO = new UserVO();
            userVO.setId(requesterUser.getId());
            userVO.setUserName(requesterUser.getUserName());
            responseVO = listUser(userVO , request ,response);
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());

        }

        return responseVO;
    }


    private boolean checkInvalidInputUserVO(@RequestParam(required = true) UserVO userVO, ResponseVO responseVO) {
        if (userVO != null && userVO.getUserVoStatusInt() != null && userVO.getUserVoStatusInt() != ResultStatus.SUCCESSFUL.getState()) {
            ResultStatus resultStatus = new ResultStatus(userVO.getUserVoStatusInt());
            responseVO.setResultStatus(resultStatus);
            responseVO.setResult(resultStatus.toString());
            return true;
        }
        return false;
    }

    private ResponseVO saveUserToDataBase(UserVO userVo) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        Long userIdFromSSO = null;

        try {
            Contact contact = null;
            session = HibernateUtil.getNewSession();

            User user = null;
            boolean correctUser = UserService.Instance.checkCorrectInputUser(userVo, responseVO);
            if (correctUser && responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                userIdFromSSO = Long.valueOf(responseVO.getResult());
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
            }
            if (userVo.getId() != null) {
                user = (User) session.load(User.class, userVo.getId());
                if (user == null) {
                    responseVO.setResultStatus(ResultStatus.INVALID_USER);
                    responseVO.setResult(ResultStatus.INVALID_USER.toString());
                    return responseVO;
                }
                contact = user.getContact();
            } else {

                boolean nullDataUser = UserService.Instance.checkNullUserInfo(userVo, responseVO);
                if (nullDataUser || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                user = new User();
                contact = new Contact();
            }

            if (userVo.getFirstName() != null && !userVo.getFirstName().trim().equals("")) {
                contact.setFirstName(userVo.getFirstName().trim());
            }
            if (userVo.getLastName() != null && !userVo.getLastName().trim().equals("")) {
                contact.setLastName(userVo.getLastName().trim());
            }

            if (userVo.getUserStatus() != null) {
                user.setUserStatus(userVo.getUserStatus());
            }
            if (userVo.getUserName() != null && !userVo.getUserName().trim().equals("")) {
                user.setUserName(userVo.getUserName().trim());
            }

            if (contact != null) {
                user.setContact(contact);
            }
            if (userVo.getRoles() != null && userVo.getRoles().size() > 0) {
                user.setRoles(userVo.getRoles());
            }
            user.setCreationDate(DateTime.now());
            if (userIdFromSSO != null) {
                user.setUserId(userIdFromSSO);
            }
            Transaction tx = session.beginTransaction();
            UserService.Instance.saveOrUpdate(user, session);
            tx.commit();
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule userModule = new SimpleModule();
            userModule.addSerializer(User.class, new UserJsonSerializer());
            objectMapper.registerModule(userModule);
            String userString = objectMapper.writeValueAsString(user);
            userString.replaceAll("\\\\", "");
            String replaceStr = userString.replaceAll("\\\\", "").replaceAll("\\\"", "\"");
            responseVO.setResult(replaceStr);
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


    @RequestMapping(value = "/getAllUserStatus", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getAllUserStatus(HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        try {

            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            PrincipalUtil.setCurrentUser(requesterUser);

            UserService.Instance.checkAccessibility(requesterUser, Access.USER, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            List<UserStatusVO> userStatusVoList = new ArrayList<>();
            List<UserStatus> userStatusList = UserStatus.listAll();
            for (int i = 1; i <= userStatusList.size(); i++) {
                UserStatus userStatus = new UserStatus(Byte.valueOf(String.valueOf(i)));
                UserStatusVO userStatusVO = new UserStatusVO();
                userStatusVO.setStatus(i);
                userStatusVO.setStatusSpec(userStatus.toString());
                userStatusVoList.add(userStatusVO);
            }
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult(JsonUtil.getJson(userStatusVoList));
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());

        }

        return responseVO;
    }

}
