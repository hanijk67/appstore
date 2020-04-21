package com.fanap.midhco.appstore.service.user;

import com.fanap.midhco.appstore.entities.Role;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.restControllers.vos.RoleVO;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.List;

/**
 * Created by admin123 on 6/22/2016.
 */
public class RoleService {
    public static RoleService Instance = new RoleService();

    private RoleService() {
    }

    private void applyCriteria(HQLBuilder builder, RoleCriteria roleCriteria) {
        if (roleCriteria.roleId != null)
            builder.addClause("and role.id = :roleId_", "roleId_", roleCriteria.roleId);
        if (roleCriteria.roleName != null && !roleCriteria.roleName.trim().equals("")) {
//            builder.addClause("and role.name like (:roleName_) ", "roleName_", HQLBuilder.like(roleCriteria.roleName));
            // to use full role name not part of role name
            builder.addClause("and  lower(role.name)= :roleName_ ", "roleName_", roleCriteria.roleName.trim().toLowerCase());
        }
        if (roleCriteria.isEditable != null)
            builder.addClause("and role.isEditable = :isEditable_", "isEditable_", roleCriteria.isEditable);

    }

    public int countAll() {
        return HibernateUtil.countAll(Role.class);
    }

    public Long count(RoleCriteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(role.id) ", "from Role role");
        if (criteria != null)
            applyCriteria(builder, criteria);
        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }

    public List<Role> list(RoleCriteria criteria, int first, int count, String sortProp, boolean isAsc, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select role ", "from Role role ");
        if (criteria != null)
            applyCriteria(builder, criteria);

        if (sortProp != null)
            builder.addOrder(sortProp, isAsc);

        Query query = builder.createQuery();

        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);
        return query.list();
    }

    public void saveOrUpdate(Role role, Session session) {
        if (role.getId() == null) {
            role.setCreationDate(DateTime.now());
            role.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            role.setLastModifyDate(DateTime.now());
            role.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(role);
    }

    public List<Role> listAll() {
        Session session = HibernateUtil.getCurrentSession();
        String queryString = "select role from Role role";
        Query query = session.createQuery(queryString);
        return query.list();
    }

    public boolean roleNameExist(Role role, Session session) {
        RoleCriteria roleCriteria = new RoleCriteria();
        roleCriteria.setRoleName(role.getName());
        List<Role> roleList = list(roleCriteria, 0, -1, null, false, session);
        if (roleList != null && roleList.size() > 0) {
            if (role.getId() != null) {
                for (Role roleInList : roleList) {
                    if (!roleInList.getId().equals(role.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static class RoleCriteria implements Serializable {
        Long roleId;
        String roleName;
        Boolean isEditable;

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        public Boolean getEditable() {
            return isEditable;
        }

        public void setEditable(Boolean editable) {
            isEditable = editable;
        }
    }

    public Role getDeveloperRole() {
        RoleCriteria roleCriteria = new RoleCriteria();
        roleCriteria.roleName = "developer";
        Session session = HibernateUtil.getNewSession();
        try {

            List<Role> roles = list(roleCriteria, 0, -1, null, true, session);
            if (!roles.isEmpty())
                return roles.get(0);
            return null;
        } finally {
            session.close();
        }
    }

    public Role getTesterRole() {
        RoleCriteria roleCriteria = new RoleCriteria();
        roleCriteria.roleName = "tester";
        Session session = HibernateUtil.getNewSession();
        try {

            List<Role> roles = list(roleCriteria, 0, -1, null, true, session);
            if (!roles.isEmpty())
                return roles.get(0);
            return null;
        } finally {
            session.close();
        }
    }

    public Role getRootRole() {
        RoleCriteria roleCriteria = new RoleCriteria();
        roleCriteria.roleName = "root";
        Session session = HibernateUtil.getNewSession();
        try {

            List<Role> roles = list(roleCriteria, 0, -1, null, true, session);
            if (!roles.isEmpty())
                return roles.get(0);
            return null;
        } finally {
            session.close();
        }
    }

    public ResponseVO checkInputRoleVo(RoleVO roleVO) {
        ResponseVO responseVO = new ResponseVO();
        if (roleVO == null) {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
        } else if (roleVO.getName() == null || roleVO.getName().trim().equals("") || roleVO.getEditable() == null) {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
        } else {
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
        }
        return responseVO;
    }
}
