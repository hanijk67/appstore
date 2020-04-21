package com.fanap.midhco.appstore.service;

import com.fanap.midhco.appstore.entities.IEntity;
import com.fanap.midhco.appstore.persistence.Dao;
import com.fanap.midhco.appstore.service.myException.ForeighKeyViolationException;
import com.fanap.midhco.appstore.service.myException.MyConstraintViolationException;
import com.fanap.midhco.appstore.service.myException.UniqueConstraintViolationException;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.proxy.HibernateProxyHelper;

import java.io.Serializable;
import java.sql.BatchUpdateException;
import java.util.List;
import java.util.Map;

public class HibernateUtil {
    static Logger logger = Logger.getLogger(HibernateUtil.class);
    private static ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
    private static Dao dao;

    static {
        dao = Dao.Instance;
    }

    public static void endTransaction() {
        Session session = currentSession.get();
        if (session != null && session.isOpen()) {
            Transaction transaction = session.getTransaction();
            try {
                if (transaction != null && transaction.isActive())
                    transaction.commit();
            } catch (RuntimeException ex) {
                try {
                    if (transaction != null)
                        transaction.rollback();
                } catch (Exception e) {
                }
                logger.error("Commit Problem: ", ex);
                throw ex;
            } finally {
                currentSession.remove();
                session.close();
            }
        }
    }

    public static void rollback() {
        Session session = currentSession.get();
        if (session != null && session.isOpen()) {
            Transaction transaction = session.getTransaction();
            try {
                if (transaction != null && !transaction.wasCommitted())
                    transaction.rollback();
            } catch (Exception ex) {
            }
            session.close();
            currentSession.set(null);
        }
    }

    public static void close() {
        Session session = currentSession.get();
        if (session != null && session.isOpen()) {
            session.close();
            currentSession.remove();
        }
    }

    public static void init() {
    }

    public static Session getNewSession() {
        Session session = dao.getNewSession();
        return session;
    }

    public static Session getCurrentSession() {
        Session session = currentSession.get();
        if (session == null || !session.isOpen()) {
            try {
                session = dao.getNewSession();
                String curUser = PrincipalUtil.getCurrentUser() != null ? PrincipalUtil.getCurrentUser().getUserName() : "-";
                if (logger.isDebugEnabled()) {
                    logger.debug("Get Current Session -> Open New Session");
                    RuntimeException x = new RuntimeException();
                    for (int i = 1; i < 6 && i < x.getStackTrace().length; i++)
                        logger.debug("\t" + x.getStackTrace()[i]);
                }
            } catch (HibernateException e) {
                if (session != null) {
                    session.reconnect(null);
                } else
                    throw e;
            }
            currentSession.set(session);
        }
        return currentSession.get();
    }

    public static void closeSessionFactory() {
        dao.closeSessionFactory();
    }

    public static void saveOrUpdate(Object obj, boolean doSave) {
        Session session = HibernateUtil.getCurrentSession();
        if (session.getTransaction() == null || !session.getTransaction().isActive())
            session.beginTransaction();
        try {
            if (doSave)
                session.save(obj);
            else
                session.update(obj);
            session.flush();
        } catch (HibernateException e) {
            logger.error(String.format("saveOrUpdate: %s", obj), e);
            throw processHibernateException(e);
        }
    }

    public static void saveOrUpdate(Object obj) {
        Session session = HibernateUtil.getCurrentSession();
        if (session.getTransaction() == null || !session.getTransaction().isActive())
            session.beginTransaction();
        try {
            session.saveOrUpdate(obj);
        } catch (HibernateException e) {
            logger.error(String.format("saveOrUpdate: %s", obj), e);
            throw processHibernateException(e);
        }
    }

