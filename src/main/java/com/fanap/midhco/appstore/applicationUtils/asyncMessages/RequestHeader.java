package com.fanap.midhco.appstore.applicationUtils.asyncMessages;

/**
 * Created by Heidari on 11/1/15.
 */
public class RequestHeader implements Cloneable {
    private String trackerId;
    private String token;
    private String id_token;
    private long requesterPostId;
    private Long[] userIds;
    private Long[] ssoIds;
    private Long peerId;
    private long asyncTracker;

    public RequestHeader() {}

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

    public void setPeerId(Long peerId) {
        this.peerId = peerId;
    }

    public Long[] getUserIds() {
        return userIds;
    }

    public void setUserIds(Long[] userIds) {
        this.userIds = userIds;
    }

    public long getAsyncTracker() {
        return asyncTracker;
    }

    public void setAsyncTracker(long asyncTracker) {
        this.asyncTracker = asyncTracker;
    }

    public String getId_token() {
        return id_token;
    }

    public void setId_token(String id_token) {
        this.id_token = id_token;
    }

    public Long[] getSsoIds() {
        return ssoIds;
    }

    public void setSsoIds(Long[] ssoIds) {
        this.ssoIds = ssoIds;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
