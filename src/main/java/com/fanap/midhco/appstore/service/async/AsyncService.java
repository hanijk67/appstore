package com.fanap.midhco.appstore.service.async;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.async.MessageWrapperVO;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.io.UnsupportedEncodingException;

/**
 * Created by admin123 on 4/5/2017.
 */
public class AsyncService {
    static final Logger logger = LogManager.getLogger();

    public static final AsyncService Instance = new AsyncService();

    private AsyncService() {
    }

    static ConnectionFactory factory;

    static {
        try {
            factory = new ActiveMQConnectionFactory(
                    ConfigUtil.getProperty(ConfigUtil.QUEUE_SERVER_USERNAME),
                    ConfigUtil.getProperty(ConfigUtil.QUEUE_SERVER_PASSWORD),
                    new StringBuilder()
                            .append("failover:(tcp://")
                            .append(ConfigUtil.getProperty(ConfigUtil.QUEUE_SERVER_URL))
                            .append(":")
                            .append(Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.QUEUE_SERVER_PORT)))
                            .append(")?jms.useAsyncSend=true")
                            .append("&jms.sendTimeout=").append(20000)
                            .toString());
        } catch (Exception ex) {
            logger.error("error occured initializing factory!", ex);
        }
    }

    public Connection getConnection() throws JMSException {
        return factory.createConnection(
                ConfigUtil.getProperty(ConfigUtil.QUEUE_SERVER_USERNAME),
                ConfigUtil.getProperty(ConfigUtil.QUEUE_SERVER_PASSWORD)
        );
    }

    public void sendMessage(MessageWrapperVO messageWrapperVO, MessageProducer producer, Session session) throws JMSException, UnsupportedEncodingException {
        String json = JsonUtil.getJson(messageWrapperVO);
        byte[] bytes = json.getBytes("utf-8");
        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(bytes);
        logger.info("write len: " + bytes.length + " " + json);
        try {
            producer.send(bytesMessage);
        } catch (Exception e) {
            throw e;
        }
        logger.info("send len: " + bytes.length + " " + json);
    }


    public static class AsyncMessageType {
        public static final byte PING = 0;
        public static final byte SERVER_REGISTER = 1;
        public static final byte DEVICE_REGISTER = 2;
        public static final byte MESSAGE = 3;
        public static final byte MESSAGE_ACK_NEEDED = 4;
        public static final byte MESSAGE_SENDER_ACK_NEEDED = 5;
        public static final byte ACK = 6;
        public static final byte SEND_MESSAGE_FAILD = 7;
        public static final byte PEER_REMOVED = (byte) -3;
        public static final byte REGISTER_QUEUE = (byte) -2;
        public static final byte NOT_REGISTERED = (byte) -1;

        public static boolean isAckNeeded(byte msgType) {
            return msgType == MESSAGE_ACK_NEEDED || msgType == MESSAGE_SENDER_ACK_NEEDED || msgType == PEER_REMOVED;
        }
    }

    public enum MessageType {
        // Push messages
        PROCESS_STATUS(DeliveryType.INVOLVED_USERS),
        ERROR,
        GET_INPUT,
        SHOW_OUTPUT,
        END_PROCESS(DeliveryType.INVOLVED_USERS),
        SEND_AVAILABLE_PROCESS,
        TEXT_MESSAGE(DeliveryType.REQUESTER),
        SEND_CARTABLE_ITEMS,
        SEND_TASKLIST,  // TODO: replacement for SEND_CARTABLE_ITEMS
        LOGIN_RESPONSE(DeliveryType.INVOLVED_USERS),
        WAIT_TO_PROCESS_CALL,
        CANCEL_GET_INPUT,//Soleimani: to cancel the get input process
        CHANGE_PASSWORD_RESPONSE,
        PROCESS_TERMINATED,//Soleimani: to terminate process
        SEND_COMMENTS,
        BOUNDARY_EVENT_CAUGHT,//Soleimani
        REQUEST_SUCCESSFUL,
        SEARCH_CALL,//Maghsoud

        // Messages from UI
        BACK_PROCESS(true),
        TIMEOUT_PROCESS(true),    // from IOManager
        SEND_INPUT(true),
        NEXT_INPUT(true),
        INIT_PROCESS,
        INIT_SUB_PROCESS,
        GET_AVAILABLE_PROCESS,  // previous: GET_DEPLOYED_PROCESS
        SEND_DEPLOYED_PROCESS,
        GET_DEPLOYED_PROCESS,
        CONTINUE_PROCESS(true),
        GET_CARTABLE_ITEMS,
        LOGIN_REQUEST,
        LOGOUT_REQUEST,
        CHANGE_PASSWORD_REQUEST,
        ABORT_PROCESS,//Soleimani: to abort the process
        RESTART_PROCESS(true),//Soleimani: to restart the process
        POST_COMMENT,
        GET_COMMENTS,
        GET_TASKLIST,   // TODO: replacement for GET_CARTABLE_ITEMS

        // From ActionType
        SHOW_INFO, SHOW_ERROR, CATCH_EVENT, INTERRUPTED_BY_SUBPROCESS,INTERRUPTED_BY_BOUNDARY_EVENT,

        // From other servers
        Notification_Server,
        // Loader request messages from UI
        SQL_LOAD,
        GET_UI_SCHEMA,
        GET_UI_LANG_BUNDLE,
        GET_UI_LANG_BUNDLE_VERSION,
        UPDATE_UI_LANG_BUNDLE_VERSION,
        GET_USER_CONFIG,
        SET_USER_CONFIG,
        EXECUTE_REMOTE_SERVICE,
        ALL_LOCALES,
        TOKEN_RESPONSE,
        // Loader response messages
        SQL_RESULT,
        UI_SCHEMA_RESULT,
        UI_LANG_BUNDLE_RESULT,
        UI_LANG_BUNDLE_VERSION,
        USER_CONFIG_RESULT,
        EXECUTE_REMOTE_SERVICE_RESULT,
        TOKEN_INFO,
        TELEGRAM_MESSAGE,
        TOKEN_REQUEST,

        // SSO messages
        // - For Tokenizer
//    TOKEN_REQUEST,
//    TOKEN_RESPONSE,   // requester and client
        OAUTH_ACCESS_CONTROL,
        OAUTH_GRANT_ACCESS,
        OAUTH_DENY_ACCESS,

        LOGIN_DELEGATE,
        OAUTH_ACCESS_DELEGATE,
        OAUTH_REDIRECT,

        // Profile Manager
        SAVE_VIEW_PROFILE,
        DELETE_VIEW,
        GET_VIEW_BY_USERNAME,
        GET_VIEW_BY_ID,
        GET_VIEW_BY_DEFAULT,
        VIEW_RESULT,

        // user Profile
        SAVE_USER_PROFILE,
        DELETE_USER_PROFILE,
        GET_USER_PROFILE_All,
        GET_USER_PROFILE_BY_USERNAME,
        GET_USER_PROFILE_BY_PERSONNELCODE,
        GET_USER_PROFILE_BY_ID,
        USER_PROFILE_RESULT;

        public enum DeliveryType {
            REQUESTER,  // only deliver to the peer of requester
            PEERS_OF_REQUESTER, // all peers of user
            INVOLVED_USERS, //  all peers of involved users in Process
            EVERYBODY,   // all peers of online users
        }


        private boolean haveToken = false;

        private DeliveryType deliveryType;

        MessageType(DeliveryType deliveryType) {
            this.deliveryType = deliveryType;
        }

        MessageType() {
            this.deliveryType = DeliveryType.REQUESTER;
        }

        MessageType(boolean haveToken) {
            this();
            this.haveToken = haveToken;
        }

        MessageType(boolean haveToken, DeliveryType deliveryType) {
            this.haveToken = haveToken;
            this.deliveryType = deliveryType;
        }

        public DeliveryType getDeliveryType() {
            return this.deliveryType;
        }

        public void setDeliveryType(DeliveryType deliveryType) {
            this.deliveryType = deliveryType;
        }

        public boolean getHaveToken() {
            return haveToken;
        }

        public void setHaveToken(boolean haveToken) {
            this.haveToken = haveToken;
        }
    }


}
