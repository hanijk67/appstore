package com.fanap.midhco.appstore.service.org;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.Organization;
import com.fanap.midhco.appstore.restControllers.vos.OrganizationVO;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.List;

/**
 * Created by A.Moshiri on 2/26/2018.
 */
public class OrgService {
    public static final OrgService Instance = new OrgService();

    private OrgService() {
    }


    public static class OrganizationVo {
        Long id;
        String nickName;
        String fullName;
        String englishFullName;
        String logoPath;
        Boolean isDefault;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEnglishFullName() {
            return englishFullName;
        }

        public void setEnglishFullName(String englishFullName) {
            this.englishFullName = englishFullName;
        }

        public String getLogoPath() {
            return logoPath;
        }

        public void setLogoPath(String logoPath) {
            this.logoPath = logoPath;
        }

        public Boolean getDefault() {
            return isDefault;
        }

        public void setDefault(Boolean aDefault) {
            isDefault = aDefault;
        }
    }


    public static class OrgCriteria implements Serializable {
        Long id;
        String nickName;
        String fullName;
        String englishFullName;

        Boolean isDefault;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEnglishFullName() {
            return englishFullName;
        }

        public void setEnglishFullName(String englishFullName) {
            this.englishFullName = englishFullName;
        }

        public Boolean getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(Boolean isDefault) {
            this.isDefault = isDefault;
        }
    }

    private void addOrgCriteria(HQLBuilder builder, OrgCriteria criteria) {
        if (criteria.id != null)
            builder.addClause("and ent.id = (:id_)", "id_", criteria.id);

        if (criteria.nickName != null && !criteria.nickName.trim().equals(""))
            builder.addClause("and lower(ent.nickName)=:nickName_", "nickName_", criteria.nickName.toLowerCase());

        if (criteria.fullName != null && !criteria.fullName.trim().equals(""))
            builder.addClause("and ent.fullName like (:fullName_)", "fullName_", HQLBuilder.like(criteria.fullName));

        if (criteria.englishFullName != null && !criteria.englishFullName.trim().equals(""))
            builder.addClause("and ent.englishFullName = :englishFullName_", "englishFullName_", criteria.englishFullName);
//            builder.addClause("and ent.englishFullName like (:englishFullName_)", "englishFullName_", HQLBuilder.like(criteria.englishFullName));

        if (criteria.isDefault!=null) {
            if ( criteria.isDefault ) {
                builder.addClause("and ent.isDefault = :isDefault_", "isDefault_", criteria.isDefault);
            }else {
                builder.addClause("and (ent.isDefault is null or ent.isDefault = :isDefault_ )", "isDefault_", criteria.isDefault);
            }
        }


    }

    public Long count(OrgCriteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(ent.id) ", "from Organization ent ");

        if (criteria != null)
            addOrgCriteria(builder, criteria);

        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }


    public List<Organization> list(OrgCriteria criteria, int first, int count, String sortProp, boolean isAscending, Session session) {

        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from Organization ent ");

        if (criteria != null)
            addOrgCriteria(builder, criteria);

        if (sortProp != null)
            builder.addOrder(sortProp, isAscending);

        Query query = builder.createQuery();
        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);

        return query.list();
    }

    public Organization loadOrganizationById(Long organizationId, Session session) {

        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from Organization ent ");
        builder.addClause(" ent.id = :id_", "id_", organizationId);

        Query query = builder.createQuery();
        List<Organization> organizationList = query.list();
        if (!organizationList.isEmpty())
            return organizationList.get(0);
        return null;
    }


    public Organization getDefaultOrganization(Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from Organization ent ");
        Boolean isDefault = true;
        builder.addClause("and ent.isDefault = :isDefault_", "isDefault_", isDefault);

        Query query = builder.createQuery();
        List<Organization> organizationList = query.list();
        if (!organizationList.isEmpty())
            return organizationList.get(0);
        return null;
    }

    public List<Organization> listAll() {
        Session session = HibernateUtil.getCurrentSession();

        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from Organization ent");
        Query query = builder.createQuery();
        return query.list();
    }

    public static OrganizationVo buildOrganizationVo(Organization organization) {
        OrganizationVo organizationVo = new OrganizationVo();
        if (organization != null) {
            organizationVo.setFullName(organization.getFullName());
            organizationVo.setNickName(organization.getNickName());
            organizationVo.setEnglishFullName(organization.getEnglishFullName());
            organizationVo.setId(organization.getId());
            String path = organization.getIconFile() != null ? organization.getIconFile().getFilePath() : null;
            organizationVo.setLogoPath(ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", path));
        }
        return organizationVo;
    }


    public ResponseVO checkInputOrganizationVo(OrganizationVO organizationVO) {

        ResponseVO responseVO = new ResponseVO();
        if (organizationVO == null) {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
        } else if (organizationVO.getFullName() == null || organizationVO.getFullName().trim().equals("") || organizationVO.getNickName() == null || organizationVO.getNickName().trim().equals("") ||
                organizationVO.getEnglishFullName() == null || organizationVO.getEnglishFullName().trim().equals("")  ||
                organizationVO.getIconFilePath() == null || organizationVO.getIconFilePath().trim().equals("") ) {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
        } else {
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
        }
        return responseVO;
    }
}
