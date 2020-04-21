package com.fanap.midhco.appstore.service.email;

/**
 * Created by Heidari on 11/1/15.
 */
public class RequestHeader implements Cloneable {
    private String trackerId;
    private String token;
    private long requesterPostId;
    private Long ssoUserId;
    private Long [] userIds;// User IDs of people whom message must be sent to
    private Long peerId;    // For use between Engine and TaskManager. Will be removed when sending to UI
    private String apiToken;
    private long senderMessageId;
    private String peerName;
    private String ssoUserName;


    public String getSsoUserName() {
        return ssoUserName;
    }

    public void setSsoUserName(String ssoUserName) {
        this.ssoUserName = ssoUserName;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public RequestHeader() {}

    public long getSenderMessageId() {
        return senderMessageId;
    }

    public void setSenderMessageId(long senderMessageId) {
        this.senderMessageId = senderMessageId;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public long getRequesterPostId() {
        return requesterPostId;
    }

    public void setRequesterPostId(long requesterPostId) {
        this.requesterPostId = requesterPostId;
    }

    public String getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(String trackerId) {
        this.trackerId = trackerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getPeerId() {
        return peerId;
    }

    public Long getSsoUserId() {
        return ssoUserId;
    }

    public void setSsoUserId(Long ssoUserId) {
        this.ssoUserId = ssoUserId;
    }

    public Long[] getUserIds() {
        return userIds;
    }

    public void setUserIds(Long[] userIds) {
        this.userIds = userIds;
    }

    public void setPeerId(Long peerId) {
        this.peerId = peerId;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
