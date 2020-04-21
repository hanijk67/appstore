package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.Role;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by A.Moshiri on 3/25/2018.
 */
public class UserVO {

    private Long id;
    private Long userId;
    private String userName;
    private String firstName;
    private String fullName;
    private String lastName;
    private UserStatus userStatus;
    private Set<Role> roles;
    private Set<RoleVO> roleVOS;
    private User creatorUser;
    private Long creationDate;
    private Long modifyDate;
    private Long lastLoginDate;
    private Long lastLoginMinDate;
    private Long lastLoginMaxDate;

    private Long creationMinDate;
    private Long creationMaxDate;
    private String lastIp;
    private Set<UserStatus> statuses;
    private Set<User> creatorUsers;

    private Boolean logged;
    private Boolean registerInAppStore;
    private Integer userVoStatusInt;
    private String userVoStatusDesc;
    private String userNameContain;
    private String lastNameContain;

    public UserVO() {
    }

    public UserVO(String request) {
        JSONObject jsonObject = new JSONObject(request);

        this.userVoStatusInt = ResultStatus.UNSUCCESSFUL.getState();
        if (jsonObject.has("id")) {
            this.id = jsonObject.getLong("id");
        }
        if (jsonObject.has("userId")) {
            this.userId = jsonObject.getLong("userId");
        }

        if (jsonObject.has("userName")) {
            //this.userName = jsonObject.getString("userName");
            //for search in like mode not exactly equal mode
            this.userNameContain = jsonObject.getString("userName");
        }
        if (jsonObject.has("firstName")) {
            this.firstName = jsonObject.getString("firstName");
        }

        if (jsonObject.has("lastName")) {
            //this.lastName = jsonObject.getString("lastName");
            //for search in like mode not exactly equal mode
            this.lastNameContain = jsonObject.getString("lastName");
        }

        if (jsonObject.has("fullName")) {
            this.fullName = jsonObject.getString("fullName");
        }

        if (jsonObject.has("lastIp")) {
            this.lastIp = jsonObject.getString("lastIp");
        }
        if (jsonObject.has("userStatus")) {
            UserStatus inputUserStatus = new UserStatus(Byte.valueOf((String) jsonObject.get("userStatus")));
            if (inputUserStatus != null && inputUserStatus.toString()!=null) {
                this.userStatus = inputUserStatus;
            }
        }


        if (jsonObject.has("roleIds") || jsonObject.has("creatorUserId") || jsonObject.has("creatorUsersId")) {
            Session session = HibernateUtil.getNewSession();

            if (jsonObject.has("roleIds")) {
                JSONArray roleJasonArray = jsonObject.getJSONArray("roleIds");
                Set<Role> roleList = new HashSet<>();

                for (Object roleJsonObj : roleJasonArray) {

                    Role role = (Role) session.get(Role.class, Long.valueOf(roleJsonObj.toString()));
                    if (role != null) {
                        roleList.add(role);
                    }
                }
                if (roleList.size()>0) {
                    this.roles = roleList;
                }
            }

            if (jsonObject.has("creatorUserId")) {
                User creatorUser = (User) session.get(User.class, jsonObject.getLong("creatorUserId"));
                if (creatorUser != null) {
                    this.creatorUser = creatorUser;
                }
            }

            if (jsonObject.has("creatorUsersId")) {
                JSONArray creatorUsersJasonArray = jsonObject.getJSONArray("creatorUsersId");
                Set<User> creatorUsersList = new HashSet<>();

                for (Object creatorUsersObj : creatorUsersJasonArray) {
                    User creatorUser = (User) session.get(User.class, Long.valueOf(creatorUsersObj.toString()));
                    if (creatorUser != null) {
                        creatorUsersList.add(creatorUser);
                    }
                }
                if (creatorUsersList.size()>0) {
                    this.creatorUsers = creatorUsersList;
                }
            }

            session.close();

        }

        if (jsonObject.has("creationDate")) {
            this.creationDate = jsonObject.getLong("creationDate");
        }


        if (jsonObject.has("modifyDate")) {
            this.modifyDate = jsonObject.getLong("modifyDate");
        }

        if (jsonObject.has("creationMinDate")) {
            this.creationMinDate = jsonObject.getLong("creationMinDate");
        }

        if (jsonObject.has("creationMaxDate")) {
            this.creationMaxDate = jsonObject.getLong("creationMaxDate");
        }

        if (jsonObject.has("lastLoginDate")) {
            this.lastLoginDate = jsonObject.getLong("lastLoginDate");
        }

        if (jsonObject.has("lastLoginMaxDate")) {
            this.lastLoginMaxDate = jsonObject.getLong("lastLoginMaxDate");
        }

        if (jsonObject.has("lastLoginMinDate")) {
            this.lastLoginMinDate = jsonObject.getLong("lastLoginMinDate");
        }

        if (jsonObject.has("statuses")) {
            JSONArray statusJasonArray = jsonObject.getJSONArray("statuses");
            Set<UserStatus> userStatuses = new HashSet<>();

            for (Object statusJsonObj : statusJasonArray) {
                UserStatus status = new UserStatus(Byte.valueOf(statusJsonObj.toString()));
                if (status != null && status.toString()!=null) {
                    userStatuses.add(status);
                }
            }

            if (userStatuses.size()>0) {
                this.statuses = userStatuses;
            }
        }

        if (jsonObject.has("isLogged")) {
            if (jsonObject.get("isLogged") != null) {
                this.logged = jsonObject.getBoolean("isLogged");
            }
        }

        this.userVoStatusInt = ResultStatus.SUCCESSFUL.getState();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<RoleVO> getRoleVOS() {
        return roleVOS;
    }

    public void setRoleVOS(Set<RoleVO> roleVOS) {
        this.roleVOS = roleVOS;
    }


    public Set<UserStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(Set<UserStatus> statuses) {
        this.statuses = statuses;
    }

    public Set<User> getCreatorUsers() {
        return creatorUsers;
    }

    public void setCreatorUsers(Set<User> creatorUsers) {
        this.creatorUsers = creatorUsers;
    }


    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public Boolean getLogged() {
        return logged;
    }

    public void setLogged(Boolean logged) {
        this.logged = logged;
    }

    public Integer getUserVoStatusInt() {
        return userVoStatusInt;
    }

    public void setUserVoStatusInt(Integer userVoStatusInt) {
        this.userVoStatusInt = userVoStatusInt;
    }

    public String getUserVoStatusDesc() {
        return userVoStatusDesc;
    }

    public void setUserVoStatusDesc(String userVoStatusDesc) {
        this.userVoStatusDesc = userVoStatusDesc;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public Long getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Long lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Long getLastLoginMinDate() {
        return lastLoginMinDate;
    }

    public void setLastLoginMinDate(Long lastLoginMinDate) {
        this.lastLoginMinDate = lastLoginMinDate;
    }

    public Long getLastLoginMaxDate() {
        return lastLoginMaxDate;
    }

    public void setLastLoginMaxDate(Long lastLoginMaxDate) {
        this.lastLoginMaxDate = lastLoginMaxDate;
    }

    public Long getCreationMinDate() {
        return creationMinDate;
    }

    public void setCreationMinDate(Long creationMinDate) {
        this.creationMinDate = creationMinDate;
    }

    public Long getCreationMaxDate() {
        return creationMaxDate;
    }

    public void setCreationMaxDate(Long creationMaxDate) {
        this.creationMaxDate = creationMaxDate;
    }

    public String getUserNameContain() {
        return userNameContain;
    }

    public void setUserNameContain(String userNameContain) {
        this.userNameContain = userNameContain;
    }

    public String getLastNameContain() {
        return lastNameContain;
    }

    public void setLastNameContain(String lastNameContain) {
        this.lastNameContain = lastNameContain;
    }

    public Boolean getRegisterInAppStore() {
        return registerInAppStore;
    }

    public void setRegisterInAppStore(Boolean registerInAppStore) {
        this.registerInAppStore = registerInAppStore;
    }

    public static UserVO buildUserVo(User user) {
        if (user != null) {
            UserVO userVo = new UserVO();
            userVo.setId(user.getId());
            userVo.setUserId(user.getUserId());
            userVo.setUserName(user.getUserName());
            if (user.getContact() != null) {
                userVo.setFirstName(user.getContact().getFirstName());
                userVo.setLastName(user.getContact().getLastName());
            }
            userVo.setFullName(user.getFullName());
            Set<RoleVO> roleVOList = new HashSet<>();
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                Stream<RoleVO> roleVOStream = user.getRoles().stream().map(roleInList -> RoleVO.buildRoleVOByRole(roleInList));
                roleVOList = roleVOStream.collect(Collectors.<RoleVO>toSet());
            }
            userVo.setRoleVOS(roleVOList);
//            userVo.setRoles(user.getRoles());
            userVo.setUserStatus(user.getUserStatus());
//            userVo.setCreatorUser(user.getCreatorUser());
            if (user.getCreationDate() != null) {
                userVo.setCreationDate(user.getCreationDate().getTime());
            }
            if (user.getLastModifyDate() != null) {
                userVo.setModifyDate(user.getLastModifyDate().getTime());
            }
            userVo.setLastIp(user.getLastIp());
            if (user.getLastLoginDate() != null) {
                userVo.setLastLoginDate(user.getLastLoginDate().getTime());
            }
            userVo.setLogged(user.isLogged());

            return userVo;
        } else {
            return null;
        }
    }

}
