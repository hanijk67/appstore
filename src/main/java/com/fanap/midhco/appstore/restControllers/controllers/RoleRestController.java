package com.fanap.midhco.appstore.restControllers.controllers;

import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.Role;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.restControllers.vos.RoleVO;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.user.RoleService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.apache.log4j.Logger;
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
 * Created by A.Moshiri on 3/28/2018.
 */
@RestController
@RequestMapping("/service/role")
public class RoleRestController {
    static private final Logger logger = Logger.getLogger(RoleRestController.class);

    @RequestMapping(value = "/listRoles", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO listRoles(@RequestParam(required = false) RoleVO roleVO, HttpServletRequest request, HttpServletResponse response) {

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
            UserService.Instance.checkAccessibility(requesterUser, Access.ROLE_LIST, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            PrincipalUtil.setCurrentUser(requesterUser);

            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            List<Role> roleList = new ArrayList<>();
            RoleService.RoleCriteria roleCriteria = new RoleService.RoleCriteria();
            session = HibernateUtil.getCurrentSession();
            Object resultObject = null;

            if (roleVO != null) {
                boolean hasValue = false;
                if (roleVO.getId() != null) {
                    roleCriteria.setRoleId(roleVO.getId());
                    hasValue = true;
                }
                if (roleVO.getName() != null && !roleVO.getName().trim().equals("")) {
                    roleCriteria.setRoleName(roleVO.getName());
                    hasValue = true;
                }

                if (roleVO.getEditable() != null) {
                    roleCriteria.setEditable(roleVO.getEditable());
                    hasValue = true;
                }
                if (!hasValue) {
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    responseVO.setResult("");
                    return responseVO;
                }
            }
            if (getResultCount) {
                Long resultCount = RoleService.Instance.count(roleCriteria, session);
                resultObject = resultCount;
            } else {
                String sortBy = PrincipalUtil.getSortBy();
                String sortProperties = null;
                if (sortBy != null) {
                    try {
                        List<String> declaredField = new Role().getDeclaredField();
                        if (declaredField.contains(sortBy)) {
                            sortProperties = sortBy;
                        }
                    } catch (Exception e) {
                        sortProperties = null;
                    }
                }
                roleList = RoleService.Instance.list(roleCriteria, fromIndex, countIndex, sortProperties, isAscending, session);

                if (roleList != null && !roleList.isEmpty()) {
                    Stream<RoleVO> roleVOStream = roleList.stream().map(roleInList -> RoleVO.buildRoleVOByRole(roleInList));
                    resultObject = roleVOStream.collect(Collectors.<RoleVO>toList());

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

    @RequestMapping(value = "/addRole", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO addRole(@RequestParam(required = true) RoleVO roleVO, HttpServletRequest request, HttpServletResponse response) {

        ResponseVO responseVO = new ResponseVO();
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (roleVO != null && roleVO.getId() != null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
            } else {
                UserService.Instance.checkAccessibility(requesterUser, Access.ROLE_CREATE, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);
                responseVO = saveRoleToDataBase(roleVO);
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }

    @RequestMapping(value = "editRole", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO editRole(@RequestParam(required = true) RoleVO roleVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();

        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (roleVO == null || roleVO.getId() == null) {
                responseVO.setResultStatus(ResultStatus.NULL_DATA);
                responseVO.setResult(ResultStatus.NULL_DATA.toString());
            } else {
                Long roleId = roleVO.getId();

                try {
                    PrincipalUtil.setCurrentUser(requesterUser);
                    Session session = HibernateUtil.getCurrentSession();
                    Role loadedRole = (Role) session.get(Role.class, roleId);
                    if (loadedRole == null) {
                        responseVO.setResultStatus(ResultStatus.INVALID_ROLE);
                        responseVO.setResult(ResultStatus.INVALID_ROLE.toString());
                        return responseVO;
                    }

                    UserService.Instance.checkAccessibility(requesterUser, Access.ROLE_EDIT, responseVO);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    responseVO = saveRoleToDataBase(roleVO);
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

    private ResponseVO saveRoleToDataBase(RoleVO roleVO) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {
            session = HibernateUtil.getNewSession();

            Role role = null;
            if (roleVO.getId() != null) {
                role = (Role) session.load(Role.class, roleVO.getId());
            } else {
                responseVO = RoleService.Instance.checkInputRoleVo(roleVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                role = new Role();
            }

            if (roleVO != null && roleVO.getName() != null && !roleVO.getName().trim().equals("")) {
                RoleService.RoleCriteria roleCriteria = new RoleService.RoleCriteria();
                roleCriteria.setRoleName(roleVO.getName().trim());

                List<Role> roleList = RoleService.Instance.list(roleCriteria, 0, -1, null, false, session);
                if (role.getId() == null) {
                    if (roleList != null && roleList.size() > 0) {
                        responseVO.setResultStatus(ResultStatus.ROLE_EXIST);
                        responseVO.setResult(ResultStatus.ROLE_EXIST.toString());
                        return responseVO;
                    }
                } else {
                    for (Role roleInList : roleList) {
                        if (!roleInList.getId().equals(role.getId())) {
                            responseVO.setResultStatus(ResultStatus.ROLE_EXIST);
                            responseVO.setResult(ResultStatus.ROLE_EXIST.toString());
                            return responseVO;
                        }
                    }
                }
                role.setName(roleVO.getName().trim());
            }

            if (roleVO.getEditable() != null) {
                role.setEditable(roleVO.getEditable());
            }
            if (roleVO.getAccessCodes() != null && !roleVO.getAccessCodes().trim().equals("")) {
                role.setAccessCodes(roleVO.getAccessCodes());
            }
            Transaction tx = session.beginTransaction();
            RoleService.Instance.saveOrUpdate(role, session);
            tx.commit();
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            RoleVO convertedRoleVO = RoleVO.buildRoleVOByRole(role);
            String roleString = JsonUtil.getJson(convertedRoleVO);
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

        return responseVO;
    }

}

