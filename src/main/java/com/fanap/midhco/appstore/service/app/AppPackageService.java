package com.fanap.midhco.appstore.service.app;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.iosUtil.IPAInfo;
import com.fanap.midhco.appstore.iosUtil.IPAReader;
import com.fanap.midhco.appstore.restControllers.vos.PackageVO;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.appstore.service.myException.appBundle.AppBundleCertificateNotValidException;
import com.fanap.midhco.appstore.service.myException.appBundle.AppBundleNotSignedException;
import com.fanap.midhco.appstore.service.myException.appBundle.AppBundleSignNotValidException;
import com.fanap.midhco.appstore.service.myException.appBundle.AppStoreNoSimilarCertificateFoundException;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import groovy.lang.GroovyClassLoader;
import io.searchbox.client.JestResult;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.util.Base64;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

/**
 * Created by admin123 on 7/1/2016.
 */
public class AppPackageService {
    static final Logger logger = Logger.getLogger(AppPackageService.class);
    public static final AppPackageService Instance = new AppPackageService();


    public static class Criteria implements Serializable {
        public Long id;
        public String appPackageName;
        public String versionName;
        public String versionCode;
        public PublishState publishState;
        public OSType osType;
        public Boolean isDeleted;
    }

    public static void applyCriteria(HQLBuilder builder, Criteria criteria) {
        if (criteria.id != null)
            builder.addClause("and appPackage.id = :id_", "id_", criteria.id);

        if (criteria.appPackageName != null && !criteria.appPackageName.trim().isEmpty())
            builder.addClause("and app.packageName  = :packageName_", "packageName_", criteria.appPackageName);

        if (criteria.versionName != null && !criteria.versionName.trim().isEmpty())
            builder.addClause("and appPackage.versionName = :versionName_", "versionName_", criteria.versionName);

        if (criteria.versionCode != null && !criteria.versionCode.trim().isEmpty())
            builder.addClause("and appPackage.versionCode = :versionCode_", "versionCode_", criteria.versionCode);

        if (criteria.publishState != null)
            builder.addClause("and appPackage.publishState = :publishState_", "publishState_", criteria.publishState);

        if (criteria.osType != null)
            builder.addClause("and app.osType = :osType_", "osType_", criteria.osType);

        if (criteria.isDeleted != null && criteria.isDeleted) {
            builder.addClause("and app.isDeleted = :isDeletedApp_", "isDeletedApp_", criteria.isDeleted);
            builder.addClause("and appPackage.isDeleted = :isDeletedPack_", "isDeletedPack_", criteria.isDeleted);

        } else {
            builder.addClause(" and (app.isDeleted is null or app.isDeleted = :isDeletedApp_ )", "isDeletedApp_", false);
            builder.addClause(" and (appPackage.isDeleted is null or appPackage.isDeleted = :isDeletedPack_ )", "isDeletedPack_", false);

        }

    }

    public long count(Criteria criteria, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select count(appPackage.id) ", " from AppPackage appPackage, App app ");
        builder.addClause("and appPackage member of app.appPackages");

        applyCurrentDeveloperCriteria(builder);

        if (criteria != null)
            applyCriteria(builder, criteria);

        Query query = builder.createQuery();

        return (Long) query.uniqueResult();
    }

    public static class AppPackageSearchResult implements Serializable {
        AppPackage appPackage;
        App app;
        Boolean isMain;

        public AppPackage getAppPackage() {
            return appPackage;
        }

        public void setAppPackage(AppPackage appPackage) {
            this.appPackage = appPackage;
        }

        public App getApp() {
            return app;
        }

        public void setApp(App app) {
            this.app = app;
        }

        public Boolean getIsMain() {
            return isMain;
        }

        public void setIsMain(Boolean isMain) {
            this.isMain = isMain;
        }
    }

    private static void applyCurrentDeveloperCriteria(HQLBuilder builder) {
        if (PrincipalUtil.isCurrentUserDeveloper()) {
            builder.addClause("and app.developer = :developer_", "developer_", PrincipalUtil.getCurrentUser());
        }
    }

    public List<AppPackageSearchResult> list(Criteria criteria, int first, int count, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select appPackage, app ", " from AppPackage appPackage, App app ");
        builder.addClause("and appPackage member of app.appPackages");

        applyCurrentDeveloperCriteria(builder);

        if (criteria != null)
            applyCriteria(builder, criteria);

        builder.addOrder("appPackage.versionName", true);
        Query query = builder.createQuery();

        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);

