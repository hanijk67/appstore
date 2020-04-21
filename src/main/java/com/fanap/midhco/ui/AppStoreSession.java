package com.fanap.midhco.ui;

import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.service.login.FanapSSOToken;
import com.fanap.midhco.appstore.service.login.JWTService;
import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

/**
 * Created by admin123 on 6/15/2016.
 */
public class AppStoreSession extends WebSession {
    static final Logger logger = Logger.getLogger(AppStoreSession.class);

    User user;
    boolean isAuthenticated;
    boolean isUserDeveloper;
    FanapSSOToken ssotoken;
    boolean isRootUser;

    public AppStoreSession(Request request) {
        super(request);
    }

    @Override
    public void onInvalidate() {
        super.onInvalidate();
        this.setAuthenticated(false);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isAuthenticated() {
        if (ssotoken != null) {
            try {
                String id_token = ssotoken.getId_token();
                JWTService.validateAndGetUser(id_token);
            } catch (Exception e) {
                logger.error("error authentication server ", e);
                removeAttribute("id_token");
                invalidate();
                return false;
            }
            return true;
        }

        return false;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public boolean isUserDeveloper() {
        return isUserDeveloper;
    }

    public void setUserDeveloper(boolean userDeveloper) {
        isUserDeveloper = userDeveloper;
    }

    public FanapSSOToken getSsotoken() {
        return ssotoken;
    }

    public void setSsotoken(FanapSSOToken ssotoken) {
        this.ssotoken = ssotoken;
    }

    public boolean isRootUser() {
        return isRootUser;
    }

    public void setRootUser(boolean rootUser) {
        isRootUser = rootUser;
    }
}
