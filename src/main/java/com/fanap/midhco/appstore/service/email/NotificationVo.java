package com.fanap.midhco.appstore.service.email;

/**
 * Created by Yadmand on 3/5/2017.
 */
public class NotificationVo {
    private NotificationType notificationType;
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

}
