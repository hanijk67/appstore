package com.fanap.midhco.appstore.service.email;

/**
 * Created by khashayar on 10/12/2015.
 */
public class Request {
    private Integer messageType;
    private String serviceName;
    private String content;
    private RequestHeader requestHeader;
    public Request() {}

    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public RequestHeader getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(RequestHeader requestHeader) {
        this.requestHeader = requestHeader;
    }
    public Integer getMessageType() {
        return messageType;
    }
    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
