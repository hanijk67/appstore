package com.fanap.midhco.appstore.service.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by Heidari on 1/23/17.
 */
public class JWTService {
    private static JWTVerifier jwtVerifier;
    private static PublicKey publicKey;
    public static final String JWT_ISSUER = ConfigUtil.getProperty(ConfigUtil.JWT_ISSUER);

    public static class JWTUserClass {
        String firstName;
        String lastName;
        String userName;
        String userId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

    public static JWTUserClass extractUserInfo(String jwtToken) {
        JWT jwt = JWT.decode(jwtToken);

        Claim firstNameClaim = jwt.getClaim("given_name");
        Claim familyNameClaim = jwt.getClaim("family_name");
        Claim userNameClaim = jwt.getClaim("preferred_username");
        String userId = jwt.getSubject();

        JWTUserClass jwtUserClass = new JWTUserClass();

        if(firstNameClaim != null)
            jwtUserClass.setFirstName(firstNameClaim.asString());

        if(familyNameClaim != null)
            jwtUserClass.setLastName(familyNameClaim.asString());

        if(userNameClaim != null)
            jwtUserClass.setUserName(userNameClaim.asString());

        jwtUserClass.setUserId(userId);

        return jwtUserClass;
    }

    public static Long validateAndGetUser(String jwtToken) throws JWTVerificationException {
        try {
            JWT jwt = (JWT) getJwtVerifier().verify(jwtToken);
            return Long.valueOf(jwt.getSubject());
        } catch (IOException | NumberFormatException e) {
            throw new JWTVerificationException("Error JWT validation", e);
        }
    }

    private static JWTVerifier getJwtVerifier() throws IOException {
        if(jwtVerifier == null) {
            jwtVerifier = JWT.require(
                    Algorithm.RSA256((RSAKey) getPublicKey()))
                    .withIssuer(JWT_ISSUER)
                    .acceptLeeway(15*60)
                    .build();
        }
        return jwtVerifier;
    }

    public static PublicKey getPublicKey() throws IOException {
        if(publicKey == null) {
            InputStream inputStream = new FileInputStream(new File(ConfigUtil.getProperty(ConfigUtil.KEYBOD_DER_FILE_LOCATION)));
            byte[] keyBytes = AppUtils.getBytesFromInputStream(inputStream);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            try {
                KeyFactory kf = KeyFactory.getInstance("RSA");
                publicKey = kf.generatePublic(spec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new IOException(e);
            }
        }
        return publicKey;
    }

    public static void main(String[] args) throws IOException {
        JWTService.getJwtVerifier().verify("eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIzMjgxIiwiaXNzIjoiaHR0cHM6Ly93d3cuZmFuYXBpdW0uY29tLyIsInByZWZlcnJlZF91c2VybmFtZSI6InBmX3JlcSIsImV4cCI6MTUwNDEwMDU4NCwiZ2l2ZW5fbmFtZSI6IiIsImlhdCI6MTUwMTUwODU4NCwiZmFtaWx5X25hbWUiOiIifQ.SLkmiDA5KKxzwX4WrZ5r_XbyS4qyxMrNve6ep1Shw_TZY77V-3gB6r0lOjQFfUnNdJaSJzEyYyEKZAzmsWYVi6oPQSJu6jampMmue72yklqt9K_yk7wZLuJu1_ClwmY2Pn2D10Fs_Bt-__Q-8owJtVmvrvWvvk1FQl1B89POXUw");
        System.out.println("");
    }
}
