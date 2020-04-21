package com.fanap.midhco.appstore.service.user;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.Gender;
import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;
import com.fanap.midhco.appstore.restControllers.exceptions.PreProcessorException;
import com.fanap.midhco.appstore.restControllers.vos.ErrorPhrases;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.restControllers.vos.UserVO;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.handlerApp.HandlerAppService;
import com.fanap.midhco.appstore.service.login.JWTService;
import com.fanap.midhco.appstore.service.login.SSOUserService;
import com.fanap.midhco.appstore.service.myException.DisabledUserException;
import com.fanap.midhco.appstore.service.myException.SSOServerException;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.security.AccessService;
import com.fanap.midhco.appstore.service.security.PasswordService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.util.*;

public class UserService {

    private static User preProcess_User() throws Exception {
        if (PrincipalUtil.getJwtTokenUser() == null) {
            throw new PreProcessorException(ErrorPhrases.NO_JWT_TOKEN_USER_RECIEVED_OR_UNKNOWN_USER.getMessage());
        }
        return PrincipalUtil.getJwtTokenUser();
    }

    public static UserService Instance = new UserService();

    private UserService() {
    }

    public boolean hasPermission(User user, Access access) {
        if(PrincipalUtil.isCurrentUserRoot())
            return true;

        return user != null && !AccessService.hasPermission(user.getAllDeniedPermissions(), access) &&
                AccessService.hasPermission(user.getAllAllowedPermissions(), access);
    }

    public static boolean hasPermission(User user, Access[] accesses) {
        if(PrincipalUtil.isCurrentUserRoot())
            return true;

        for (Access access : accesses) {
            boolean authorized = user != null && !AccessService.hasPermission(user.getAllDeniedPermissions(), access) &&
                    AccessService.hasPermission(user.getAllAllowedPermissions(), access);
            if (authorized)
                return true;
        }
        return false;
    }

    public boolean isRoot(User currentUser) {
        return false;
    }

    public User findUser(String username, Session session) {
        String queryString = "select user from User user where upper(userName) = :userName_ ";
        Query query = session.createQuery(queryString);
        query.setParameter("userName_", username.toUpperCase());
        List<User> userList = query.list();
        if (!userList.isEmpty())
            return userList.get(0);
        return null;
    }

    public User findUserWithUserId(Long userId, Session session) {
        String queryString = "select user from User user where userId = :userId ";
        Query query = session.createQuery(queryString);
        query.setParameter("userId", userId);
        List<User> userList = query.list();
        if (!userList.isEmpty())
            return userList.get(0);
        return null;
    }

    public void logout(String userName, Session session) {
        User user = findUser(userName, session);
        if (user != null) {
            user.setLogged(false);
        }
        HibernateUtil.saveOrUpdate(user);
    }

    public boolean verifyUserPassword(User user, String password) {
        if (user == null || password == null)
            return false;
        String salt = user.getPasswordSalt();
        String encryptedPassword = PasswordService.encrypt(password, salt);
        return encryptedPassword.equals(user.getPassword());
    }

    public boolean userNewPassEqualsOldPass(User user, String newPass) {
        if (user == null || newPass == null)
            return false;
        String salt = user.getPasswordSalt();
        String newPassEncrypted = PasswordService.encrypt(newPass, salt);
        if (newPassEncrypted.equals(user.getPassword()))
            return true;
        return false;
    }

    public void changeUserPassword(User user, String password) {
        String salt = PasswordService.generateSalt();
        String encryptedPassword = PasswordService.encrypt(password, salt);
        user.setPasswordSalt(salt);
        user.setPassword(encryptedPassword);
    }

    public User authenticate(String username, String password, String ip, Session session, boolean lock) throws LoginException, DisabledUserException {
        User user = findUser(username, session);

        if (user != null) {
            if (!UserStatus.ENABLED.equals(user.getUserStatus()))
                throw new DisabledUserException(user.getUserStatus());
            if (lock)
                session.lock(user, LockMode.UPGRADE_NOWAIT);
        }

        Integer MAX_WRONG_LOGIN =
                Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_MAX_WRONG_LOGIN));

        if (user == null || !verifyUserPassword(user, password)) {
            if (user != null && !"root".equals(username)) {
                byte tries = 0;
                if (user.getNumOfWrongTries() != null)
                    tries = user.getNumOfWrongTries();
                user.setNumOfWrongTries(++tries);
                if (tries == MAX_WRONG_LOGIN) {
                    user.setUserStatus(UserStatus.SECURITY_BLOCKED);
                    user.setNumOfWrongTries((byte) 0);
                }
                if (tries == MAX_WRONG_LOGIN) {
                    saveOrUpdate(user, session);
                    throw new DisabledUserException(UserStatus.SECURITY_BLOCKED);
                }
            }
            throw new LoginException("Bad password");
        }

        if (ip != null) {
            user.setLastIp(ip);
            user.setLogged(true);
        }

        user.setLastLoginDate(DateTime.now());
        user.setNumOfWrongTries((byte) 0);
        saveOrUpdate(user, session);

        //TODO : set allowed and denied permissions here!
