package com.fanap.midhco.appstore.applicationUtils.asyncMessages;

/**
 * Created by F.Seyfi on 6/21/2016.
 */
public class ClientMessage {

    private long id;
    private long senderMessageId;
    private String senderName;
    private long senderId;
    private byte type;
    private String content;


    public ClientMessage() {

    }

    public ClientMessage(long m_id, long m_senderMessageId, String m_senderName, long m_senderId,
                         byte m_type, String m_content) {

        this.id = m_id;
        this.senderMessageId = m_senderMessageId;
        this.senderName = m_senderName;
        this.senderId = m_senderId;
        this.type = m_type;
        this.content = m_content;


    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSenderMessageId() {
        return senderMessageId;
    }

    public void setSenderMessageId(long senderMessageId) {
        this.senderMessageId = senderMessageId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
