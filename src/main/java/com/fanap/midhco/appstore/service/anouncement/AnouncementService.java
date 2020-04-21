package com.fanap.midhco.appstore.service.anouncement;

import com.fanap.midhco.appstore.entities.Anouncement;
import com.fanap.midhco.appstore.entities.OSEnvironment;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.entities.Organization;
import com.fanap.midhco.appstore.entities.helperClasses.AnouncementType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.vos.AnouncementVO;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.*;

/**
 * Created by admin123 on 8/28/2017.
 */
public class AnouncementService {
    public static AnouncementService Instance = new AnouncementService();

    private AnouncementService() {
    }

    public static class AnouncmentCriteria implements Serializable {
        Long id;
        Collection<String> actionCategory;
        String anouncementText;
        Collection<AnouncementType> anouncementType;


        DateTime[] expireDateTime = new DateTime[2];
        DateTime[] startDateTime = new DateTime[2];
        Collection<OSType> osTypes;
        Collection<Organization> organizations;
        Collection<OSEnvironment> osEnvironments;

        Boolean isActive;
        Boolean isExpired;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Collection<String> getActionCategory() {
            return actionCategory;
        }

        public void setActionCategory(Collection<String> actionCategory) {
            this.actionCategory = actionCategory;
        }

        public String getAnouncementText() {
            return anouncementText;
        }

        public void setAnouncementText(String anouncementText) {
            this.anouncementText = anouncementText;
        }

        public Collection<AnouncementType> getAnouncementType() {
            return anouncementType;
        }

        public void setAnouncementType(Collection<AnouncementType> anouncementType) {
            this.anouncementType = anouncementType;
        }

        public DateTime[] getExpireDateTime() {
            return expireDateTime;
        }

        public void setExpireDateTime(DateTime[] expireDateTime) {
            this.expireDateTime = expireDateTime;
        }

        public DateTime[] getStartDateTime() {
            return startDateTime;
        }

        public void setStartDateTime(DateTime[] startDateTime) {
            this.startDateTime = startDateTime;
        }

        public Collection<OSType> getOsTypes() {
            return osTypes;
        }

        public void setOsTypes(Collection<OSType> osTypes) {
            this.osTypes = osTypes;
        }

        public Collection<Organization> getOrganizations() {
            return organizations;
        }

        public void setOrganizations(Collection<Organization> organizations) {
            this.organizations = organizations;
        }

        public Collection<OSEnvironment> getOsEnvironments() {
            return osEnvironments;
        }

        public void setOsEnvironments(Collection<OSEnvironment> osEnvironments) {
            this.osEnvironments = osEnvironments;
        }

        public Boolean getActive() {
            return isActive;
        }

        public void setActive(Boolean active) {
            isActive = active;
        }

        public Boolean getExpired() {
            return isExpired;
        }

        public void setExpired(Boolean expired) {
            isExpired = expired;
        }
    }

    public void applyCriteria(HQLBuilder builder, AnouncmentCriteria criteria) {
        Collection<String> actionCategory = new ArrayList<>();
        if(criteria.id != null)
            builder.addClause("and ent.id=:id_", "id_", criteria.id);
        List<String> actionCategoryList = new ArrayList<>();
        if (criteria.actionCategory != null && !criteria.actionCategory.isEmpty()) {
            for (Object object : criteria.actionCategory) {
                actionCategoryList.add(((Class) object).getCanonicalName());
            }
            builder.addClause("and ent.actionCategory in (:actionCategory_)", "actionCategory_", actionCategoryList);
        }

        if (criteria.anouncementType != null && !criteria.anouncementType.isEmpty())
            builder.addClause("and ent.anouncementType in (:anouncementTypes_)", "anouncementTypes_", criteria.anouncementType);

        if(criteria.anouncementText != null)
            builder.addClause("and ent.anouncementText like (:anouncementText_)", "anouncementText_", HQLBuilder.like(criteria.anouncementText));

        if (criteria.osTypes != null && !criteria.osTypes.isEmpty()) {
            builder.addClause("and ( select count(r) from ent.osTypes r where r in (:osTypes) ) >= :n ", "osTypes", criteria.osTypes);
            builder.addParam("n", 1L);
        }


        if (criteria.organizations != null && !criteria.organizations.isEmpty()) {
            builder.addClause("and ( select count(r) from ent.organizations r where r in (:organizations) ) >= :n ", "organizations", criteria.organizations);
            builder.addParam("n", 1L);
        }


        if (criteria.osEnvironments != null && !criteria.osEnvironments.isEmpty()) {
            builder.addClause("and ( select count(r) from ent.osEnvironments r where r in (:osEnvironments) ) >= :n ", "osEnvironments", criteria.osEnvironments);
            builder.addParam("n", 1L);
        }


        if (criteria.startDateTime != null && criteria.startDateTime.length > 1) {
            if (criteria.startDateTime[0] != null && criteria.startDateTime[1] != null) {
            builder.addDateTimeRange("ent", "startDateTime", "lStartDateTime", "uStartDateTime", criteria.startDateTime);
        }
        }
        if (criteria.expireDateTime != null && criteria.expireDateTime.length > 1) {
            if (criteria.expireDateTime[0] != null && criteria.expireDateTime[1] != null) {
            builder.addDateTimeRange("ent", "expireDateTime", "lExpireDateTime", "uExpireDateTime", criteria.expireDateTime);
        }
        }
        if(criteria.isActive!=null) {
            builder.addClause("and ent.isActive = :isActive_", "isActive_", criteria.isActive);
        }



    }