//        user.setAllDeniedPermissions(AccessService.getPermissionsBytes(user.getDeniedPermissions()));
        populateUserPermissions(user);

        return user;
    }

    public void populateUserPermissions(User user) {
        byte[] allowed = null;
        boolean first = true;
        for (Role role : user.getRoles()) {
            if(first)
                allowed = AccessService.getPermissionsBytes(role.getAccessCodes());
            else
                AccessService.addPermissions(allowed, role.getAccessCodes());
        }
        user.setAllAllowedPermissions(allowed);
    }

    public void saveOrUpdate(User user, Session session) {
        if (user.getId() == null) {
            user.setCreationDate(DateTime.now());
            user.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            user.setLastModifyUser(PrincipalUtil.getCurrentUser());
            user.setLastModifyDate(DateTime.now());
        }
        session.saveOrUpdate(user);
    }

    public static class UserCriteria implements Serializable {
        public Long id;
        public String userName;
        public String userNameContain;
        public String firstName;
        public String lastNameContain;
        public String lastName;
        public String nationalCode;
        public String lastIp;
        public DateTime[] lastLoginDateTime;
        public Boolean logged;
        public Collection<UserStatus> statuses;
        public Collection<Role> roles;
        public Gender gender;
        public Collection<User> creatorUsers;
        public DateTime[] creationDateTime;
        public Long userId;
    }

    public void applyCriteria(HQLBuilder builder, UserCriteria criteria) {
        if (criteria.id != null)
            builder.addClause("and ent.id=:id_", "id_", criteria.id);

        if (criteria.userId != null)
            builder.addClause("and ent.userId=:userId_", "userId_", criteria.userId);

        if (criteria.userName != null && !criteria.userName.isEmpty())
            builder.addClause("and lower(ent.userName) =:userName_", "userName_", criteria.userName.toLowerCase());
        if (criteria.firstName != null && !criteria.firstName.trim().isEmpty())
            builder.addClause("and ent.contact.firstName=:firstName_", "firstName_", criteria.firstName);
        if (criteria.lastName != null && !criteria.lastName.trim().isEmpty())
            builder.addClause("and ent.contact.lastName=:lastName_", "lastName_", criteria.lastName);
        if (criteria.lastNameContain != null && !criteria.lastNameContain.trim().equals(""))
            builder.addClause(" and lower(ent.contact.lastName) like(:lastNameContain_)", "lastNameContain_" , HQLBuilder.like(criteria.lastNameContain));

        if (criteria.roles != null && !criteria.roles.isEmpty()) {
            builder.addClause("and ( select count(r) from ent.roles r where r in (:roles) ) >= :n ", "roles", criteria.roles);
            builder.addParam("n", 1L);
        }
        if(criteria.nationalCode != null) {
            builder.addClause("and ent.contact.nationalCode = :nationalCode_", "nationalCode_", criteria.nationalCode);
        }

        if(criteria.lastIp != null)
            builder.addClause("and ent.lastIp = :lastIP_", "lastIP_", criteria.lastIp);

        if(criteria.lastLoginDateTime != null)
            builder.addDateTimeRange("ent", "lastLoginDate", "lloginDateTime", "uloginDateTime", criteria.lastLoginDateTime);

        if(criteria.logged != null)
            builder.addClause("and ent.logged = :logged_", "logged_", criteria.logged);

        if(criteria.statuses != null && !criteria.statuses.isEmpty())
            builder.addClause("and ent.userStatus in (:userStatues_) ", "userStatues_", criteria.statuses);

        if(criteria.gender != null)
            builder.addClause("and ent.gender = :gender_ ", "gender_", criteria.gender);

        if(criteria.creatorUsers != null && !criteria.creatorUsers.isEmpty())
            builder.addClause("and ent.creatorUser in (:creatorUsers_)", "creatorUsers_", criteria.creatorUsers);

        if(criteria.creationDateTime != null)
            builder.addDateTimeRange("ent", "creationDate", "lcreationDate", "ucreationDate", criteria.creationDateTime);



        if (criteria.userNameContain != null && !criteria.userNameContain.trim().equals(""))
            builder.addClause(" and lower(ent.userName) like(:userNameContain_)", "userNameContain_" , HQLBuilder.like(criteria.userNameContain));

    }

    public List<User> list(UserCriteria cri, int first, int count, String sortProp, boolean isAscending, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select distinct(ent) ", " from User ent  ");

        if (cri != null)
            applyCriteria(builder, cri);

        if (sortProp != null)
            builder.addOrder(sortProp, isAscending);
        Query query = builder.createQuery();

        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);

        return query.list();
    }

    public List<User> listAllUser() {
        Session session = HibernateUtil.getCurrentSession();
        HQLBuilder builder = new HQLBuilder(session, "select distinct(ent) ", " from User ent  ");
        Query query = builder.createQuery();
        return query.list();
    }

    public Long count(UserCriteria cri, String sortProp, boolean isAscending, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(ent.id) ", " from User ent  ");

        if (cri != null)
            applyCriteria(builder, cri);

        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }


    public boolean checkCorrectInputUser(UserVO userVo, ResponseVO responseVO) {
        String userName = userVo.getUserName();
        Long userIdFromSSO = null;
        if (userName != null && !userName.trim().equals("")) {
            Session session = HibernateUtil.getCurrentSession();
            try {

                userIdFromSSO = SSOUserService.Instance.getUserIdByUserName(userName);

                User user = UserService.Instance.getUserBySSOId(userIdFromSSO, session);
                if (user != null) {
                    // in edit mode we shouldn't get any error for existence of userName
                    if (userVo.getId() == null || (userVo.getId() != null && !userVo.getId().equals(user.getId()))) {
                        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                        responseVO.setResult(AppStorePropertyReader.getString("User.userNameExists"));
                        return false;
                    }

                }
                User userByUserId = UserService.Instance.findUserWithUserId(userVo.getUserId(), session);
                if (userByUserId != null) {
                    // in edit mode we shouldn't get any error for existence of userId
                    if (userVo.getId() == null || (userVo.getId() != null && !userVo.getId().equals(userByUserId.getId()))) {
                        responseVO.setResultStatus(ResultStatus.INVALID_USER);
                        responseVO.setResult(ResultStatus.INVALID_USER.toString());
                        return false;
                    }
                }

                if (userIdFromSSO.compareTo(Long.valueOf(1)) == -1) {
                    responseVO.setResultStatus(ResultStatus.USER_NOT_IN_SSO);
                    responseVO.setResult(AppStorePropertyReader.getString("User.userName.notExistInSSOServer"));
                    return false;
                }
            } catch (SSOServerException e) {
                if (e.getErrorMessage() != null && !e.getErrorMessage().trim().isEmpty()) {
                    if (e.getErrorMessage().equals(new ResourceModel("sso.invalid.request").getObject())) {
                        responseVO.setResultStatus(ResultStatus.USER_NOT_IN_SSO);
                        responseVO.setResult(AppStorePropertyReader.getString("User.userName.notExistInSSOServer"));
                        return false;
                    } else {
                        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                        responseVO.setResult(AppStorePropertyReader.getString("error.generalErr"));
                        return false;
                    }
                }
            } catch (Exception e) {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(AppStorePropertyReader.getString("general error occurred in  getting user Id from SSO service "));
                return false;
            }
        } else {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
            return false;
        }
        responseVO.setResult(userIdFromSSO.toString());
        responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        return true;
    }

    public boolean checkNullUserInfo(UserVO userVo, ResponseVO responseVO) {
        String firstName = userVo.getFirstName();
        String lastName = userVo.getLastName();
        UserStatus userStatus = userVo.getUserStatus();
        Set<Role> roleSet = userVo.getRoles();

        if (firstName == null || firstName.trim().equals("") || lastName == null || lastName.trim().equals("") ||
                userStatus == null || roleSet == null || userVo.getLogged() == null) {
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            return true;
        }
        if (roleSet.size() == 0) {
            responseVO.setResultStatus(ResultStatus.INVALID_ROLE);
            responseVO.setResult(ResultStatus.INVALID_ROLE.toString());
            return true;
        }
        return false;
    }


    public static void main(String[] args) {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();

        try {
            Role role1 = new Role();
            role1.setName("root");
            role1.setEditable(false);
            session.saveOrUpdate(role1);

            Role role2 = new Role();
            role2.setName("tester");
            role2.setEditable(false);
            session.saveOrUpdate(role2);

            Role role3 = new Role();
            role3.setName("developer");
            role3.setEditable(false);
            session.saveOrUpdate(role3);

            User user = new User();
            user.setUserName("khan1");
            Contact contact = new Contact();
            contact.setFirstName("root");
            contact.setLastName("root");
            user.setContact(contact);

            Contact secondContact = new Contact();
            secondContact.setFirstName("root");
            secondContact.setLastName("root");
            user.setRoles(new HashSet<>());
            user.getRoles().add(role1);
            Long userIdFromSSO = null;
            Long secondUserIdFromSSO = null;
            User secondUser = new User();
            secondUser.setUserName("na");
            secondUser.setContact(secondContact);
            secondUser.setRoles(new HashSet<>());
            secondUser.getRoles().add(role2);
            secondUser.getRoles().add(role3);
            userIdFromSSO = SSOUserService.Instance.getUserIdByUserName(user.getUserName());
            secondUserIdFromSSO = SSOUserService.Instance.getUserIdByUserName(secondUser.getUserName());
            if (userIdFromSSO.compareTo(Long.valueOf(1)) != -1) {
                user.setUserId(userIdFromSSO);
            }
            if (secondUserIdFromSSO.compareTo(Long.valueOf(1)) != -1) {
                secondUser.setUserId(secondUserIdFromSSO);
            }
            session.saveOrUpdate(user);
            session.saveOrUpdate(secondUser);
            tx.commit();

            tx = session.beginTransaction();
            user = UserService.Instance.findUser("khan1", session);
//            Instance.changeUserPassword(user, "root");
            HibernateUtil.saveOrUpdate(user);
            tx.commit();

            OSType osType = new OSType();
            osType.setName("ANDROID");
            osType.setOsCompareScript("Os compare script");

            tx = session.beginTransaction();
            session.saveOrUpdate(osType);


            List<OS> osList = OSService.Instance.listAll(session);
            for (OS os : osList) {
                String filePath = os.getHandlerAppDownloadPath();
                String testFilePath = filePath;
                HandlerAppService.Instance.getAndSetHandlerAppList2(session, 0L, true, filePath, testFilePath, os);
            }

            tx.commit();

        } catch (Exception ex) {
            ex.printStackTrace();
            if(tx != null)
                tx.rollback();
        } finally {
            session.close();
        }

//        Session newSession = HibernateUtil.getNewSession();
//        try {
//            tx = newSession.beginTransaction();
//            OS os = new OS();
//            os.setName("ANDROID");
//            os.setVersion(6l);
//            os.setHandlerAppDownloadPath("Handler");
//            os.setOsType(OSTypeService.Instance.listAll().get(0));
//            newSession.saveOrUpdate(os);
//            tx.commit();
//        } finally {
//            newSession.close();
//        }

        System.exit(0);
    }

    public boolean isUserDeveloper(User user) {
        Session session = HibernateUtil.getNewSession();
        try {
            Role developerRole = RoleService.Instance.getDeveloperRole();
            user = (User)session.load(User.class, user.getId());
            if(user.getRoles() != null && new ArrayList<>(user.getRoles()).indexOf(developerRole)!= -1)
                return true;
        } finally {
            session.close();
        }
        return false;
    }

    public boolean isUserRoot(User user) {
        Session session = HibernateUtil.getNewSession();
        try {
            Role rootRole = RoleService.Instance.getRootRole();
            user = (User)session.load(User.class, user.getId());
            if(user.getRoles() != null && user.getRoles().contains(rootRole))
                return true;
        } finally {
            session.close();
        }
        return false;
    }

    public void checkAccessibility(JWTService.JWTUserClass jwtUser, Access accessDetail, ResponseVO responseVO, Session session) {

        if (jwtUser == null || jwtUser.getUserId() == null || jwtUser.getUserId().trim().equals("") || accessDetail == null || session == null) {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
            return;
        }
        List<User> userList = new ArrayList<>();
        UserService.UserCriteria userCriteria = new UserService.UserCriteria();
        userCriteria.userId = Long.valueOf(jwtUser.getUserId());
        userList = UserService.Instance.list(userCriteria, 0, -1, "id", false, session);
        if (userList == null || userList.size() == 0) {
            responseVO.setResult(ResultStatus.USER_NOT_DEFINED_IN_APPSTORE.toString());
            responseVO.setResultStatus(ResultStatus.USER_NOT_DEFINED_IN_APPSTORE);
            return;
        }

        int atLeastOneValidUser = 0;
        for (User user : userList) {
            if (UserService.Instance.isUserRoot(user)) {
                atLeastOneValidUser++;
            } else {
                populateUserPermissions(user);
                if (AccessService.hasPermission(user.getAllAllowedPermissions(), accessDetail)) {
                    atLeastOneValidUser++;
                }
            }
        }
        if (atLeastOneValidUser == 0) {
            responseVO.setResult(ResultStatus.INVALID_ACCESS.toString());
            responseVO.setResultStatus(ResultStatus.INVALID_ACCESS);
            return;
        }

        return;
    }


    public void checkAccessibility(User user, Access accessDetail, ResponseVO responseVO) {
        responseVO.setResultStatus(ResultStatus.INVALID_ACCESS);

        if (user == null || accessDetail == null) {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
            return;
        }
        if (UserService.Instance.isUserRoot(user)) {
            responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            return;
        } else {
            populateUserPermissions(user);
            if (AccessService.hasPermission(user.getAllAllowedPermissions(), accessDetail)) {
                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                return;
            }
        }
        responseVO.setResult(ResultStatus.INVALID_ACCESS.toString());
        responseVO.setResultStatus(ResultStatus.INVALID_ACCESS);
        return;
    }

    public void checkAccessibility(User user, List<Access> accessDetailList, ResponseVO responseVO) {
        responseVO.setResultStatus(ResultStatus.INVALID_ACCESS);

        if (user == null || accessDetailList == null || accessDetailList.isEmpty()) {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
            return;
        }
        if (UserService.Instance.isUserRoot(user)) {
            responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            return;
        } else {
            populateUserPermissions(user);
            for (Access accessDetail : accessDetailList) {
                if (!AccessService.hasPermission(user.getAllAllowedPermissions(), accessDetail)) {
                    responseVO.setResult(ResultStatus.INVALID_ACCESS.toString());
                    responseVO.setResultStatus(ResultStatus.INVALID_ACCESS);
                    return;
                }
            }
        }
        responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
        responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        return;
    }


    public JWTService.JWTUserClass getJwtUserByGivenToken(String jwtToken, ResponseVO responseVO) {
        checkUser(jwtToken, responseVO);

        JWTService.JWTUserClass jwtUser = null;
        if (responseVO != null && responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
            jwtUser = JWTService.extractUserInfo(jwtToken);
        }
        return jwtUser;
    }


    public ResponseVO checkUser(String jwtToken, ResponseVO responseVO) {
        try {
            Long userId;
            userId = JWTService.validateAndGetUser(jwtToken);
            JWTService.JWTUserClass userClass = JWTService.extractUserInfo(jwtToken);
            boolean invalidUser = checkUserIdAndSetResponseVO(userClass.getUserName(), responseVO);
            if (invalidUser) {
                responseVO.setResultStatus(ResultStatus.USER_NOT_IN_SSO);
                responseVO.setResult(ResultStatus.USER_NOT_IN_SSO.toString());

                return responseVO;
            }
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult(userId.toString());
            return responseVO;
        } catch (JWTVerificationException e) {
            responseVO.setResultStatus(ResultStatus.INVALID_TOKEN);
            responseVO.setResult(ResultStatus.INVALID_TOKEN.toString());
            return responseVO;
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        }
    }

    public boolean checkUserIdAndSetResponseVO(String userName, ResponseVO responseVO) {
        boolean invalidUser = false;
        try {
            if (userName != null && !userName.trim().equals("")) {
//                if (SSOUserService.Instance.getUserIdByUserName(userName).equals(Long.valueOf(-1))) {
//                    invalidUser = true;
//                    responseVO.setResult(AppStorePropertyReader.getString("label.sso.user.name.error").replace("${userName}", userName));
//                }
            } else {
                invalidUser = true;
                responseVO.setResult(AppStorePropertyReader.getString("error.userId.required"));
            }
            return invalidUser;
        } catch (Exception e) {
            return false;
        } finally {
            return invalidUser;
        }
    }

    public User getUserFromJwtToken(ResponseVO responseVO) {
        User requesterUser;
        try {
            requesterUser = preProcess_User();
            if (requesterUser == null) {
                responseVO.setResultStatus(ResultStatus.NO_JWT_TOKEN_RECEIVED);
                responseVO.setResult(ResultStatus.NO_JWT_TOKEN_RECEIVED.toString());
                return null;
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.NO_JWT_TOKEN_RECEIVED);
            responseVO.setResult(ResultStatus.NO_JWT_TOKEN_RECEIVED.toString());
            return null;
        }

        responseVO.setResult(requesterUser.toString());
        responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        return requesterUser;
    }


    public User getUserBySSOId(Long userSSOId, Session session) {
        String queryString = "select user from User user where user.userId = :userId_";
        Query query = session.createQuery(queryString);
        query.setParameter("userId_", userSSOId);
        List<User> userList = query.list();
        if(!userList.isEmpty())
            return userList.get(0);
        return null;
    }
}
