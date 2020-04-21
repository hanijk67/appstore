package com.fanap.midhco.appstore.iosUtil;
import java.io.Serializable;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;


public class IPAInfo implements Serializable {
    private String minimumOSVersion;
    private String bundleVersionString;
    private String buildNumber;
    private String bundleName;
    private String requiredDeviceCapabilities;
    private byte[] bundleIcon;
    private String platformVersion;
    private Boolean iPadSupport;
    private Boolean iPhoneSupport;
    private Boolean hasCorrectHashValue;
    private String bundleIdentifier;
    private String provisioningProfileName;
    private String provisioningProfileCreationDate;
    private String provisioningProfileExpirationDate;
    private List<String>provisioningProfileDevices;
    private List<String>permissions;
    private Map<String , String> permissionsMap;
    private String teamIdentifier;
    private String teamName;
    private byte[] infoPlistFile;
    private byte[] mobileProvisionFile;
    private long fileSize;
    private String bundleIconFileName;
    private String appIDName;
    private Integer version;
    private Certificate certificate;

    public String getBundleIconFileName() {
        return bundleIconFileName;
    }
    public void setBundleIconFileName(String bundleIconFileName) {
        this.bundleIconFileName = bundleIconFileName;
    }
    public long getFileSize() {
        return fileSize;
    }
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    public String getProvisioningProfileCreationDate() {
        return provisioningProfileCreationDate;
    }
    public void setProvisioningProfileCreationDate(
            String provisioningProfileCreationDate) {
        this.provisioningProfileCreationDate = provisioningProfileCreationDate;
    }
    public byte[] getInfoPlistFile() {
        return infoPlistFile;
    }
    public void setInfoPlistFile(byte[] infoPlistFile) {
        this.infoPlistFile = infoPlistFile;
    }
    public byte[] getMobileProvisionFile() {
        return mobileProvisionFile;
    }
    public void setMobileProvisionFile(byte[] mobileProvisionFile) {
        this.mobileProvisionFile = mobileProvisionFile;
    }
    public String getMinimumOSVersion() {
        return minimumOSVersion;
    }
    public void setMinimumOSVersion(String minimumOSVersion) {
        this.minimumOSVersion = minimumOSVersion;
    }
    public String getBundleVersionString() {
        return bundleVersionString;
    }
    public void setBundleVersionString(String bundleVersionString) {
        this.bundleVersionString = bundleVersionString;
    }
    public String getBuildNumber() {
        return buildNumber;
    }
    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }
    public String getBundleName() {
        return bundleName;
    }
    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }
    public String getRequiredDeviceCapabilities() {
        return requiredDeviceCapabilities;
    }
    public void setRequiredDeviceCapabilities(String requiredDeviceCapabilities) {
        this.requiredDeviceCapabilities = requiredDeviceCapabilities;
    }
    public byte[] getBundleIcon() {
        return bundleIcon;
    }
    public void setBundleIcon(byte[] bundleIcon) {
        this.bundleIcon = bundleIcon;
    }
    public String getPlatformVersion() {
        return platformVersion;
    }
    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }
    public Boolean getiPadSupport() {
        return iPadSupport;
    }
    public void setiPadSupport(Boolean iPadSupport) {
        this.iPadSupport = iPadSupport;
    }
    public Boolean getiPhoneSupport() {
        return iPhoneSupport;
    }
    public void setiPhoneSupport(Boolean iPhoneSupport) {
        this.iPhoneSupport = iPhoneSupport;
    }
    public String getBundleIdentifier() {
        return bundleIdentifier;
    }
    public void setBundleIdentifier(String bundleIdentifier) {
        this.bundleIdentifier = bundleIdentifier;
    }
    public String getProvisioningProfileName() {
        return provisioningProfileName;
    }
    public void setProvisioningProfileName(String provisioningProfileName) {
        this.provisioningProfileName = provisioningProfileName;
    }
    public String getProvisioningProfileExpirationDate() {
        return provisioningProfileExpirationDate;
    }
    public void setProvisioningProfileExpirationDate(
            String provisioningProfileExpirationDate) {
        this.provisioningProfileExpirationDate = provisioningProfileExpirationDate;
    }
    public List<String> getProvisioningProfileDevices() {
        return provisioningProfileDevices;
    }
    public void setProvisioningProfileDevices(
            List<String> provisioningProfileDevices) {
        this.provisioningProfileDevices = provisioningProfileDevices;
    }
    public String getTeamIdentifier() {
        return teamIdentifier;
    }
    public void setTeamIdentifier(String teamIdentifier) {
        this.teamIdentifier = teamIdentifier;
    }
    public String getTeamName() {
        return teamName;
    }
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Boolean getHasCorrectHashValue() {
        return hasCorrectHashValue;
    }

    public void setHasCorrectHashValue(Boolean hasCorrectHashValue) {
        this.hasCorrectHashValue = hasCorrectHashValue;
    }

    public String getAppIDName() {
        return appIDName;
    }

    public void setAppIDName(String appIDName) {
        this.appIDName = appIDName;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public Map<String, String> getPermissionsMap() {
        return permissionsMap;
    }

    public void setPermissionsMap(Map<String, String> permissionsMap) {
        this.permissionsMap = permissionsMap;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