    public Long count(AnouncmentCriteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(ent.id) ", " from Anouncement ent ");

        if(criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();
        Long result = (Long) query.uniqueResult();
        return (Long) query.uniqueResult();
    }

    public List<Anouncement> list(AnouncmentCriteria criteria, int first, int count, String[] sortProps, boolean isAsc, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent ", " from Anouncement ent ");

        if(criteria != null)
            applyCriteria(builder, criteria);

        if(sortProps != null)
            for(String sortString : sortProps) {
                if (sortString!=null && !sortString.trim().equals("")) {
                    builder.addOrder(sortString, isAsc);
                }
            }

        Query query = builder.createQuery();
        query.setFirstResult(first);
        if(count != -1)
            query.setMaxResults(count);

        List<Anouncement> resultObjects = query.list();

        return resultObjects;
    }

    public List<Anouncement> getLatestAnouncements( AnouncmentCriteria anouncmentCriteria ,int latestAnouncements, Session session) {
        anouncmentCriteria.expireDateTime[0] = DateTime.now();
        anouncmentCriteria.expireDateTime[1] = DateTime.MAX_DATE_TIME;


        anouncmentCriteria.startDateTime[0] = DateTime.MIN_DATE_TIME;
        anouncmentCriteria.startDateTime[1] = DateTime.MAX_DATE_TIME;
        String sortProperties[] = new String[2];
        sortProperties[0] = "ent.startDateTime.dayDate";
        sortProperties[1] = "ent.startDateTime.dayTime";

        List<Anouncement> anouncementList = list(anouncmentCriteria, 0, latestAnouncements, sortProperties, false, session);

        return anouncementList;
    }

    public Anouncement load(Long anouncementId, Session session) {
        try {
            return (Anouncement)session.load(Anouncement.class, anouncementId);
        } catch (HibernateException ex) {
            return null;
        }
    }

    public ITaskResult launchAnouncement(Anouncement anouncement, Map<String, String> parametersMap) throws Exception {
        String categoryActionClassName = anouncement.getActionCategory();
        Class anouncementActionDescriptorClass = Class.forName(categoryActionClassName);
        IAnouncementActionDescriptor iAnouncementActionDescriptor = (IAnouncementActionDescriptor)anouncementActionDescriptorClass.newInstance();

        String actionDescriptor = anouncement.getActionDescriptor();
        ITaskResult taskResult = iAnouncementActionDescriptor.doAction(actionDescriptor, parametersMap);

        return taskResult;
    }


    public ResponseVO checkAnouncement(Anouncement intendedAnouncement) {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

        if(intendedAnouncement.getExpireDateTime()!=null && intendedAnouncement.getExpireDateTime().compareTo(DateTime.now()) < 0){
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(AppStorePropertyReader.getString("error.anouncement.is.expired"));
            return responseVO;
        }


        if (intendedAnouncement.getActive() == null || !intendedAnouncement.getActive()) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(AppStorePropertyReader.getString("error.anouncement.is.disabled"));
            return responseVO;
        }
        return  responseVO;

    }

    public void saveOrUpdate(Anouncement anouncement, Session session) {
        if (anouncement.getId() == null) {
            anouncement.setCreationDate(DateTime.now());
            anouncement.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            anouncement.setLastModifyDate(DateTime.now());
            anouncement.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(anouncement);
    }


    public ResponseVO checkInputAnnouncementVO(AnouncementVO announcementVO, Session session) {
        ResponseVO responseVO = new ResponseVO();

        if (announcementVO != null) {
            if (announcementVO.getId() == null) {
                if (announcementVO.getStartDateTime() == null || announcementVO.getAnouncementType() == null || announcementVO.getExpireDateTime() == null || announcementVO.getActive() == null ||
                        announcementVO.getExpired() == null || announcementVO.getAnouncementImageFileKey() == null || announcementVO.getOsEnvironments() == null ||
                        announcementVO.getAnouncementImageFileKey().trim().equals("") || announcementVO.getOrganizations() == null || announcementVO.getOsTypes() == null ||
                        announcementVO.getAnouncementText() == null || announcementVO.getAnouncementText().trim().equals("")
                        ) {
                    responseVO.setResult(ResultStatus.NULL_DATA.toString());
                    responseVO.setResultStatus(ResultStatus.NULL_DATA);
                    return responseVO;
                }
            }
            if (announcementVO.getAnouncementType() != null && announcementVO.getAnouncementType().equals(AnouncementType.PRODUCTLISTTYPE)) {
                if (announcementVO.getActionCategory() == null || announcementVO.getActionCategory().trim().equals("") || announcementVO.getActionDescriptor() == null ||
                        announcementVO.getActionDescriptor().trim().equals("")) {
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }
            } else if (announcementVO.getAnouncementType() != null && announcementVO.getAnouncementType().equals(AnouncementType.VOID)) {
                if (announcementVO.getActionCategory() != null || announcementVO.getActionDescriptor() != null) {
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }
            }

            responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

            return responseVO;
        } else {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
            return responseVO;
        }

    }

    public List<AnouncementType> allAnnouncementTypeList() {
        List<AnouncementType> announcementTypes = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            AnouncementType announcementType = new AnouncementType(i);
            announcementTypes.add(announcementType);
        }
        return announcementTypes;
    }
}
