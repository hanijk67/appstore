package com.fanap.midhco.appstore.applicationUtils.asyncMessages;

/**
 * Created by Bastam on 10/12/2015.
 */
public class Request {
    private String engineName;
    private RequestHeader requestHeader;
    private EngineMessageType messageType;
    private String content;

    public Request() {}

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public EngineMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(EngineMessageType messageType) {
        this.messageType = messageType;
    }

    public RequestHeader getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(RequestHeader requestHeader) {
        this.requestHeader = requestHeader;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