    public static void delete(Object obj) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            session.delete(obj);
            session.flush();
        } catch (HibernateException e) {
            logger.error(String.format("delete: %s", obj), e);
            throw processHibernateException(e);
        }
    }

    public static <T extends IEntity> T reload(T entity) {
        if (entity == null || entity.getId() == null)
            return entity;
        Session session = HibernateUtil.getCurrentSession();
        if (!session.contains(entity))
            entity = (T) session.get(findClass(entity), (Serializable) entity.getId());
        return entity;
    }

    public static void evict(Object o) {
        getCurrentSession().evict(o);
    }

    public static Class findClass(Object obj) {
        return HibernateProxyHelper.getClassWithoutInitializingProxy(obj);
    }

    public static void clear() {
        getCurrentSession().clear();
    }

    public static <T extends IEntity> List<T> list(Class<T> entity) {
        try {
            Session session = getCurrentSession();
            Query query = session.createQuery(String.format("from %s ent", entity.getName()));
            return (List<T>) query.list();
        } catch (Exception e) {
            logger.error("HibernateUtil", e);
            throw new RuntimeException(e);
        }
    }

    public static <T extends IEntity> List<T> list(Class<T> entity, int index, int count) {
        Session session = getCurrentSession();
        String queryString = "from " + entity.getName();
        Query query = session.createQuery(queryString);
        query.setFirstResult(index);
        query.setMaxResults(count);
        List<T> result = query.list();
        return result;
    }

    public static Object findObject(String query, Map<String, Object> parameters) {
        Session session = getCurrentSession();
        Query q = session.createQuery(query);
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet())
                q.setParameter(entry.getKey(), entry.getValue());
        }
        List list = q.list();
        Object result;
        if (list == null || list.size() == 0)
            result = null;
        else
            result = list.get(0);
        return result;
    }

    public static List find(String query, Map<String, Object> parameters) {
        Session session = getCurrentSession();
        Query q = session.createQuery(query);
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet())
                q.setParameter(entry.getKey(), entry.getValue());
        }
        return q.list();
    }

    public static <T extends IEntity> T findById(Class<T> cls, Serializable id) {
        Session session = getCurrentSession();
        return (T) session.get(cls, id);
    }

    @Deprecated
    public static int countAll(Class cls) {
        Session session = getCurrentSession();
        return (Integer) session.createCriteria(cls).
                setProjection(Projections.count("id")).uniqueResult();
    }

    public static Object findRoot(Class cls, String parentProperty) {
        Session session = getCurrentSession();
        String q = String.format("from %s ent where ent.%s is null", cls.getName(), parentProperty);
        return session.createQuery(q).uniqueResult();
    }

    public static List findChildren(Class cls, String parentProperty, Object parentId) {
        Session session = getCurrentSession();
        String q = String.format("from %s ent where ent.%s.id=:parentId", cls.getName(), parentProperty);
        Query query = session.createQuery(q).setParameter("parentId", parentId);
        return query.list();
    }

    public static <T extends IEntity> List<T> findChildrenWithoutId(Class<T> cls, String parentProperty, Object parentId) {
        Session session = getCurrentSession();
        String q = String.format("from %s ent where ent.%s=:parentId", cls.getName(), parentProperty);
        Query query = session.createQuery(q).setParameter("parentId", parentId);
        return query.list();
    }

    public static AppStoreRuntimeException processHibernateException(HibernateException he) {
        if (he instanceof ConstraintViolationException) {
            if (he.getCause() instanceof BatchUpdateException && he.getCause().getMessage().contains("Cannot delete or update a parent row"))
                return new ForeighKeyViolationException(he.getCause());
            else if (he.getCause().getMessage().contains("unique constraint"))
                return new UniqueConstraintViolationException(he.getCause());
            else
                return new MyConstraintViolationException(he.getCause());
        }
        return new AppStoreRuntimeException(he);
    }

    public static String getCurrentSessionId() {
        Object[] d = (Object[]) getCurrentSession().createSQLQuery("select sys_context('USERENV','sid')," +
                "sys_context('USERENV','instance'),sys_context('USERENV','instance_name') from dual").uniqueResult();
        return (String) d[0];
    }
}
