package com.fanap.midhco.appstore.service.os;

import com.fanap.midhco.appstore.entities.OS;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.vos.OSVO;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.List;

/**
 * Created by admin123 on 6/28/2016.
 */
public class OSService {
    public static OSService Instance = new OSService();

    private OSService() {}

    public static class OSCriteria implements Serializable {
        public Long osId;
        public String osName;
        public String osCode;
        public String osVersion;
        public List<OSType> osType;
        public Boolean disabled;

        public Long getOsId() {
            return osId;
        }

        public void setOsId(Long osId) {
            this.osId = osId;
        }

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public List<OSType> getOsType() {
            return osType;
        }

        public void setOsType(List<OSType> osType) {
            this.osType = osType;
        }

        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
        }

        public String getOsCode() {
            return osCode;
        }

        public void setOsCode(String osCode) {
            this.osCode = osCode;
        }
    }

    public void applyCriteria(HQLBuilder builder, OSCriteria osCriteria) {
        if(osCriteria.osId != null)
            builder.addClause("and ent.id = :id_", "id_", osCriteria.osId);

        if(osCriteria.osName != null && !osCriteria.osName.trim().isEmpty())
            builder.addClause("and ent.osName = :osName_", "osName_", osCriteria.osName);

        if (osCriteria.osCode != null && !osCriteria.osCode.trim().isEmpty())
            builder.addClause("and ent.osCode = :osCode_", "osCode_", osCriteria.osCode);

        if(osCriteria.osVersion != null && !osCriteria.osVersion.isEmpty())
            builder.addClause("and ent.osVersion = :osversion_", "osversion_", osCriteria.osVersion);

        if(osCriteria.osType != null && !osCriteria.osType.isEmpty())
            builder.addClause("and ent.osType in (:osType_)", "osType_", osCriteria.osType);


        if (osCriteria.disabled != null) {
            builder.addClause("and ent.disabled = :disabled_", "disabled_", osCriteria.disabled);
        }

    }

    public Long count(OSCriteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(ent.id) ", " from OS ent ");
        if(criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }

    public List<OS> getEnabledOS() {
        Session session = HibernateUtil.getNewSession();
        try {
            String queryString = "select os from OS os where os.disabled is null or os.disabled = :disabled_";
            Query query = session.createQuery(queryString);
            query.setParameter("disabled_", false);
            return query.list();
        } finally {
            session.close();
        }
    }

    public List<OS> list(OSCriteria criteria, int first, int count, String sortProp, boolean isAsc, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from OS ent ");
        if(criteria != null)
            applyCriteria(builder, criteria);

        if(sortProp != null)
            builder.addOrder(sortProp, isAsc);
        Query query = builder.createQuery();

        query.setFirstResult(first);
        if(count != -1)
            query.setMaxResults(count);

        return query.list();
    }

    public List<OS> listAll(Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from OS ent ");
        Query query = builder.createQuery();
        return query.list();
    }


    public void saveOrUpdate(OS os, Session session) {
        if(os.getId() == null) {
            os.setCreationDate(DateTime.now());
            os.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            os.setLastModifyDate(DateTime.now());
            os.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(os);
    }

    public List<OS> getOSForOSType(OSType osType) {
        Session session = HibernateUtil.getCurrentSession();
        String queryString = "select os from OS os where os.osType = :osType_";
        Query query = session.createQuery(queryString);
        query.setParameter("osType_", osType);
        return query.list();
    }

    public OS loadOSByOSId(Long osId, Session session) {
        String queryString = "select os from OS os where os.id = :id_";
        Query query = session.createQuery(queryString);
        query.setParameter("id_", osId);
        List<OS> osList = query.list();
        if(!osList.isEmpty())
            return osList.get(0);
        return null;
    }


    public OS loadOSByOSId(Long osId) {
        Session session = HibernateUtil.getNewSession();
        OS loadedOs = loadOSByOSId(osId , session);
        session.close();
        return loadedOs;

    }

    public static ResponseVO checkInputOsVo(OSVO osVO) {
        ResponseVO responseVO = new ResponseVO();
        if (osVO == null || osVO.getDisabled() == null || osVO.getOsVersion() == null || osVO.getOsVersion().trim().equals("") || osVO.getOsName() == null || osVO.getOsName().trim().equals("")
                || osVO.getOsType() == null ) {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
        } else {
            responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        }
        return responseVO;
    }

}
