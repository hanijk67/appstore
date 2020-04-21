package com.fanap.midhco.appstore.service.osType;

import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.vos.OSTypeVO;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.IAPPPackageService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.access.PrincipalUtil;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationFailedException;
import org.hibernate.Query;
import org.hibernate.Session;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * Created by admin123 on 6/28/2016.
 */
public class OSTypeService {
    public static OSTypeService Instance = new OSTypeService();

    private OSTypeService() {
    }

    public static class OSTypeCriteria implements Serializable {
        public Long id;
        public String name;
        public String upperCaseName;
        public Boolean disabled;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUpperCaseName() {
            return upperCaseName;
        }

        public void setUpperCaseName(String upperCaseName) {
            this.upperCaseName = upperCaseName;
        }

        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
        }
    }

    public void applyCriteria(HQLBuilder builder, OSTypeCriteria criteria) {
        if (criteria.id != null)
            builder.addClause("and ent.id = :id_", "id_", criteria.id);

        if (criteria.name != null && !criteria.name.trim().isEmpty())
            builder.addClause("and lower(ent.name) = :name_", "name_", criteria.name.toLowerCase());

        if (criteria.upperCaseName != null && !criteria.upperCaseName.trim().isEmpty())
            builder.addClause("and upper(ent.name) = :name_", "name_", criteria.upperCaseName);

        if (criteria.disabled != null) {
            builder.addClause("and ent.disabled = :disabled_ ", "disabled_", criteria.disabled);
        }
    }

    public Long count(OSTypeCriteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(ent.id) ", " from OSType ent ");
        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }

    public List<OSType> list(OSTypeCriteria criteria, int first, int count, String sortProp, boolean isAsc, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select ent ", "from OSType ent ");
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

    public List<OSType> getEnabledOSTypes() {
        Session session = HibernateUtil.getNewSession();
        try {
            String queryString = "select osType from OSType osType where osType.disabled is null or osType.disabled = :disabled_";
            Query query = session.createQuery(queryString);
            query.setParameter("disabled_", false);
            return query.list();
        } finally {
            session.close();
        }
    }

    public void saveOrUpdate(OSType osType, Session session) {
        if (osType.getId() == null) {
            osType.setCreationDate(DateTime.now());
            osType.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            osType.setLastModifyDate(DateTime.now());
            osType.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(osType);
    }

    public IUploadFilter getOSTypeUploadFilter(OSType osType) throws ScriptException {
        String osTypeCompareScript = osType.getOsCompareScript();
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");
        engine.eval(osTypeCompareScript);

        IUploadFilter iUploadFilter = (IUploadFilter) engine.eval("getUploadFilter();");
        return iUploadFilter;
    }

    public OSType getOSTypeByName(String osTypeName, Session session) {
        OSTypeCriteria criteria = new OSTypeCriteria();
        criteria.upperCaseName = osTypeName.toUpperCase();
        List<OSType> osTypes = list(criteria, 0, -1, null, true, session);
        if (!osTypes.isEmpty())
            return osTypes.get(0);
        return null;
    }

    public Comparator getVersionComparatorForOSType(OSType osType) throws Exception {

        String osTypeCompareScript = osType.getOsCompareScript();
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");
        engine.eval(osTypeCompareScript);

        Comparator versionComparator = (Comparator) engine.eval("getVersionComparator();");
        return versionComparator;
    }

    public List<OSType> listAll() {
        Session session = HibernateUtil.getCurrentSession();
        OSTypeCriteria criteria = new OSTypeCriteria();
        criteria.disabled = false; // to return enabled osTypes
        String queryString = "select osType from OSType osType";
        Query query = session.createQuery(queryString);
        return query.list();
    }


    public ResponseVO checkInputOsTypeVo(OSTypeVO osTypeVO) {

        ResponseVO responseVO = new ResponseVO();
        if (osTypeVO == null || osTypeVO.getDisabled() == null || osTypeVO.getOsName() == null || osTypeVO.getOsName().trim().equals("")
                || osTypeVO.getOsCompareScriptFileKey() == null || osTypeVO.getOsCompareScriptFileKey().trim().equals("")) {
            responseVO.setResult(ResultStatus.NULL_DATA.toString());
            responseVO.setResultStatus(ResultStatus.NULL_DATA);
        } else {
            responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        }

        return responseVO;
    }


    public ResponseVO checkScrtip(String osCompareScript) {
        ResponseVO responseVO = new ResponseVO();
        if (osCompareScript != null && !osCompareScript.trim().isEmpty()) {
            try {
                Class scriptClass = new GroovyClassLoader().parseClass(osCompareScript);
                Method parseMethod = scriptClass.getMethod("parse", String.class);
                if (!Modifier.isStatic(parseMethod.getModifiers())) {
                    responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.parseMethod.isNotStatic"));
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }

                if (!(parseMethod.getReturnType().isAssignableFrom(IAPPPackageService.class))) {
                    responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.parseMethod.returnType"));
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }

                Method getUploadFilterMethod = scriptClass.getMethod("getUploadFilter");
                if (!Modifier.isStatic(getUploadFilterMethod.getModifiers())) {
                    responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.getUploadFilterMethod.isNotStatic"));
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }

                if (!(getUploadFilterMethod.getReturnType().isAssignableFrom(IUploadFilter.class))) {
                    responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.getUploadFilterMethod.returnType"));
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }

                Method getVersionComaparatorMethod = scriptClass.getMethod("getVersionComparator");
                if (!Modifier.isStatic(getVersionComaparatorMethod.getModifiers())) {
                    responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.getVersionComaparator.isNotStatic"));
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }

                if (!(getVersionComaparatorMethod.getReturnType().isAssignableFrom(Comparator.class))) {
                    responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.getVersionComaparator.returnType"));
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }

                Method IsDeltaUpdatableMethod = scriptClass.getMethod("IsDeltaUpdatable");
                if (!Modifier.isStatic(IsDeltaUpdatableMethod.getModifiers())) {
                    responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.getIsDeltaUpdatable.isNotStatic"));
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }

                if (!(IsDeltaUpdatableMethod.getReturnType().isAssignableFrom(boolean.class))) {
                    responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.getIsDeltaUpdatable.returnType"));
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }

                Method getDeltaPackageMethod = scriptClass.getMethod("getDeltaPackage", URL.class, URL.class);
                if (!Modifier.isStatic(getDeltaPackageMethod.getModifiers())) {
                    responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.getDeltaPackage.isNotStatic"));
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }

                Method checkFileExistenceInPackage = scriptClass.getMethod("checkFileExistenceInPackage", ZipFile.class, String.class);
                if (!Modifier.isStatic(checkFileExistenceInPackage.getModifiers())) {

                    //todo send error
                    //  error(editorField, "OSType.editorField.checkFileExistenceInPackage.isNotStatic");
                }


                if (!(getDeltaPackageMethod.getReturnType().isAssignableFrom(String.class))) {
                    responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.getDeltaPackage.returnType"));
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }
                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                return responseVO;
            } catch (CompilationFailedException ex) {
                responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.compilationFailed"));
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                return responseVO;
            } catch (Exception ex) {
                if (ex instanceof NoSuchMethodException) {
                    if (ex.getMessage().contains("parse")) {
                        responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.parseMethod.not.present"));
                        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    } else if (ex.getMessage().contains("getUploadFilterMethod")) {
                        responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.getUploadFilter.not.present"));
                        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    } else if (ex.getMessage().contains("getVersionComparator")) {
                        responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.getVersionComparator.not.present"));
                        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    } else if (ex.getMessage().contains("verifyPackage")) {
                        responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.verifyPackage.not.present"));
                        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    } else if (ex.getMessage().contains("IsDeltaUpdatable")) {
                        responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.IsDeltaUpdatable.not.present"));
                        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    } else if (ex.getMessage().contains("getDeltaPackage")) {
                        responseVO.setResult(AppStorePropertyReader.getString("OSType.editorField.getDeltaPackage.not.present"));
                        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    }else {
                        responseVO.setResult(ex.getMessage());
                        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    }
                    return responseVO;
                }
            }
        }
        responseVO.setResult(ResultStatus.INVALID_DATA.toString());
        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
        return responseVO;
    }


}
