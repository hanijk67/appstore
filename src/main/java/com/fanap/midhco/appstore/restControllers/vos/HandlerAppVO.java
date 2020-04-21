package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.HandlerApp;
import com.fanap.midhco.appstore.service.handlerApp.HandlerAppService;
import org.json.JSONObject;

/**
 * Created by A.Moshiri on 8/15/2017.
 */
public class HandlerAppVO  implements Comparable{

    public HandlerAppVO() {
    }

    public HandlerAppVO(String inputString) {
        JSONObject jsonObject = new JSONObject(inputString);

        try {
            if (jsonObject.has("osId")) {
                this.osId = jsonObject.getLong("osId");
            }

            if (jsonObject.has("id")) {
                this.id = jsonObject.getLong("id");
            }

            if (jsonObject.has("orgId")) {
                this.orgId = jsonObject.getLong("orgId");
            }

            if (jsonObject.has("environmentId")) {
                this.environmentId = jsonObject.getLong("environmentId");
            }

            if (jsonObject.has("isActive")) {
                if (jsonObject.get("isActive") != null) {
                    this.isActive = jsonObject.getBoolean("isActive");
                }
            }


            if (jsonObject.has("isDefault")) {
                if (jsonObject.get("isDefault") != null) {
                    this.isDefault = jsonObject.getBoolean("isDefault");
                }
            }

            if (jsonObject.has("versionCode")) {
                this.versionCode = jsonObject.getLong("versionCode");
            }

            if (jsonObject.has("testFileHandlerAppUploadTime")) {
                this.testFileHandlerAppUploadTime = jsonObject.getLong("testFileHandlerAppUploadTime");
            }

            if (jsonObject.has("fileHandlerAppUploadTime")) {
                this.fileHandlerAppUploadTime = jsonObject.getLong("fileHandlerAppUploadTime");
            }

            if (jsonObject.has("fileHandlerAppKey")) {
                this.fileHandlerAppKey = jsonObject.getString("fileHandlerAppKey");
            }

            if (jsonObject.has("testFileHandlerAppKey")) {
                this.testFileHandlerAppKey = jsonObject.getString("testFileHandlerAppKey");
            }
        } finally {

        }
    }

    Long id;
    Long versionCode;
    Boolean isActive;
    Boolean isDefault;
    Long fileHandlerAppUploadTime;
    Long testFileHandlerAppUploadTime;
    String fileHandlerAppKey;
    String testFileHandlerAppKey;
    String fileHandlerAppKey32Bit;
    String testFileHandlerAppKey32Bit;
    String fileName;
    String testFileName;
    String osName;
    Long osId;