        List<Object[]> objectResultList = query.list();
        List<AppPackageSearchResult> searchResultList = new ArrayList<>();

        for (Object[] tempObject : objectResultList) {
            AppPackageSearchResult appPackageSearchResult = new AppPackageSearchResult();
            appPackageSearchResult.appPackage = (AppPackage) tempObject[0];
            appPackageSearchResult.app = (App) tempObject[1];
            if (appPackageSearchResult.app.getMainPackage() != null &&
                    appPackageSearchResult.app.getMainPackage().equals(appPackageSearchResult.appPackage))
                appPackageSearchResult.setIsMain(true);
            else
                appPackageSearchResult.setIsMain(false);
            searchResultList.add(appPackageSearchResult);
        }

        return searchResultList;
    }

    public List<AppPackage> listAll(Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select appPackage ", " from AppPackage appPackage");

        Query query = builder.createQuery();

        return query.list();
    }

    public App publishAppPackage(App app, AppPackage appPackage, Session session) throws Exception {
        try {
            Boolean hasScheduler = null;
            if (app != null) {
                hasScheduler = app.getHasScheduler();
            }
            app = (App) session.load(App.class, app.getId());
            AppPackage mainPackage = app.getMainPackage();
//            AppPackageHistory appPackageHistory = AppPackageHistoryService.Instance.setAppPackageHistoryByAppPackage(appPackage, session);
//            if (appPackageHistory!=null) {
//                AppPackageHistoryService.Instance.saveOrUpdate(appPackageHistory , session);
//            }
            if (mainPackage.equals(appPackage)) {

                mainPackage.setPublishState(PublishState.PUBLISHED);
                mainPackage.setLastPublishDate(DateTime.now());
                AppPackageService.Instance.saveOrUpdate(mainPackage, session);
            } else {
                OSType osType = app.getOsType();
                String compareScript = osType.getOsCompareScript();

                ScriptEngineManager factory = new ScriptEngineManager();
                ScriptEngine engine = factory.getEngineByName("groovy");
                engine.eval(compareScript);

                Comparator versionComparator = (Comparator) engine.eval("getVersionComparator();");

                int compareResult = versionComparator.compare(mainPackage.getVersionCode(), appPackage.getVersionCode());
//                if (compareResult <= 0) {
//                    throw new APPPackageException(AppStorePropertyReader.getString("App.mainPackage.versionCode.Have.bigger.versionCode"));
//                } else {
                if (mainPackage.getPublishState().equals(PublishState.PUBLISHED)) {
                    mainPackage.setPublishState(PublishState.UNPUBLISHED);
                    AppPackageService.Instance.saveOrUpdate(mainPackage, session);
                }

                app.setMainPackage(appPackage);
                app.setMainPackageModificationDate(DateTime.now());
                app.setHasScheduler(hasScheduler);
//                AppService.Instance.saveOrUpdate(app, session);
                AppPackage loadedAppPackage = (AppPackage) session.load(AppPackage.class, appPackage.getId());
                loadedAppPackage.setPublishState(appPackage.getPublishState());
                mainPackage.setLastPublishDate(DateTime.now());

                AppPackageService.Instance.saveOrUpdate(loadedAppPackage, session);
//                }

            }
            return app;
        } catch (Exception ex) {
            if (ex instanceof APPPackageException)
                throw ex;
            else
                throw new AppStoreRuntimeException(ex);
        }
    }

    public static class APPPackageException extends Exception {
        public APPPackageException(String message) {
            super(message);
        }
    }

    public static class APPPackageGeneralException extends APPPackageException {
        public APPPackageGeneralException() {
            super(EnumCaptionHelper.APP_PACK_EXCEPTION.get(APPPackageGeneralException.class));
        }
    }

    public static class PackageFileNotValidException extends APPPackageException {
        public PackageFileNotValidException() {
            super(EnumCaptionHelper.APP_PACK_EXCEPTION.get(PackageFileNotValidException.class));
        }
    }

    public static class PackageFileNotExistsException extends APPPackageException {
        public PackageFileNotExistsException() {
            super(EnumCaptionHelper.APP_PACK_EXCEPTION.get(PackageFileNotExistsException.class));
        }
    }

    public static class OSValidationScriptNotValid extends APPPackageException {
        public OSValidationScriptNotValid() {
            super(EnumCaptionHelper.APP_PACK_EXCEPTION.get(OSValidationScriptNotValid.class));
        }
    }

    public static class VersionInfo implements Serializable {
        String versionCode;
        String versionName;

        public String getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(String versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }
    }

    private AppPackageService() {
    }

    public static class AppPackageModel implements Serializable {
        public UploadedFileInfo packFileInfo;
        public UploadedFileInfo iconFileInfo;
        public Set<UploadedFileInfo> thumbFilesInfo;
        public boolean usePreviousFile;
        public Certificate certificate;


        public UploadedFileInfo getPackFileInfo() {
            return packFileInfo;
        }

        public void setPackFileInfo(UploadedFileInfo packFileInfo) {
            this.packFileInfo = packFileInfo;
        }

        public UploadedFileInfo getIconFileInfo() {
            return iconFileInfo;
        }

        public void setIconFileInfo(UploadedFileInfo iconFileInfo) {
            this.iconFileInfo = iconFileInfo;
        }

        public Set<UploadedFileInfo> getThumbFilesInfo() {
            return thumbFilesInfo;
        }

        public void setThumbFilesInfo(Set<UploadedFileInfo> thumbFilesInfo) {
            this.thumbFilesInfo = thumbFilesInfo;
        }

        public Certificate getCertificate() {
            return certificate;
        }

        public boolean getUsePreviousFile() {
            return usePreviousFile;
        }

        public void setUsePreviousFile(boolean usePreviousFile) {
            this.usePreviousFile = usePreviousFile;
        }

        public void setCertificate(Certificate certificate) {
            this.certificate = certificate;
        }

    }

    public void saveOrUpdate(AppPackage appPackage, Session session) throws Exception {

        if (appPackage.getId() == null) {
            appPackage.setCreationDate(DateTime.now());
            appPackage.setCreatorUser(PrincipalUtil.getCurrentUser());
            session.saveOrUpdate(appPackage);

        } else {
            AppPackage appPackageForSaveOrUpdate = AppPackageHistoryService.Instance.setAppPackageHistoryForAppPackage(session, appPackage);
            appPackageForSaveOrUpdate.setLastModifyDate(DateTime.now());
            appPackageForSaveOrUpdate.setLastModifyUser(PrincipalUtil.getCurrentUser());
            session.evict(appPackage);
            session.saveOrUpdate(appPackageForSaveOrUpdate);
        }

    }

    public IUploadFilter getUploadFilterForOSType(OSType osType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String osCompareScript = osType.getOsCompareScript();
        Class scriptClass = new GroovyClassLoader().parseClass(osCompareScript);
        IUploadFilter uploadFilter =
                (IUploadFilter) scriptClass.getMethod("getUploadFilter").invoke(scriptClass);
        return uploadFilter;
    }

    public IAPPPackageService processPackageFile(String fileKey, OSType osType) throws APPPackageException {
        FileInputStream fin;
        try {
            String fileDownURL = FileServerService.FILE_DOWNLOAD_SERVER_PATH;
            fileDownURL.replace("${key}", fileKey);
            String tempFileLocation = FileServerService.Instance.copyFileFromServerToTemp(fileKey);
            File file = new File(tempFileLocation);
            if (!file.exists()) {
                throw new PackageFileNotExistsException();
            }
            String validationScript = osType.getOsCompareScript();
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("groovy");
            Class scriptClass = new GroovyClassLoader().parseClass(validationScript);
            IAPPPackageService packageService = (IAPPPackageService) scriptClass.getMethod("parse", String.class).invoke(scriptClass, tempFileLocation);
            return packageService;

        } catch (Exception ex) {
            logger.error("processPackageFile exception ", ex);
            if (ex instanceof APPPackageException) {
                throw (APPPackageException) ex;
            } else if (ex instanceof MultipleCompilationErrorsException) {
                throw new OSValidationScriptNotValid();
            } else if (ex instanceof ScriptException) {
                throw new PackageFileNotValidException();
            } else {
                throw new APPPackageGeneralException();
            }
        }
    }

    public AppPackage getMainPackage(String packageName, Session session) {
        String queryString = "select appPack from App app inner join app.mainPackage appPack where app.packageName = :packageName_";
        Query query = session.createQuery(queryString);
        query.setParameter("packageName_", packageName);
        List<AppPackage> appPackageList = query.list();
        if (!appPackageList.isEmpty()) {
            return appPackageList.get(0);
        }
        return null;
    }

    public AppPackage getPackage(String packageName, String versionCode, Session session) {
        String queryString = "select appPackage from App app , AppPackage appPackage where appPackage in elements(app.appPackages) and " +
                " app.packageName = :packageName_ and appPackage.versionCode = :versionCode_";
        Query query = session.createQuery(queryString);
        query.setParameter("packageName_", packageName);
        query.setParameter("versionCode_", versionCode);
        List<AppPackage> appPackageList = query.list();
        if (!appPackageList.isEmpty()) {
            return appPackageList.get(0);
        }
        return null;
    }

    public AppPackage getPackage(OSType osType, String packageName, String versionCode, Session session) {
        String queryString = "select appPackage from App app , AppPackage appPackage where appPackage in elements(app.appPackages) and " +
                " app.packageName = :packageName_ and appPackage.versionCode = :versionCode_ and app.osType = :osType_";
        Query query = session.createQuery(queryString);
        query.setParameter("packageName_", packageName);
        query.setParameter("versionCode_", versionCode);
        query.setParameter("osType_", osType);
        List<AppPackage> appPackageList = query.list();
        if (!appPackageList.isEmpty()) {
            return appPackageList.get(0);
        }
        return null;
    }

    public List<VersionInfo> getVersionsForApp(String packageName, OSType osType, Session session) {
        HQLBuilder builder = new HQLBuilder(session, "select appPackage.versionCode, appPackage.versionName  ", " from AppPackage appPackage, App app ");
        builder.addClause("and appPackage member of app.appPackages");

        builder.addClause("and app.osType = :osType_ ", "osType_", osType);

        builder.addClause("and app.packageName = :packageName_", "packageName_", packageName);

        builder.addClause(" and (app.isDeleted is null or app.isDeleted = :isDeleted_)", "isDeleted_", false);


        Query query = builder.createQuery();
        List<Object[]> objectList = query.list();

        return objectList.stream().map((Object[] resultObject) -> {
            VersionInfo versionInfo = new VersionInfo();
            versionInfo.setVersionCode((String) resultObject[0]);
            versionInfo.setVersionName((String) resultObject[1]);
            return versionInfo;
        }).collect(Collectors.toList());
    }

    public boolean hasAppWithVersionCode(String packageName, String versionCode, OSType osType, Session session) {

        HQLBuilder builder = new HQLBuilder(session, "select count(app.id)  ", " from App app inner join app.appPackages appPackages");
        builder.addClause("and appPackages.versionCode = :versionCode_ ", "versionCode_", versionCode);

        builder.addClause("and app.osType = :osType_ ", "osType_", osType);

        builder.addClause("and app.packageName=:packageName_", "packageName_", packageName);

        builder.addClause(" and (app.isDeleted is null or app.isDeleted = :isDeleted_)", "isDeleted_", false);

        Query query = builder.createQuery();


        return !query.uniqueResult().equals(0l);
    }

    public static class NewPackageInfo implements Serializable {
        public Certificate certificate;

        public Certificate getCertificate() {
            return certificate;
        }

        public void setCertificate(Certificate certificate) {
            this.certificate = certificate;
        }
    }

    public NewPackageInfo validateNewAppPackage(App app, IAPPPackageService iappPackageService, boolean forReplacement) throws APPPackageException {
        Certificate mainPackageCertificate = null;

        if (app.getId() != null) {
            if (app.getPackageName() == null || app.getPackageName().trim().equals("")) {
                throw new APPPackageException(AppStorePropertyReader.getString("App.has.no.packageName"));
            } else if (iappPackageService.getPackage() == null || iappPackageService.getPackage().trim().equals("")) {
                throw new APPPackageException(AppStorePropertyReader.getString("App.newApp.has.no.packageName"));
            } else if (!app.getPackageName().equals(iappPackageService.getPackage())) {
                throw new APPPackageException(AppStorePropertyReader.getString("App.newApp.has.different.appPackageName.with.original.package"));
            }

            Session session = HibernateUtil.getNewSession();
            try {
                AppPackage mainPackage = app.getMainPackage();

                mainPackageCertificate = getAppPackageCertificate(mainPackage);
                if (mainPackageCertificate == null)
                    throw new APPPackageException(AppStorePropertyReader.getString("App.previousApp.has.no.certificate"));

                if (!forReplacement) {
                    if (hasAppWithVersionCode(app.getPackageName(), iappPackageService.getVersionCode(), app.getOsType(), session)) {
                        throw new APPPackageException(AppStorePropertyReader.getString("App.has.package.with.same.versionCode.as.new.package"));
                    }
                }
            } catch (Exception ex) {
                if (ex instanceof APPPackageException)
                    throw (APPPackageException) ex;
                if (ex instanceof CertificateException)
                    throw new APPPackageException(AppStorePropertyReader.getString("App.certificate.not.valid"));
                throw new AppStoreRuntimeException(ex);
            } finally {
                session.close();
            }
        }

        Certificate newPackageCertificate = null;
        try {
            newPackageCertificate = iappPackageService.verifyPackage(mainPackageCertificate);
        } catch (Exception ex) {
            String message = ex.getMessage();
            logger.debug("apppackage validation exception " + ex.getMessage());
            if (ex instanceof AppBundleCertificateNotValidException || (message.contains("AppBundleCertificateNotValidException"))) {
                throw new APPPackageException(AppStorePropertyReader.getString("AppBundleCertificateNotValidException"));
            } else if (ex instanceof AppBundleNotSignedException || message.contains("AppBundleNotSignedException")) {
                throw new APPPackageException(AppStorePropertyReader.getString("AppBundleNotSignedException"));
            } else if (ex instanceof AppBundleSignNotValidException || message.contains("AppBundleSignNotValidException")) {
                throw new APPPackageException(AppStorePropertyReader.getString("AppBundleSignNotValidException"));
            } else if (ex instanceof AppStoreNoSimilarCertificateFoundException || message.contains("AppStoreNoSimilarCertificateFoundException")) {
                throw new APPPackageException(AppStorePropertyReader.getString("AppStoreNoSimilarCertificateFoundException"));
            }
        }

        NewPackageInfo packageInfo = new NewPackageInfo();
        packageInfo.setCertificate(newPackageCertificate);
        return packageInfo;
    }

    public static Certificate getAppPackageCertificate(AppPackage appPackage) throws CertificateException {
        String certInfo = appPackage.getCertificateInfo();
        if (certInfo != null) {
            byte[] crtBytes = Base64.decode(certInfo.getBytes());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bin = new ByteArrayInputStream(crtBytes);
            Collection<? extends Certificate> certificates = cf.generateCertificates(bin);
            Iterator<Certificate> iterator = (Iterator<Certificate>) certificates.iterator();
            if (iterator.hasNext()) {
                Certificate certificate = iterator.next();
                return certificate;
            }
        }
        return null;
    }

    public String getDeltaPackageDownloadURL(OSType osType, String packageName, String prevVersionCode, String forwardVersionCode, Session session) throws Exception {
        AppPackageService.Criteria appSearchCriteria = new Criteria();
        appSearchCriteria.osType = osType;
        appSearchCriteria.appPackageName = packageName;
        appSearchCriteria.versionCode = prevVersionCode;

        try {
            List<AppPackageSearchResult> prevSearchResultModelList =
                    AppPackageService.Instance.list(appSearchCriteria, 0, -1, session);

            if (prevSearchResultModelList == null || prevSearchResultModelList.isEmpty()) {
                throw new Exception("packageNotFound");
            }

            appSearchCriteria.versionCode = forwardVersionCode;
            List<AppPackageSearchResult> forwardSearchResultModelList =
                    AppPackageService.Instance.list(appSearchCriteria, 0, -1, session);
            if (forwardSearchResultModelList == null || forwardSearchResultModelList.isEmpty()) {
                throw new Exception("packageNotFound");
            }

            String prevDownloadPath =
                    FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", prevSearchResultModelList.get(0).getAppPackage().getPackFile().getFilePath());
            URL url1 = new URL(prevDownloadPath);

            String forwardDownloadPath =
                    FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", forwardSearchResultModelList.get(0).getAppPackage().getPackFile().getFilePath());
            URL url2 = new URL(forwardDownloadPath);

            String validationScript = osType.getOsCompareScript();
            Class scriptClass = new GroovyClassLoader().parseClass(validationScript);

            boolean isDeltaUpdatable = (boolean) scriptClass.getMethod("IsDeltaUpdatable").invoke(scriptClass);
            if (!isDeltaUpdatable) {
                String message = AppStorePropertyReader.getString("deltaPackage.osType.Not.DeltaUpdatable");
                message = message.replace("${osType}", osType.getName());
                throw new Exception(message);
            }

            Comparator comparator = (Comparator) scriptClass.getMethod("getVersionComparator").invoke(scriptClass);
            int compareResult = comparator.compare(prevVersionCode, forwardVersionCode);
            if (compareResult == -1 || compareResult == 0) {
                throw new Exception(AppStorePropertyReader.getString("deltaPackage.prevVersion.biggerThan.forwardVersion"));
            }

            String deltaPackageKey = (String) scriptClass.getMethod("getDeltaPackage", URL.class, URL.class).invoke(scriptClass, url1, url2);

            String downloadPath = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH);
            downloadPath = downloadPath.replace("${key}", deltaPackageKey);

            return downloadPath;

        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("packageNotFound")) {
                String message = AppStorePropertyReader.getString("deltaPackage.package.not.found");
                message = message.replace("${packageName}", packageName);
                message = message.replace("${osType}", osType.getName());
                message = message.replace("${versionCode}", appSearchCriteria.versionCode);
                throw new Exception(message);
            } else
                throw ex;
        }
    }

    public ResponseVO checkFileExistenceInPackage(OSType osType, String packageName, String versionCode, String fileName) throws Exception {
        Session session = null;
        Boolean existedFile = null;
        ResponseVO responseVO = new ResponseVO();
        try {
            String osCompareScript = osType.getOsCompareScript();
            Class scriptClass = new GroovyClassLoader().parseClass(osCompareScript);
            session = HibernateUtil.getCurrentSession();
            AppPackageService.Criteria appSearchCriteria = new Criteria();
            appSearchCriteria.osType = osType;
            appSearchCriteria.appPackageName = packageName;
            appSearchCriteria.versionCode = versionCode;

            List<AppPackageSearchResult> prevSearchResultModelList =
                    AppPackageService.Instance.list(appSearchCriteria, 0, -1, session);

            if (prevSearchResultModelList == null || prevSearchResultModelList.isEmpty()) {
                responseVO.setResultStatus(ResultStatus.PACKAGE_NOT_FOUND);
                responseVO.setResult(ResultStatus.PACKAGE_NOT_FOUND.toString());
                return responseVO;
            }
            String fileKey = prevSearchResultModelList.get(0).getAppPackage().getPackFile().getFilePath();

            String tempLocation = System.getProperty("java.io.tmpdir");
            String tempFileName = tempLocation + "/" + fileKey;
            FileOutputStream out = new FileOutputStream(tempFileName);
            FileServerService.Instance.downloadFileFromServer(fileKey, out);

            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(tempFileName));
            existedFile = (Boolean) scriptClass.getMethod("checkFileExistenceInPackage", ZipInputStream.class, String.class, Boolean.class).invoke(scriptClass, zipIn, fileName);
            responseVO.setResult(existedFile ? "true" : "false");
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            return responseVO;
        } catch (NoSuchMethodException ex) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(AppStorePropertyReader.getString("error.osType.does.not.support.all.scripts"));
            return responseVO;
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public void publishUnPublishPackage(Session session, AppPackageService.AppPackageSearchResult searchResult, AppPackage appPackage, PublishState publishState, Boolean publishPackage) throws Exception {
        if (publishPackage) {
            if (publishState.equals(PublishState.UNPUBLISHED)) {
                App app = AppPackageService.Instance.publishAppPackage(searchResult.getApp(), appPackage, session);
                if (!app.getMainPackage().equals(appPackage)) {
                    AppService.Instance.saveOrUpdate(app, session);
                }

            }
        } else {
            if (publishState.equals(PublishState.PUBLISHED)) {
                appPackage.setPublishState(PublishState.UNPUBLISHED);
                AppPackageService.Instance.saveOrUpdate(appPackage, session);
            }
        }
    }

    public void publishUnPublishPackage(Session session, App app, AppPackage appPackage, PublishState publishState, Boolean publishPackage) throws Exception {
        if (publishPackage) {
            if (publishState.equals(PublishState.UNPUBLISHED)) {
                if (!app.getMainPackage().equals(appPackage)) {
                    AppService.Instance.saveOrUpdate(app, session);
                }

            }
        } else {
            if (publishState.equals(PublishState.PUBLISHED)) {
                appPackage.setPublishState(PublishState.UNPUBLISHED);
                AppPackageService.Instance.saveOrUpdate(appPackage, session);
            }
        }
    }

    public ResponseVO checkPackageInfo(PackageVO packageVO, boolean isMainPackage) {
        ResponseVO responseVO = new ResponseVO();
        try {
            if (packageVO != null && packageVO.getAppPackageName() != null && !packageVO.getAppPackageName().trim().equals("")) {
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());

                responseVO.setResultStatus(ResultStatus.INVALID_DATA);

                if (!isMainPackage) {
                    if (packageVO.getVersionCode() == null || packageVO.getVersionCode().trim().equals("") ||
                            packageVO.getVersionName() == null || packageVO.getVersionName().trim().equals("")) {
                        return responseVO;
                    }
                }
                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            } else {
                responseVO.setResult(ResultStatus.NULL_DATA.toString());
                responseVO.setResultStatus(ResultStatus.NULL_DATA);
            }
        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        }

        return responseVO;
    }

    public ResponseVO checkPackageInfoForPublish(PackageVO packageVO, boolean isMainPackage) {
        ResponseVO responseVO = checkPackageInfo(packageVO, isMainPackage);
        try {
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            } else {
                if (packageVO.getPublishStateStr() == null || packageVO.getPublishStateStr().trim().equals("")) {
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());

                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }
                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            }
        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        }

        return responseVO;
    }

    public ResponseVO checkPackageInfoForChangeIcon(PackageVO packageVO, boolean isMainPackage) {
        ResponseVO responseVO = checkPackageInfo(packageVO, isMainPackage);
        try {
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            } else {
                if (packageVO.getIconFileKey() == null || packageVO.getIconFileKey().trim().equals("")) {
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }
                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            }
        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        }

        return responseVO;
    }

    public ResponseVO checkPackageInfoForChangeImages(PackageVO packageVO, boolean isMainPackage) {
        ResponseVO responseVO = checkPackageInfo(packageVO, isMainPackage);
        try {
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            } else {
                if (packageVO.getImagesFileKey() == null || packageVO.getImagesFileKey().isEmpty()) {
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }
                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            }
        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        }

        return responseVO;
    }

    public ResponseVO checkPackageInfoForEditPackage(PackageVO packageVO, boolean isMainPackage) {
        ResponseVO responseVO = checkPackageInfoForChangeImages(packageVO, isMainPackage);
        if (responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
            if (packageVO.getChangesLog() == null || packageVO.getChangesLog().trim().equals("") || packageVO.getPackageFileKey() == null || packageVO.getPackageFileKey().trim().equals("") ||
                    packageVO.getIconFileKey() == null || packageVO.getIconFileKey().trim().equals("") || packageVO.getChangesLog() == null || packageVO.getChangesLog().trim().equals("") ) {
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
            }
        }

        return responseVO;
    }

    public void replacePackage(AppPackage appPackage, String packageFileKey, String appPackageFileName, String iconFileKey, String iconFileName, Map<String, String> thumbFileInfoMap, String changesLog, Session session) throws Exception {

        if (packageFileKey != null && !packageFileKey.trim().equals("")) {
            com.fanap.midhco.appstore.entities.File packFile = new com.fanap.midhco.appstore.entities.File();
            packFile.setFilePath(packageFileKey);
            packFile.setFileName(appPackageFileName);
            packFile.setStereoType(StereoType.MAIN_APP_PACK_FILE);
            BaseEntityService.Instance.saveOrUpdate(packFile, session);
            if (packageFileKey != null && !FileServerService.Instance.doesFileExistOnFileServer(packageFileKey)) {
                FileServerService.Instance.persistFileToServer(packageFileKey);
            }
            Long fileSize = FileServerService.Instance.getFileSizeByFileKey(packageFileKey);
            packFile.setFileSize(fileSize);
            appPackage.setPackFile(packFile);
        }
        if (iconFileKey != null && !iconFileKey.trim().equals("")) {
            com.fanap.midhco.appstore.entities.File iconFile = new com.fanap.midhco.appstore.entities.File();
            iconFile.setStereoType(StereoType.ICON_FILE);
            iconFile.setFileName(iconFileName);
            iconFile.setFilePath(iconFileKey);
            appPackage.setIconFile(iconFile);
            BaseEntityService.Instance.saveOrUpdate(iconFile, session);
            if (!FileServerService.Instance.doesFileExistOnFileServer(iconFileKey))
                FileServerService.Instance.persistFileToServer(iconFileKey);
        }

        if (thumbFileInfoMap != null && !thumbFileInfoMap.isEmpty()) {
            List<String> inputThumbFiles = new ArrayList(thumbFileInfoMap.keySet());
            List<com.fanap.midhco.appstore.entities.File> thumbFiles = AppPackageService.Instance.setImagesToPackage(inputThumbFiles, session);
            appPackage.setThumbImages(thumbFiles);
        }
        if(changesLog!=null && !changesLog.trim().equals("")){
            appPackage.setChangeLog(changesLog);
        }

        try {
        AppPackageService.Instance.saveOrUpdate(appPackage, session);
        } catch (Exception e) {
           throw  e;
        }

    }


    public static void main(String[] args) throws Exception {//} throws APPPackageException, IOException {
        Session session = HibernateUtil.getCurrentSession();

//        String ipaPath = "E:\\projects\\Mobile\\app\\MBankNew.ipa";
        String ipaPath = "E:\\projects\\Mobile\\app\\mbank.ipa";

        IAPPPackageService iappPackageService = IPAReader.parsIos(ipaPath);

//        Certificate certificate = iappPackageService.verifyPackage(iappPackageService.get.getCertificate());

        String basePath = "E:\\projects\\Mobile\\app\\extracted\\MBank.app";

        Certificate mainPackageCertificate = null;
        Map<String, String> hashedDataMap = new HashMap<>();

//        mainPackageCertificate =IPAReader.getCertificateAndFileKeys(hashedDataMap,ipaPath);


        if (mainPackageCertificate == null)
            throw new AppPackageService.APPPackageException(AppStorePropertyReader.getString("App.previousApp.has.no.certificate"));

        logger.debug("Certificate and hashKeys has benn fetched");

        IPAInfo ipaInfo = IPAReader.parse(mainPackageCertificate, hashedDataMap, ipaPath);
        if (ipaInfo == null || !ipaInfo.getHasCorrectHashValue()) {
            throw new APPPackageException(AppStorePropertyReader.getString("App.certificate.not.valid"));
        }

        if (session != null && session.isOpen()) {
            session.close();
        }

    }


    public static PackageVO getPreviousPackageFiles(App app, Session session) {
        PackageVO packageVO = new PackageVO();
        if (app != null && app.getMainPackage() != null) {

            AppPackage appPackage = (AppPackage) session.get(AppPackage.class,app.getMainPackage().getId());
            if (appPackage != null) {
                packageVO.setIconFile(appPackage.getIconFile());
                packageVO.setThumbFiles(appPackage.getThumbImages());
            }
        }
        return packageVO;

    }

    public List<com.fanap.midhco.appstore.entities.File> setImagesToPackage(List<String> imageFileKeys, Session session) {
        List<com.fanap.midhco.appstore.entities.File> thumbFiles = new ArrayList<>();
        for (String fileKey : imageFileKeys) {
            String fileName = FileServerService.Instance.getFileNameFromFilePath(fileKey);// thumbFileInfoMap.get(fileKey);
            com.fanap.midhco.appstore.entities.File tempFile = new com.fanap.midhco.appstore.entities.File();
            tempFile.setStereoType(StereoType.THUMB_FILE);
            tempFile.setFileName(fileName);
            tempFile.setFilePath(fileKey);
            thumbFiles.add(tempFile);
            BaseEntityService.Instance.saveOrUpdate(tempFile, session);
            if (!FileServerService.Instance.doesFileExistOnFileServer(fileKey))
                FileServerService.Instance.persistFileToServer(fileKey);
        }
        return thumbFiles;
    }


}
