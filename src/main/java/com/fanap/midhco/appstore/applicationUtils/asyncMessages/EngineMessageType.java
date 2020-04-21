package com.fanap.midhco.appstore.applicationUtils.asyncMessages;

/**
 * Created by saeedy on 4/6/2015.
 */
public enum EngineMessageType {
    UNKNOWN,
    PROCESS_STATUS,
    ERROR,
    GET_INPUT,
    SHOW_OUTPUT,
    END_PROCESS,
    SEND_AVAILABLE_PROCESS,
    TEXT_MESSAGE,
    SEND_CARTABLE_ITEMS,
    SEND_TASKLIST,
    LOGIN_RESPONSE,
    WAIT_TO_PROCESS_CALL,
    CANCEL_GET_INPUT,
    CHANGE_PASSWORD_RESPONSE,
    PROCESS_TERMINATED,
    SEND_COMMENTS,
    BOUNDARY_EVENT_CAUGHT,
    REQUEST_SUCCESSFUL,
    SEARCH_CALL,
    BACK_PROCESS,
    TIMEOUT_PROCESS,
    SEND_INPUT,
    NEXT_INPUT,
    INIT_PROCESS,
    INIT_SUB_PROCESS,
    GET_AVAILABLE_PROCESS,
    SEND_DEPLOYED_PROCESS,
    GET_DEPLOYED_PROCESS,
    CONTINUE_PROCESS,
    GET_CARTABLE_ITEMS,
    LOGIN_REQUEST,
    LOGOUT_REQUEST,
    CHANGE_PASSWORD_REQUEST,
    ABORT_PROCESS,
    RESTART_PROCESS,
    POST_COMMENT,
    GET_COMMENTS,
    GET_TASKLIST,
    USER_POSTS,
    SHOW_INFO,
    SHOW_ERROR,
    CATCH_EVENT,
    INTERRUPTED_BY_SUBPROCESS,
    INTERRUPTED_BY_BOUNDARY_EVENT,
    CHAT_MESSAGE,
    TELEGRAM_MESSAGE,
    EMAIL_MESSAGE,
    REGISTER_EMAIL_REQUEST,
    GET_EMAIL,
    WATCHED_EMAIL,
    FETCH_EMAIL_REQUEST,
    FETCH_EMAIL_RESPONSE,
    REGISTER_EMAIL_RESPONSE,
    SQL_LOAD,
    GET_UI_SCHEMA,
    GET_UI_LANG_BUNDLE,
    GET_UI_LANG_BUNDLE_VERSION,
    UPDATE_UI_LANG_BUNDLE_VERSION,
    GET_USER_CONFIG,
    SET_USER_CONFIG,
    EXECUTE_REMOTE_SERVICE,
    ALL_LOCALES,
    SQL_RESULT,
    UI_SCHEMA_RESULT,
    UI_LANG_BUNDLE_RESULT,
    UI_LANG_BUNDLE_VERSION,
    USER_CONFIG_RESULT,
    EXECUTE_REMOTE_SERVICE_RESULT,
    SAVE_VIEW_PROFILE,
    DELETE_VIEW,
    SAVE_CATEGORY_VIEW_PROFILE,
    DELETE_CATEGORY_VIEW,
    GET_VIEW_SEARCHABLEFIELD,
    SEND_VIEWS,
    VIEW_INSERT_RESULT,
    VIEW_DELETE_RESULT,
    CAREGORY_VIEW_INSERT_RESULT,
    CAREGORY_VIEW_DELETE_RESULT,
    VIEW_SEARCHABLEFIELD_RESULT,
    GET_VIEWS,
    GET_CAREGORY_VIEWS,
    SAVE_FORWARD_DATA,
    DELETE_FORWARD_DATA,
    GET_FORWARD_DATA,
    FORWARD_SAVE_RESULT,
    FORWARD_DELETE_RESULT,
    SEND_FORWARD_DATA,
    FORWARD_ACCEPT,
    FORWARD_PROCESS,
    SAVE_REPLACEMENT_PROCESS_DATA,
    DELETE_REPLACEMENT_PROCESS_DATA,
    GET_REPLACEMENT_PROCESS_DATA,
    REPLACEMENT_PROCESS_SAVE_RESULT,
    REPLACEMENT_PROCESS_DELETE_RESULT,
    SEND_REPLACEMENT_PROCESS_DATA,
    REPLACEMENT_PROCESS_ACCEPT,
    REPLACEMENT_PROCESS,
    SAVE_USER_PROFILE,
    DELETE_USER_PROFILE,
    GET_USER_PROFILE_All,
    GET_USER_PROFILE_BY_USERNAME,
    GET_USER_PROFILE_BY_PERSONNELCODE,
    GET_USER_PROFILE_BY_ID,
    USER_PROFILE_RESULT,
    REQUEST_USER_LIST,
    SEND_USER_LIST,
    GET_PROCESS_LIST_NAME,
    GET_PROCESS_HISTORY,
    SEND_PROCESS_HISTORY,
    SEND_PROCESS_LIST_NAME,
    GET_OU_LIST,
    SEND_OU_LIST,
    GET_SUBPROCESSES;

    //Message Event

    public enum DeliveryType {
        REQUESTER,  // only deliver to the peer of requester
        PEERS_OF_REQUESTER, // all peers of user
        INVOLVED_USERS, //  all peers of involved users in Process
        EVERYBODY,   // all peers of online users
    }


    private boolean haveToken = false;

    private DeliveryType deliveryType;

    EngineMessageType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    EngineMessageType() {
        this.deliveryType = DeliveryType.REQUESTER;
    }

    EngineMessageType(boolean haveToken) {
        this();
        this.haveToken = haveToken;
    }

    EngineMessageType(boolean haveToken, DeliveryType deliveryType) {
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