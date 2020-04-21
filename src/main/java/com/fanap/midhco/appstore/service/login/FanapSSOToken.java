package com.fanap.midhco.appstore.service.login;

import org.json.*;

import java.io.Serializable;

/**
 * Created by admin123 on 2/15/2017.
 */
public class FanapSSOToken implements Serializable {
    String access_token;
    String id_token;
    String refresh_token;
    String scope;
    String bearer;
    String username;
    String userId;

    public FanapSSOToken() {}

    public FanapSSOToken(String tokenString) {
        JSONObject ssoJsonObject = new JSONObject(tokenString);
        access_token = ssoJsonObject.getString("access_token");
        id_token = ssoJsonObject.getString("id_token");
        refresh_token = ssoJsonObject.getString("refresh_token");
        scope = ssoJsonObject.getString("scope");
        bearer = ssoJsonObject.getString("token_type");
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getId_token() {
        return id_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public String getScope() {
        return scope;
    }

    public String getBearer() {
        return bearer;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public void setId_token(String id_token) {
        this.id_token = id_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setBearer(String bearer) {
        this.bearer = bearer;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
