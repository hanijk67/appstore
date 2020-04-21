package com.fanap.midhco.appstore.applicationUtils.asyncMessages;

/**
 * Created by F.Seyfi on 6/21/2016.
 */
public enum MessageType {


    PING(0), SERVER_REGISTER(1), DEVICE_REGISTER(2), MESSAGE(3), MESSAGE_ACK_NEEDED(4), MESSAGE_SENDER_ACK_NEEDED(5), ACK(6);
    private int value;

    public int getValue() {
        return value;
    }

    private MessageType(int value) {
        this.value = value;
    }

}
