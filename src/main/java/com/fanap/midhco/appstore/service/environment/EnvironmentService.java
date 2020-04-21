package com.fanap.midhco.appstore.service.environment;

import com.fanap.midhco.appstore.entities.OSEnvironment;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.List;

/**
 * Created by A.Moshiri on 4/23/2018.
 */
public class EnvironmentService {

    public static EnvironmentService Instance = new EnvironmentService();

    private EnvironmentService() {
    }


    public static class EnvironmentCriteria implements Serializable {
        public Long envId;
        public String envName;

        public Long getEnvId() {
            return envId;
        }

        public void setEnvId(Long envId) {
            this.envId = envId;
        }

        public String getEnvName() {
            return envName;
        }

        public void setEnvName(String envName) {
            this.envName = envName;
        }
    }

    public void applyCriteria(HQLBuilder builder, EnvironmentCriteria environmentCriteria) {
        if (environmentCriteria.envId != null)
            builder.addClause("and ent.id = :id_", "id_", environmentCriteria.envId);

        if (environmentCriteria.envName != null && !environmentCriteria.envName.trim().isEmpty())
            builder.addClause("and ent.envName = :envName_", "envName_", environmentCriteria.envName);


    }

    public Long count(EnvironmentCriteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(ent.id) ", " from OSEnvironment ent ");
        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }

    public List<OSEnvironment> list(EnvironmentCriteria criteria, int first, int count, String sortProp, boolean isAsc, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from OSEnvironment ent ");
        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();

        if (sortProp != null)
            builder.addOrder(sortProp, isAsc);

        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);

        return query.list();
    }

    public List<OSEnvironment> listAll(Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from OSEnvironment ent ");
        Query query = builder.createQuery();
        List<OSEnvironment> osEnvironmentList = query.list();
        return osEnvironmentList;
    }

    public List<OSEnvironment> listAll() {
        Session session = HibernateUtil.getNewSession();
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from OSEnvironment ent ");
        Query query = builder.createQuery();
        List<OSEnvironment> environments = query.list();
        session.close();

        return environments;

    }


    public OSEnvironment getDefaultEnvironment() {
        Session session = HibernateUtil.getNewSession();
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from OSEnvironment ent ");
        String defaultOSName = "windows";
        builder.addClause("and lower(ent.envName)= :envName_", "envName_", defaultOSName.trim().toLowerCase());
        Query query = builder.createQuery();
        List<OSEnvironment> environments = query.list();
        session.close();

        if (environments!=null && !environments.isEmpty()) {
            return environments.get(0);
        }else {
            return null;
        }

    }

    public void saveOrUpdate(OSEnvironment environment, Session session) {
        if (environment.getId() == null) {
            environment.setCreationDate(DateTime.now());
            environment.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            environment.setLastModifyDate(DateTime.now());
            environment.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(environment);
    }


}
