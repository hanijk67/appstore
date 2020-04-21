package com.fanap.midhco.appstore.applicationUtils.asyncMessages;

/**
 * Created by F.Seyfi on 6/21/2016.
 */
public class PeerInfoVO {

    private long peerId;
    private String deviceId;
    private String appId;
    private String username;
    private String password;


    public PeerInfoVO() {
    }

    public PeerInfoVO(long m_peerId, String m_deviceId, String m_appId,
                      String m_username, String m_password) {

        this.peerId = m_peerId;
        this.deviceId = m_deviceId;
        this.appId = m_appId;
        this.username = m_username;
        this.password = m_password;

    }


    public long getPeerId() {
        return peerId;
    }

    public void setPeerId(long peerId) {
        this.peerId = peerId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
