package com.fanap.midhco.appstore.service.login;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.service.myException.SSOServerException;
import org.apache.logging.log4j.LogManager;
import org.apache.xerces.impl.dv.util.Base64;
import org.hibernate.Transaction;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by A.Moshiri on 4/8/2017.
 */
public class SSOUserService {
    final static org.apache.logging.log4j.Logger logger = LogManager.getLogger();
    public static SSOUserService Instance = new SSOUserService();

    public static final String SSO_OAUTH_SERVER_URL = ConfigUtil.getProperty(ConfigUtil.SSO_OAUTH_SERVER_URL);
    public static final String SSO_URL_SEND_USER_NAME;
    public static final String SSO_URL_USER_LOGOUT;
    public static final String AUTHENTICATIONURL;
    public static final String ACCESSTOKENURL;
    public static final String REVOKETOKENURL;
    public static final String USER_ENDPOINT;

    static {
        SSO_URL_SEND_USER_NAME = SSO_OAUTH_SERVER_URL + "users/";
        SSO_URL_USER_LOGOUT = SSO_OAUTH_SERVER_URL + "oauth2/logout";
        AUTHENTICATIONURL = SSO_OAUTH_SERVER_URL + "oauth2/authorize";
        ACCESSTOKENURL = SSO_OAUTH_SERVER_URL + "oauth2/token";
        REVOKETOKENURL = SSO_OAUTH_SERVER_URL + "oauth2/token/revoke";
        USER_ENDPOINT = SSO_OAUTH_SERVER_URL + "users";
    }


    public SSOUserService() {
    }

    public Long getUserIdByUserName(String userName) throws Exception {
        boolean createSession = false;
        Transaction tx = null;
        Long id = Long.valueOf(-1);
        try {
            StringBuffer userCredentials = new StringBuffer(ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_ID));
            userCredentials.append(":");
            userCredentials.append(ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_SECRET));
            StringBuffer userUrl = new StringBuffer(SSO_URL_SEND_USER_NAME);
            userUrl.append(userName);
            userUrl.append("?identityType=username");
            URL url = new URL(userUrl.toString());
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            String userCredentialsStr = userCredentials.toString();
            String basicAuth = "Basic ".concat(org.apache.commons.codec.binary.Base64.encodeBase64String(userCredentialsStr.getBytes("UTF-8")));
            httpURLConnection.setRequestProperty("Authorization", basicAuth);
            httpURLConnection.setRequestMethod("GET");

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject responseObj = parseSSOResponseStream(httpURLConnection, true);
                id = Long.valueOf(responseObj.get("id").toString());
            } else {
                JSONObject responseObj = parseSSOResponseStream(httpURLConnection, false);
                String errDescription = responseObj.get("error").toString();
                throw new SSOServerException(errDescription, String.valueOf(responseCode));
            }
        } catch (SSOServerException ssoEx) {
            throw ssoEx;
        } catch (Exception ex) {
            throw ex;
        }
        return id;
    }

    private static JSONObject parseSSOResponseStream(HttpURLConnection httpURLConnection, boolean isOk) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader rd;
        if (isOk) { // for correct response
            rd = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        } else { // for wrong response
            rd = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
        }
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();

        return new JSONObject(result.toString());
    }

    public static void main(String[] args) throws Exception {
        try {

            StringBuffer userCredentials = new StringBuffer(ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_ID));
            userCredentials.append(":");
            userCredentials.append(ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_SECRET));
            StringBuffer userUrl = new StringBuffer(SSO_URL_SEND_USER_NAME);
            userUrl.append(ConfigUtil.getProperty(ConfigUtil.ADMIN_USER_NAME));
            userUrl.append("?identityType=username");
            URL url = new URL(userUrl.toString());
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            String userCredentialsStr = userCredentials.toString();
            String basicAuth = "Basic ".concat(new String(org.apache.commons.codec.binary.Base64.encodeBase64(userCredentialsStr.getBytes("UTF-8"))));
            System.out.println(basicAuth);
            System.out.println(new String(Base64.decode(basicAuth), "utf-8"));
            System.out.println(new String(org.apache.commons.codec.binary.Base64.decodeBase64(basicAuth), "utf-8"));


            String basicAuthNew1 = new String(org.apache.commons.codec.binary.Base64.encodeBase64(userCredentialsStr.getBytes("UTF-8")));

            System.out.println(basicAuthNew1);
            System.out.println(new String(Base64.decode(basicAuthNew1), "utf-8"));
            System.out.println(new String(org.apache.commons.codec.binary.Base64.decodeBase64(basicAuthNew1), "utf-8"));


            String basicAuthSafe = "Basic ".concat(org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(userCredentialsStr.getBytes("UTF-8")));
            System.out.println(basicAuthSafe);
            System.out.println(new String(Base64.decode(basicAuthSafe), "utf-8"));
            System.out.println(new String(org.apache.commons.codec.binary.Base64.decodeBase64(basicAuthSafe), "utf-8"));


            httpURLConnection.setRequestProperty("Authorization", basicAuth);
            httpURLConnection.setRequestMethod("GET");

            logger.error("ali moshiri" + basicAuth);
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject responseObj = parseSSOResponseStream(httpURLConnection, true);
                Long id = Long.valueOf(responseObj.get("id").toString());
            } else {
                JSONObject responseObj = parseSSOResponseStream(httpURLConnection, false);
                String errDescription = responseObj.get("error").toString();
                throw new SSOServerException(errDescription, String.valueOf(responseCode));
            }


            StringBuffer userCredentialsNew = new StringBuffer("alex");
            userCredentialsNew.append(":");
            userCredentialsNew.append("c670091d");

            StringBuffer userUrlNew = new StringBuffer("");
            userUrlNew.append("http://172.17.15.53:8080/users");
            userUrlNew.append("?identityType=username");
            URL urlNew = new URL(userUrlNew.toString());

            String userCredentialsStrNew = userCredentialsNew.toString();
            System.out.println(userCredentialsStrNew);

            String basicAuthNew = new String(org.apache.commons.codec.binary.Base64.encodeBase64(userCredentialsStrNew.getBytes("UTF-8")));

            System.out.println(basicAuthNew);
            System.out.println(new String(Base64.decode(basicAuthNew), "utf-8"));
            System.out.println(new String(org.apache.commons.codec.binary.Base64.decodeBase64(basicAuthNew), "utf-8"));


            StringBuffer userCredentialsNewByConfig = new StringBuffer(ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_SECRET));
            userCredentialsNewByConfig.append(":");
            userCredentialsNewByConfig.append(ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_ID));
            StringBuffer userUrlNewByConfig = new StringBuffer("");
            userUrlNewByConfig.append("http://172.17.15.53:8080/users");
            userUrlNewByConfig.append("?identityType=username");
            URL urlNewByConfig = new URL(userUrlNewByConfig.toString());

            String userCredentialsStrNewByConfig = userCredentialsNewByConfig.toString();
            System.out.println(userCredentialsStrNewByConfig);

            String basicAuthNewByConfig = new String(org.apache.commons.codec.binary.Base64.encodeBase64(userCredentialsStrNewByConfig.getBytes("UTF-8")));

            System.out.println(basicAuthNew);
            System.out.println(new String(Base64.decode(basicAuthNewByConfig), "utf-8"));
            System.out.println(new String(org.apache.commons.codec.binary.Base64.decodeBase64(basicAuthNewByConfig), "utf-8"));

        } catch (SSOServerException ssoEx) {
            throw ssoEx;
        } catch (Exception ex) {
            throw ex;
        }

        System.exit(0);

    }
}
