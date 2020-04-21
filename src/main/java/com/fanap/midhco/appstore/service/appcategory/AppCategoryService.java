package com.fanap.midhco.appstore.service.appcategory;

import com.fanap.midhco.appstore.entities.AppCategory;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.vos.CategoryVO;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

/**
 * Created by admin123 on 2/12/2017.
 */
public class AppCategoryService {
    public static final AppCategoryService Instance = new AppCategoryService();

    private AppCategoryService() {
    }

    public static class AppCategoryCriteria implements Serializable {
        Long id;
        Long parentId;
        String categoryName;
        Boolean isAssignable;
        OSType osType;
        Boolean isEnabled;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public Boolean getAssignable() {
            return isAssignable;
        }

        public void setAssignable(Boolean assignable) {
            isAssignable = assignable;
        }

        public OSType getOsType() {
            return osType;
        }

        public void setOsType(OSType osType) {
            this.osType = osType;
        }

        public Boolean getEnabled() {
            return isEnabled;
        }

        public void setEnabled(Boolean enabled) {
            isEnabled = enabled;
        }
    }

    private void appCategoryCriteria(HQLBuilder builder, AppCategoryCriteria criteria) {
        if(criteria.id != null)
            builder.addClause("and ent.id = (:id_)", "id_", criteria.id);

        if (criteria.parentId != null)
            builder.addClause("and ent.parent.id = (:parentId_)", "parentId_", criteria.parentId);

        if (criteria.categoryName != null && !criteria.categoryName.trim().equals(""))
            builder.addClause("and lower(ent.categoryName)= :categoryName_", "categoryName_", criteria.categoryName.trim().toLowerCase());

        if (criteria.isAssignable != null)
            builder.addClause("and ent.isAssignable = :isAssignable_", "isAssignable_", criteria.isAssignable);

        if(criteria.isEnabled != null)
            builder.addClause("and ent.isEnabled = :isEnabled_", "isEnabled_", criteria.isEnabled);

    }

    public Long count(AppCategoryCriteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(ent.id) ", "from AppCategory ent ");

        if (criteria != null)
            appCategoryCriteria(builder, criteria);

        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }


    public List<AppCategory> list(AppCategoryCriteria criteria, int first, int count, String sortProp, boolean isAscending, Session session) {

        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from AppCategory ent ");

        if (criteria != null)
            appCategoryCriteria(builder, criteria);

        if (sortProp != null)
            builder.addOrder(sortProp, isAscending);

        Query query = builder.createQuery();
        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);

        return query.list();
    }

    public AppCategory loadCategoryById(Long categoryId, Session session) {
        String queryString = "select appcategory from AppCategory appcategory where appcategory.id = :id_";
        Query query = session.createQuery(queryString);
        query.setParameter("id_", categoryId);
        List<AppCategory> appCategoryList = query.list();
        if(!appCategoryList.isEmpty())
            return appCategoryList.get(0);
        return null;
    }

    public AppCategory loadRootAppCategory(Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from AppCategory ent where parent is null");
        Query query = builder.createQuery();
        List<AppCategory> appCategoryList = query.list();
        if (appCategoryList != null && !appCategoryList.isEmpty())
            return appCategoryList.get(0);
        return null;
    }

    public List<AppCategory> listAll() {
        Session session = HibernateUtil.getCurrentSession();
        String queryString = "select appCategory from AppCategory appCategory";
        Query query = session.createQuery(queryString);
        return query.list();
    }


    public List<AppCategory> listAllAssignable() {
        Session session = HibernateUtil.getCurrentSession();
        try {
            String queryString = "select appCategory from AppCategory appCategory where appCategory.isAssignable = :isAssignable_";
            Query query = session.createQuery(queryString);
            query.setParameter("isAssignable_", true);
            return query.list();
        } finally {
            session.close();
        }
    }


    public List<AppCategory> listAllEnable() {
        Session session = HibernateUtil.getCurrentSession();
        String queryString = "select appCategory from AppCategory appCategory where appCategory.isEnabled =:isEnabled_ ";
            Query query = session.createQuery(queryString);
        query.setParameter("isEnabled_", true);
            return query.list();
    }

    public void insertRootCategory() {
        Session session = HibernateUtil.getCurrentSession();
        try {
            AppCategory rootCategory = loadRootAppCategory(session);
            if (rootCategory == null) {
                rootCategory = new AppCategory();
                rootCategory.setParent(null);
                rootCategory.setEnabled(true);
                rootCategory.setCategoryName("root");
                rootCategory.setCreationDate(DateTime.now());

                Transaction tx = session.beginTransaction();
                BaseEntityService.Instance.saveOrUpdate(rootCategory, session);
                tx.commit();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            session.close();
        }
    }

    public boolean checkAppCategoryVO(CategoryVO categoryVO) {
        if (categoryVO.getCategoryName() == null || categoryVO.getCategoryName().trim().equals("") || categoryVO.getAssignable() == null ||
                categoryVO.getEnabled() == null || categoryVO.getParentId() == null || categoryVO.getIconPath() == null ||
                categoryVO.getIconPath().trim().equals("")) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        Instance.insertRootCategory();
        System.exit(0);
    }
}