    Long orgId;
    Long environmentId;
    String environmentName;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Long versionCode) {
        this.versionCode = versionCode;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public Long getFileHandlerAppUploadTime() {
        return fileHandlerAppUploadTime;
    }

    public void setFileHandlerAppUploadTime(Long fileHandlerAppUploadTime) {
        this.fileHandlerAppUploadTime = fileHandlerAppUploadTime;
    }

    public Long getTestFileHandlerAppUploadTime() {
        return testFileHandlerAppUploadTime;
    }

    public void setTestFileHandlerAppUploadTime(Long testFileHandlerAppUploadTime) {
        this.testFileHandlerAppUploadTime = testFileHandlerAppUploadTime;
    }

    public String getFileHandlerAppKey() {
        return fileHandlerAppKey;
    }

    public void setFileHandlerAppKey(String fileHandlerAppKey) {
        this.fileHandlerAppKey = fileHandlerAppKey;
    }

    public String getTestFileHandlerAppKey() {
        return testFileHandlerAppKey;
    }

    public void setTestFileHandlerAppKey(String testFileHandlerAppKey) {
        this.testFileHandlerAppKey = testFileHandlerAppKey;
    }

    public String getFileHandlerAppKey32Bit() {
        return fileHandlerAppKey32Bit;
    }

    public void setFileHandlerAppKey32Bit(String fileHandlerAppKey32Bit) {
        this.fileHandlerAppKey32Bit = fileHandlerAppKey32Bit;
    }

    public String getTestFileHandlerAppKey32Bit() {
        return testFileHandlerAppKey32Bit;
    }

    public void setTestFileHandlerAppKey32Bit(String testFileHandlerAppKey32Bit) {
        this.testFileHandlerAppKey32Bit = testFileHandlerAppKey32Bit;
    }

    public Long getOsId() {
        return osId;
    }

    public void setOsId(Long osId) {
        this.osId = osId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTestFileName() {
        return testFileName;
    }

    public void setTestFileName(String testFileName) {
        this.testFileName = testFileName;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

    public static HandlerAppVO buildHandlerAppVO(HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel) {
        HandlerAppVO handlerAppVO = new HandlerAppVO();
        try {
            handlerAppVO.setActive(handlerAppSearchResultModel.getActive());
            handlerAppVO.setDefault(handlerAppSearchResultModel.getDefault());

            if (handlerAppSearchResultModel.getFileHandlerAppKey() != null) {
//                String handlerAppFilePath = FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", handlerAppSearchResultModel.getFileHandlerAppKey());
                String handlerAppFilePath = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerAppSearchResultModel.getFileHandlerAppKey());
                handlerAppVO.setFileHandlerAppKey(handlerAppFilePath);
                handlerAppVO.setFileHandlerAppUploadTime(
                        handlerAppSearchResultModel.getUploadedFileDate() != null ? handlerAppSearchResultModel.getUploadedFileDate().getTime() : null
                );
            }

            if (handlerAppSearchResultModel.getTestFileHandlerAppKey() != null) {
//                String testHandlerAppFilePath = FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", handlerAppSearchResultModel.getTestFileHandlerAppKey());
                String testHandlerAppFilePath = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerAppSearchResultModel.getTestFileHandlerAppKey());
                handlerAppVO.setTestFileHandlerAppKey(testHandlerAppFilePath);
                handlerAppVO.setTestFileHandlerAppUploadTime(
                        handlerAppSearchResultModel.getUploadedTestFileDate() != null ? handlerAppSearchResultModel.getUploadedTestFileDate().getTime() : null
                );
            }

            //32Bit Versions
            if (handlerAppSearchResultModel.getFileHandlerAppKey32Bit() != null) {
                String handlerAppFilePath32Bit = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerAppSearchResultModel.getFileHandlerAppKey32Bit());
                handlerAppVO.setFileHandlerAppKey32Bit(handlerAppFilePath32Bit);
                if (handlerAppVO.getFileHandlerAppUploadTime()==null) {
                    handlerAppVO.setFileHandlerAppUploadTime(
                            handlerAppSearchResultModel.getUploadedFileDate() != null ? handlerAppSearchResultModel.getUploadedFileDate().getTime() : null
                    );
                }
            }

            if (handlerAppSearchResultModel.getTestFileHandlerAppKey32Bit() != null) {
                String testHandlerAppFilePath32Bit = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerAppSearchResultModel.getTestFileHandlerAppKey32Bit());
                handlerAppVO.setTestFileHandlerAppKey32Bit(testHandlerAppFilePath32Bit);
                if (handlerAppVO.getTestFileHandlerAppUploadTime()==null) {
                    handlerAppVO.setTestFileHandlerAppUploadTime(
                            handlerAppSearchResultModel.getUploadedFileDate() != null ? handlerAppSearchResultModel.getUploadedFileDate().getTime() : null
                    );
                }
            }
            handlerAppVO.setVersionCode(handlerAppSearchResultModel.getVersionCode());
            handlerAppVO.setId(handlerAppSearchResultModel.getId());
            handlerAppVO.setOsId(handlerAppSearchResultModel.getOsId());
            handlerAppVO.setOsName(handlerAppSearchResultModel.getOsName());
            handlerAppVO.setOrgId(handlerAppSearchResultModel.getOrganizationId());
            if (handlerAppSearchResultModel.getOsEnvironment() != null) {
                handlerAppVO.setEnvironmentId(handlerAppSearchResultModel.getOsEnvironment().getId());
                handlerAppVO.setEnvironmentName(handlerAppSearchResultModel.getOsEnvironment().getEnvName());
            }

            if (handlerAppSearchResultModel.getOrganizationId() != null) {
                handlerAppVO.setOrgId(handlerAppSearchResultModel.getOrganizationId());
            }

        } catch (NumberFormatException e) {
            throw e;
        }

        return handlerAppVO;
    }

    public static HandlerAppVO buildHandlerAppVO(HandlerApp handlerApp) {
        HandlerAppVO handlerAppVO = new HandlerAppVO();
        handlerAppVO.setActive(handlerApp.getActive());
        handlerAppVO.setDefault(handlerApp.getDefaultForOrganization());

        if (handlerApp.getHandlerFile() != null && handlerApp.getHandlerFile().getFilePath() != null && !handlerApp.getHandlerFile().getFilePath().trim().equals("")) {
//            String fileDownloadPath = FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", handlerApp.getHandlerFile().getFilePath());
            String fileDownloadPath = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerApp.getHandlerFile().getFilePath());
            handlerAppVO.setFileHandlerAppKey(fileDownloadPath);
            handlerAppVO.setFileHandlerAppUploadTime(
                    handlerApp.getUploadedFileDate() != null ? handlerApp.getUploadedFileDate().getTime() : null
            );
            handlerAppVO.setFileName(handlerApp.getHandlerFile().getFileName());
        }

        if (handlerApp.getTestHandlerFile() != null && handlerApp.getTestHandlerFile().getFilePath() != null && !handlerApp.getTestHandlerFile().getFilePath().trim().equals("")) {
//            String testFileDownloadPath = FileServerService.FILE_DOWNLOAD_SERVER_PATH.replace("${key}", handlerApp.getTestHandlerFile().getFilePath());
            String testFileDownloadPath = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerApp.getTestHandlerFile().getFilePath());
            ;
            handlerAppVO.setTestFileHandlerAppKey(testFileDownloadPath);
            handlerAppVO.setTestFileHandlerAppUploadTime(
                    handlerApp.getUploadedTestFileDate() != null ? handlerApp.getUploadedTestFileDate().getTime() : null
            );
            handlerAppVO.setTestFileName(handlerApp.getTestHandlerFile().getFileName());
        }


        if (handlerApp.getHandlerFile32bit() != null && handlerApp.getHandlerFile32bit().getFilePath() != null && !handlerApp.getHandlerFile32bit().getFilePath().trim().equals("")) {
            String fileDownloadPath32Bit = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerApp.getHandlerFile32bit().getFilePath());
            handlerAppVO.setFileHandlerAppKey32Bit(fileDownloadPath32Bit);
            handlerAppVO.setFileHandlerAppUploadTime(
                    handlerApp.getUploadedFileDate() != null ? handlerApp.getUploadedFileDate().getTime() : null
            );
            if (handlerAppVO.getFileName()==null) {
                handlerAppVO.setFileName(handlerApp.getHandlerFile32bit().getFileName());
            }
        }

        if (handlerApp.getTestHandlerFile32bit() != null && handlerApp.getTestHandlerFile32bit().getFilePath() != null && !handlerApp.getTestHandlerFile32bit().getFilePath().trim().equals("")) {
            String testFileDownloadPath32Bit = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH).replace("${key}", handlerApp.getTestHandlerFile32bit().getFilePath());
            ;
            handlerAppVO.setTestFileHandlerAppKey32Bit(testFileDownloadPath32Bit);
            handlerAppVO.setTestFileHandlerAppUploadTime(
                    handlerApp.getUploadedTestFileDate() != null ? handlerApp.getUploadedTestFileDate().getTime() : null
            );
            if (handlerAppVO.getTestFileName()==null) {
                handlerAppVO.setTestFileName(handlerApp.getTestHandlerFile32bit().getFileName());
            }
        }



        handlerAppVO.setVersionCode(handlerApp.getVersionCode());
        handlerAppVO.setId(handlerApp.getId());
        handlerAppVO.setOsId(handlerApp.getParentOS().getId());
        if (handlerApp.getOsEnvironment() != null) {
            handlerAppVO.setEnvironmentId(handlerApp.getOsEnvironment().getId());
        }
        if (handlerApp.getOrganization() != null) {
            handlerAppVO.setOrgId(handlerApp.getOrganization().getId());
        }

        return handlerAppVO;
    }

    @Override
    public int compareTo(Object handlerAppVo) {
        return ((HandlerAppVO) handlerAppVo).getVersionCode().compareTo(this.getVersionCode()) ;
    }
}
