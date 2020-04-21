package com.fanap.midhco.appstore.entities.async;

/**
 * Created by h.mehrara on 1/28/2015.
 */
public class MessageVO {
    private String peerName;
    private long[] receivers;
    private long collapseId;
    private long groupId;
    private int index;
    private long messageId;
    private long ttl;
    private String content;

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public long[] getReceivers() {
        return receivers;
    }

    public void setReceivers(long[] receivers) {
        this.receivers = receivers;
    }

    public long getCollapseId() {
        return collapseId;
    }

    public void setCollapseId(long collapseId) {
        this.collapseId = collapseId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
