package com.fanap.midhco.appstore.service.login;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.impl.dv.util.Base64;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by admin123 on 1/22/2017.
 */
public class LoginService {
    final static Logger logger = LogManager.getLogger();
    public static final LoginService Instance = new LoginService();

    private LoginService() {}


    public String getFanapSSORedirectURL(String oAuthRedirect_url, String stateParameter) {
        String oAuthClientId = ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_ID);

        String authenticationUrl = SSOUserService.AUTHENTICATIONURL;

        String endPointURL = authenticationUrl + "/?client_id=" + oAuthClientId + "&response_type=code&redirect_uri=" + oAuthRedirect_url;
        endPointURL = endPointURL + "&state=" + stateParameter;

        return endPointURL;
    }
//
    public FanapSSOToken getSSOToken(String code) throws Exception {
        String oAuth_Client_id = ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_ID);
        String oAuth_Client_secret = ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_SECRET);
        String acccessTokenURL = SSOUserService.ACCESSTOKENURL;

        if (code != null) {
            try {
                HttpClient client = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost(acccessTokenURL);

                List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                urlParameters.add(new BasicNameValuePair("client_id", oAuth_Client_id));
                urlParameters.add(new BasicNameValuePair("client_secret", oAuth_Client_secret));
                urlParameters.add(new BasicNameValuePair("code", code));
                urlParameters.add(new BasicNameValuePair("redirect_uri", ConfigUtil.getProperty(ConfigUtil.OAUTH_PORTAL_LOGIN_REDIRECT_URL)));
                urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));

                post.setEntity(new UrlEncodedFormEntity(urlParameters));
                HttpResponse response = client.execute(post);

                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                StringBuffer tokenResponseString = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    tokenResponseString.append(line);
                }

                return new FanapSSOToken(tokenResponseString.toString());
            } catch (Exception ex) {
                logger.error("Exception occured  getting token! ", ex);
                throw ex;
            }
        }
        return null;
    }


    public static void main(String[] args) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);

        CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null);

        X500Name x500Name = new X500Name("Ha", "Ha", "Ha", "Ha", "Ha", "Ha");

        keypair.generate(2048);
        PrivateKey privKey = keypair.getPrivateKey();

        X509Certificate[] chain = new X509Certificate[1];

        chain[0] = keypair.getSelfCertificate(x500Name, new Date(), (long) 2000 * 24 * 60 * 60);

        keyStore.setKeyEntry("pluginCertificate", privKey, "123456".toCharArray(), chain);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        keyStore.store(bout, "123456".toCharArray());

        String encoded= Base64.encode(bout.toByteArray());

        System.out.println("");

        KeyStore keyStoreLoadedFrom = KeyStore.getInstance("JKS");
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());

        keyStoreLoadedFrom.load(bin, null);
        keyStoreLoadedFrom.getCertificate("123456");

        System.out.println("");

    }
//
//    public void revokeToken(String refreshToken) throws Exception {
//        String client_id = ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_ID);
//        String client_secret = ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_SECRET);
//        String revokeEndPointURL = ConfigUtil.getProperty(ConfigUtil.OAUTH_PORTAL_REVOKE_ENDPOINT);
//
//        try {
//            //?token_type_hint=refresh_token&token=${REFRESH_TOKEN}&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}
//            HttpClient client = HttpClientBuilder.create().build();
//            HttpPost post = new HttpPost(revokeEndPointURL);
//
//            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
//            urlParameters.add(new BasicNameValuePair("client_id", client_id));
//            urlParameters.add(new BasicNameValuePair("client_secret", client_secret));
//            urlParameters.add(new BasicNameValuePair("token_type_hint", "refresh_token"));
//            urlParameters.add(new BasicNameValuePair("token", refreshToken));
//
//            post.setEntity(new UrlEncodedFormEntity(urlParameters));
//            HttpResponse response = client.execute(post);
//
//            BufferedReader rd = new BufferedReader(
//                    new InputStreamReader(response.getEntity().getContent()));
//
//            StringBuffer tokenResponseString = new StringBuffer();
//            String line = "";
//            while ((line = rd.readLine()) != null) {
//                tokenResponseString.append(line);
//            }
//
//        } catch (Exception ex) {
//            logger.error("Exception occured  getting token! ", ex);
//            throw ex;
//        }
//
//    }
//
//    public static void main(String[] args) throws Exception {
////        JWTService.validateAndGetUser(
////                "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIzOSIsImlzcyI6Imh0dHBzOi8vd3d3LmtleWJvZC5jb20vIiwicHJlZmVycmVkX3VzZXJuYW1lIjoia2hhbjEiLCJleHAiOjE0ODUxNzkxMDIsImdpdmVuX25hbWUiOiJLaGFuIiwiaWF0IjoxNDg1MTc1NTAyLCJmYW1pbHlfbmFtZSI6IlRlc3QifQ.WzLxZYTDygQche8-og-QsfjXqUC2WdtHfnCluupn6Fm4gUDpml0Xbk4AHYxJ16Di7eNMfTwxdHD2_AlY2aNF-v1p38Jik4rAK2WyvZ5EqYBhi7K_5Map-BA9_aBVFXSJCtzvrEpu3hX3k2Lc93rWyDa7i0GtyfuJOFuN2T-elZU");
////        HttpClient client = HttpClientBuilder.create().build();
////        HttpPost post = new HttpPost("https://api.keybod.com:14005/oauth2/token/info");
////
////        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
////        urlParameters.add(new BasicNameValuePair("token_type_hint", "refresh_token"));
////        urlParameters.add(new BasicNameValuePair("client_secret", ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_SECRET)));
////        urlParameters.add(new BasicNameValuePair("client_id", ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_ID)));
////        urlParameters.add(new BasicNameValuePair("token", "4e0164b793674694bb8510410abb4f51"));
////
////        post.setEntity(new UrlEncodedFormEntity(urlParameters));
////        HttpResponse response = client.execute(post);
////
////        BufferedReader rd = new BufferedReader(
////                new InputStreamReader(response.getEntity().getContent()));
////
////        StringBuffer tokenResponseString = new StringBuffer();
////        String line = "";
////        while ((line = rd.readLine()) != null) {
////            tokenResponseString.append(line);
////        }
////
////        System.out.println("3333");
//    }
}
