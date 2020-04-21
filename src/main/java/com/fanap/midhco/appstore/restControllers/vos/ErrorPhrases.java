package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;

/**
 * Created by admin123 on 7/5/2016.
 */
public class ErrorPhrases {
    public static ErrorPhrases GENERAL_ERROR = new ErrorPhrases(AppStorePropertyReader.getString("label.general.error"));
    public static ErrorPhrases NO_APP_WITH_GIVEN_PACKAGENAME_EXISTS = new ErrorPhrases(AppStorePropertyReader.getString("no.app.with.given.packageName.exists"));
    public static ErrorPhrases NO_APP_WITH_GIVEN_DATA_EXISTS = new ErrorPhrases(AppStorePropertyReader.getString("no.app.with.given.data.exists"));
    public static ErrorPhrases NO_CATEGORY_WITH_GIVEN_DATA_EXISTS = new ErrorPhrases(AppStorePropertyReader.getString("no.category.with.given.data.exists"));
    public static ErrorPhrases PACKAGENAME_IS_REQUIRED = new ErrorPhrases(AppStorePropertyReader.getString("packageName.is.required"));
    public static ErrorPhrases VERSIONCODE_IS_REQUIRED = new ErrorPhrases(AppStorePropertyReader.getString("versionCode.is.required"));
    public static ErrorPhrases NO_APP_FOUND = new ErrorPhrases(AppStorePropertyReader.getString("no.app.with.given.packageName.exists"));
    public static ErrorPhrases NO_OSTYPE_RECIEVED_OR_UNKNOWN_OSTYPE = new ErrorPhrases(AppStorePropertyReader.getString("no.ostype.recieved.or.unknown.ostype"));
    public static ErrorPhrases NO_JWT_TOKEN_USER_RECIEVED_OR_UNKNOWN_USER = new ErrorPhrases(AppStorePropertyReader.getString("no.jwtTokenUser.recieved.or.unknown.jwtTokenUser"));

    String message;

    public ErrorPhrases(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
