package com.fanap.midhco.appstore.entities.async;

/**
 * Created by h.mehrara on 1/28/2015.
 */
public class MessageWrapperVO {

    private byte type;
    private String content;

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
