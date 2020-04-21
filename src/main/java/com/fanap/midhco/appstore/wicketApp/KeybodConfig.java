package com.fanap.midhco.appstore.wicketApp;

import com.fanap.midhco.appstore.service.login.SSOUserService;
import com.fanapium.keylead.client.KeyleadClient;
import com.fanapium.keylead.client.KeyleadClientFactory;
import com.fanapium.keylead.client.config.DefaultKeyleadConfig;

/**
 * Created by admin123 on 10/2/2017.
 */
public class KeybodConfig {

    public static void init() {
        KeyleadClient KeyleadClient = KeyleadClientFactory.getClient();
        KeyleadClient.getSettings().setProperty(DefaultKeyleadConfig.OAUTH_AUTHORIZATION_ENDPOINT, SSOUserService.AUTHENTICATIONURL);
        KeyleadClient.getSettings().setProperty(DefaultKeyleadConfig.OAUTH_TOKEN_EXCHANGE_ENDPOINT, SSOUserService.ACCESSTOKENURL);
        KeyleadClient.getSettings().setProperty(DefaultKeyleadConfig.OAUTH_TOKEN_REVOKE_ENDPOINT, SSOUserService.REVOKETOKENURL);
        KeyleadClient.getSettings().setProperty(DefaultKeyleadConfig.KEYLEAD_USER_ENDPOINT, SSOUserService.USER_ENDPOINT);
    }
}
