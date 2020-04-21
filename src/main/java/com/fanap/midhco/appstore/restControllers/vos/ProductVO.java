package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppElasticService;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.app.IAPPPackageService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by admin123 on 7/4/2016.
 */
public class ProductVO {
    private Long id;
    private String title;
    private String shortDescription;
    private String bulk;
    private String developer;
    private String icon;
    private String iconThumbNail;
    private String versionName;
    private Integer number_installs;

    private String downLoadLink;

    private String versionCode;
    private String packageName;
    public String minSDK;
    public String targetSDK;
    public Long osTypeId;
    public String osTypeName;
    public Long osId;
    public String osName;
    public Long fileSize;
    private String publishState;
    private String appCategoryName;
    private String appCategoryID;
    public String[] keyword;
    public Double averageRateIndex;
    public List<String> permissionDetails;
    public DateTime creationDateTime;
    public DateTime lastModifyDate;

    public Boolean isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getBulk() {
        return bulk;
    }

    public void setBulk(String bulk) {
        this.bulk = bulk;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Integer getNumber_installs() {
        return number_installs;
    }

    public void setNumber_installs(Integer number_installs) {
        this.number_installs = number_installs;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getDownLoadLink() {
        return downLoadLink;
    }

    public void setDownLoadLink(String downLoadLink) {
        this.downLoadLink = downLoadLink;
    }

    public String getIconThumbNail() {
        return iconThumbNail;
    }

    public void setIconThumbNail(String iconThumbNail) {
        this.iconThumbNail = iconThumbNail;
    }

    public String getMinSDK() {
        return minSDK;
    }

    public void setMinSDK(String minSDK) {
        this.minSDK = minSDK;
    }

    public String getTargetSDK() {
        return targetSDK;
    }

    public void setTargetSDK(String targetSDK) {
        this.targetSDK = targetSDK;
    }

    public Long getOsTypeId() {
        return osTypeId;
    }

    public void setOsTypeId(Long osTypeId) {
        this.osTypeId = osTypeId;
    }

    public String getOsTypeName() {
        return osTypeName;
    }

    public void setOsTypeName(String osTypeName) {
        this.osTypeName = osTypeName;
    }

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

    public String getPublishState() {
        return publishState;
    }

    public void setPublishState(String publishState) {
        this.publishState = publishState;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getAppCategoryName() {
        return appCategoryName;
    }

    public void setAppCategoryName(String appCategoryName) {
        this.appCategoryName = appCategoryName;
    }

    public String getAppCategoryID() {
        return appCategoryID;
    }

    public void setAppCategoryID(String appCategoryID) {
        this.appCategoryID = appCategoryID;
    }

    public String[] getKeyword() {
        return keyword;
    }

    public void setKeyword(String[] keyword) {
        this.keyword = keyword;
    }

    public Double getAverageRateIndex() {
        return averageRateIndex;
    }

    public void setAverageRateIndex(Double averageRateIndex) {
        this.averageRateIndex = averageRateIndex;
    }

    public List<String> getPermissionDetails() {
        return permissionDetails;
    }

    public void setPermissionDetails(List<String> permissionDetails) {
        this.permissionDetails = permissionDetails;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public DateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(DateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public DateTime getLastModifyDate() {
        return lastModifyDate;
    }

    public void setLastModifyDate(DateTime lastModifyDate) {
        this.lastModifyDate = lastModifyDate;
    }

    public static ProductVO buildProductVO(App app) throws Exception {
        ProductVO productVO = new ProductVO();
        productVO.setPackageName(app.getPackageName());
        productVO.setDeveloper(app.getDeveloper().getFullName());

        AppPackage appMainPackage = app.getMainPackage();

        productVO.setVersionCode(appMainPackage.getVersionCode());
        productVO.setVersionName(appMainPackage.getVersionName());
        productVO.setTitle(app.getTitle());
        productVO.setMinSDK(appMainPackage.getMinSDK());
        productVO.setShortDescription(app.getShortDescription());
        productVO.setTargetSDK(appMainPackage.getTargetSDK());
        productVO.setOsTypeId(app.getOsType().getId());
        productVO.setOsTypeName(app.getOsType().getName());
        productVO.setOsId(app.getOs() != null ? app.getOs().getId() : null);
        productVO.setOsName(app.getOs() != null ? app.getOs().getOsName() : null);
        productVO.setId(app.getId());
        productVO.setPublishState(appMainPackage.getPublishState().toString());
        if(appMainPackage.getPackFile()!=null){
            File packFile = appMainPackage.getPackFile();
            productVO.setFileSize(packFile.getFileSize());
        }

        String appMainPackagePermissionDetailAsString = appMainPackage.getPermissionDetail();
        if (appMainPackagePermissionDetailAsString != null && !appMainPackagePermissionDetailAsString.trim().isEmpty()) {
            String[] permissionDetailAsArray = appMainPackagePermissionDetailAsString.split(",");
            List<String> permissionDetailList = new ArrayList<>();
            for (String permissionDetail : permissionDetailAsArray) {
                permissionDetailList.add(permissionDetail.trim());
            }
            productVO.setPermissionDetails(permissionDetailList);
        }

        AppCategory appCategory = app.getAppCategory() != null ? app.getAppCategory() : null;
        if (appCategory != null) {
            productVO.setAppCategoryName(appCategory.getCategoryName());
            productVO.setAppCategoryID(appCategory.getId().toString());
        }

        String webServiceBasePath = ConfigUtil.getProperty(ConfigUtil.APP_RESTAPI_BASEPATH);
        String baseServiceURL = ConfigUtil.getProperty(ConfigUtil.APPLICATION_PATH) + webServiceBasePath;

        File iconFile = appMainPackage.getIconFile();
        if (iconFile != null) {
            try {
                String basePath = baseServiceURL + "getFile?path=";

                String thumbUrl = AppUtils.getImageThumbNail(
                        iconFile.getFilePath() == null ? "" : iconFile.getFilePath(),
                        iconFile.getFileName() == null ? "" : iconFile.getFileName());
                thumbUrl = basePath + "File:" + thumbUrl;
                String thumbPath = StringEscapeUtils.escapeHtml4(thumbUrl);
                productVO.setIconThumbNail(thumbPath);
            } catch (Exception ex) {

            }

            String path = iconFile.getFilePath() == null ? "" : iconFile.getFilePath();
            if (path != null)
                productVO.setIcon(ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", path));
        }


        String downPackPath = appMainPackage.getPackFile() == null ? "" :
                appMainPackage.getPackFile().getFilePath();
        if (downPackPath != null) {
            downPackPath =
                    ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", downPackPath);
        }

        productVO.setDownLoadLink(downPackPath);
        AppElasticService.AppKeyWordCriteria appKeyWordCriteria = new AppElasticService.AppKeyWordCriteria();
        appKeyWordCriteria.setAppId(productVO.getId());
        List<AppElasticService.AppKeyWordVO> appKeyWordVOs = AppElasticService.Instance.listAppKeyWord(appKeyWordCriteria, 0, 01);
        String[] stringModels = null;
        if (appKeyWordVOs != null && !appKeyWordVOs.isEmpty()) {
            stringModels = appKeyWordVOs.get(0).getKeyword().replace("#", "").split(" ");
        }
        if (app.getId()!=null && app.getPackageName()!=null && !app.getPackageName().trim().equals("")) {
            Double average = getAverageRateFromApp(app.getId(),app.getPackageName());
            if (productVO.getAverageRateIndex() == null || productVO.getAverageRateIndex().equals(Double.valueOf(0))) {
                productVO.setAverageRateIndex(average);
            }
        }
        productVO.setKeyword(stringModels);

        if(PrincipalUtil.hasPermission(Access.APP_REMOVE)){
           productVO.setDeleted(app.getDeleted());
        }
        productVO.setCreationDateTime(app.getCreationDate());
        productVO.setLastModifyDate(app.getLastModifyDate());

        return productVO;
    }

    private static Double getAverageRateFromApp(Long id, String packageName) {
        AppElasticService.AppInstallCriteria appInstallCriteria = new AppElasticService.AppInstallCriteria();
        appInstallCriteria.appId = id;
        appInstallCriteria.appPackageName = packageName;

        try {
            Double average = AppElasticService.Instance.gerAverageInstallRate(appInstallCriteria, 0, -1, null, true);
            if(average!=null && average.equals(Double.valueOf(-1))){
                average= Double.valueOf(0);
            }
            return average;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static ProductVO buildProductVO(App app, Double averageRateIndex) throws Exception {
        ProductVO productVO = buildProductVO(app);
        productVO.setAverageRateIndex(averageRateIndex);

        return productVO;
    }

    public static ProductVO buildProductVO(AppService.AppSearchResultModel appSearchResultModel) throws Exception {
        ProductVO productVO = new ProductVO();

        if (appSearchResultModel!=null) {
        productVO.setPackageName(appSearchResultModel.getPackageName());
        productVO.setDeveloper(appSearchResultModel.getDeveloperName());
        productVO.setVersionCode(
                appSearchResultModel.getApp_Pack_versionCode() == null ? appSearchResultModel.getApp_mainPack_versionCode() : appSearchResultModel.getApp_Pack_versionCode());
        productVO.setVersionName(
                appSearchResultModel.getApp_Pack_versionName() == null ? appSearchResultModel.getApp_mainPack_versionName() : appSearchResultModel.getApp_Pack_versionName());
        productVO.setTitle(appSearchResultModel.getTitle());
        productVO.setShortDescription(appSearchResultModel.getShortDescription());
        productVO.setMinSDK(
                appSearchResultModel.getApp_Pack_minSDK() == null ? appSearchResultModel.getApp_mainPack_minSDK() : appSearchResultModel.getApp_mainPack_minSDK());
        productVO.setTargetSDK(
                appSearchResultModel.getApp_Pack_targetSDK() == null ? appSearchResultModel.getApp_mainPack_targetSDK() : appSearchResultModel.getApp_Pack_targetSDK());
        productVO.setDeveloper(appSearchResultModel.getDeveloperName());
        productVO.setOsTypeId(appSearchResultModel.getOsType().getId());
        productVO.setOsTypeName(appSearchResultModel.getOsType().getName());
        productVO.setOsId(appSearchResultModel.getOsId());
        productVO.setOsName(appSearchResultModel.getOsName());
        productVO.setId(appSearchResultModel.getAppId());
        productVO.setPublishState(appSearchResultModel.getPublishState() != null ? appSearchResultModel.getPublishState().toString() : "");
        productVO.setFileSize(appSearchResultModel.getApp_mainPack_fileSize());
            if(PrincipalUtil.hasPermission(Access.APP_REMOVE)){
                productVO.setDeleted(appSearchResultModel.getDeleted());
            }

            if (appSearchResultModel.getAppId()!=null) {
                AppElasticService.AppInstallCriteria appInstallCriteria = new AppElasticService.AppInstallCriteria();
                appInstallCriteria.appId = appSearchResultModel.getAppId();
                Double averageRateIndex = AppElasticService.Instance.gerAverageInstallRate(appInstallCriteria ,0,-1,null,false);
                productVO.setAverageRateIndex(new Double(0));

                if (!averageRateIndex.equals(-1.0)) {
                    productVO.setAverageRateIndex(averageRateIndex);
                }

            }


        AppCategory appCategory = appSearchResultModel.getAppCategory();
        if (appCategory != null) {
            productVO.setAppCategoryName(appSearchResultModel.getAppCategory().toString());
            productVO.setAppCategoryID(appCategory.getId().toString());
        }

        String webServiceBasePath = ConfigUtil.getProperty(ConfigUtil.APP_RESTAPI_BASEPATH);
        String baseServiceURL = ConfigUtil.getProperty(ConfigUtil.APPLICATION_PATH) + webServiceBasePath;

        productVO.setPermissionDetails(appSearchResultModel.getPermissionDetailList());

        try {
            String basePath = baseServiceURL + "getFile?path=";

            String thumbUrl = AppUtils.getImageThumbNail(
                    appSearchResultModel.getApp_Pack_iconPath() == null ? appSearchResultModel.getApp_mainPack_iconPath() : appSearchResultModel.getApp_Pack_iconPath(),
                    appSearchResultModel.getApp_Pack_iconFile() == null ? appSearchResultModel.getApp_mainPack_iconFile() : appSearchResultModel.getApp_Pack_iconFile());
            thumbUrl = basePath + "File:" + thumbUrl;// + thumbUrl.replace("\\", "/");
            String thumbPath = StringEscapeUtils.escapeHtml4(thumbUrl);
            productVO.setIconThumbNail(thumbPath);
        } catch (Exception ex) {

        }

        String path = appSearchResultModel.getApp_Pack_iconPath() == null ? appSearchResultModel.getApp_mainPack_iconPath() :
                appSearchResultModel.getApp_Pack_iconPath();
        if(path != null)
            productVO.setIcon(ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", path));


        String downPackPath = appSearchResultModel.getApp_Pack_filePath() == null ? appSearchResultModel.getApp_mainPack_filePath() :
                appSearchResultModel.getApp_Pack_filePath();
        if(downPackPath != null) {
            downPackPath =
                ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", downPackPath);
        }

        productVO.setFileSize(appSearchResultModel.getApp_mainPack_fileSize());
        productVO.setDownLoadLink(downPackPath);

        AppElasticService.AppKeyWordCriteria appKeyWordCriteria = new AppElasticService.AppKeyWordCriteria();
        appKeyWordCriteria.setAppId(productVO.getId());
        List<AppElasticService.AppKeyWordVO> appKeyWordVOs = AppElasticService.Instance.listAppKeyWord(appKeyWordCriteria, 0, 01);
        String[] stringModels = null;
        if (appKeyWordVOs != null && !appKeyWordVOs.isEmpty()) {
            stringModels = appKeyWordVOs.get(0).getKeyword().replace("#", "").split(" ");
        }
        productVO.setKeyword(stringModels);

            if (appSearchResultModel.getAppId()!=null && appSearchResultModel.getPackageName()!=null && !appSearchResultModel.getPackageName().trim().equals("")) {
                Double average = getAverageRateFromApp(appSearchResultModel.getAppId(),appSearchResultModel.getPackageName());
                if (productVO.getAverageRateIndex() == null || productVO.getAverageRateIndex().equals(Double.valueOf(0))) {
                    productVO.setAverageRateIndex(average);
                }
            }
            productVO.setKeyword(stringModels);

            productVO.setLastModifyDate(appSearchResultModel.getLastModifyDate());
            productVO.setCreationDateTime(appSearchResultModel.getCreationDateTime());
        }


        return productVO;
    }

    public static void main(String[] args) throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        Transaction tx = null;

        try {
            OSType osType = OSTypeService.Instance.getOSTypeByName("ANDROID", session);

            String queryString = "select appPackages from App app inner join app.appPackages appPackages where app.osType = :osType_";
            Query query = session.createQuery(queryString);
            query.setParameter("osType_", osType);
            List<AppPackage> packageList = query.list();

            tx = session.beginTransaction();

            for (AppPackage appPackage : packageList) {
                File packFile = appPackage.getPackFile();
                String fileKey = packFile.getFilePath();
                try {
                    IAPPPackageService appPackageService = AppPackageService.Instance.processPackageFile(fileKey, osType);

                    List<String> permissionDetailList = appPackageService.getPermissions();
                    String permissionDetailAsString = String.join(",", permissionDetailList);
                    appPackage.setPermissionDetail(permissionDetailAsString);

                    session.saveOrUpdate(appPackage);
                } catch (Exception e) {
                    if(e instanceof AppPackageService.APPPackageException)
                        continue;
                    else
                        throw e;
                }
            }

            tx.commit();

            System.out.println("23123");
        } catch (Exception ex) {
            ex.printStackTrace();
            if (tx != null)
                tx.rollback();
        } finally {
            session.close();
        }

        System.exit(0);
    }
}
