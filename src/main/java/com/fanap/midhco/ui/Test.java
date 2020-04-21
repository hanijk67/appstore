package com.fanap.midhco.ui;


import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import org.apache.commons.io.IOUtils;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Created by admin123 on 6/26/2016.
 */
public class Test {

    public static void main(String[] args) throws Exception {

        String fileDownloadURL = "http://172.16.110.127:8080/fileServer/download?key=${key}";
        String concreateDownLoadURL = fileDownloadURL.replace("${key}", "midhcoCert123456789" );
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try (InputStream input = new URL(concreateDownLoadURL).openStream()) {
            Certificate ca = cf.generateCertificate(input);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

        }
//
//
//        final int keysize = 1024;
//        final String commonName = "www.test.de";
//        final String organizationalUnit = "IT";
//        final String organization = "test";
//        final String city = "test";
//        final String state = "test";
//        final String country = "DE";
//        final long validity = 1096; // 3 years
//        final String alias = "tomcat";
//        final char[] keyPass = "changeit".toCharArray();
//
//        KeyStore keyStore = KeyStore.getInstance("JKS");
//        keyStore.load(null, null);
//
//        CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
//
//        X500Name x500Name = new X500Name(commonName, organizationalUnit, organization, city, state, country);
//
//        keypair.generate(keysize);
//        PrivateKey privKey = keypair.getPrivateKey();
//
//        X509Certificate[] chain = new X509Certificate[1];
//
//        chain[0] = keypair.getSelfCertificate(x500Name, new Date(), (long) validity * 24 * 60 * 60);
//
//        try {
//            System.out.println("-----BEGIN CERTIFICATE-----");
//            System.out.println(DatatypeConverter.printBase64Binary(chain[0].getEncoded()));
//            System.out.println("-----END CERTIFICATE-----");
//        } catch (CertificateEncodingException e) {
//            e.printStackTrace();
//        }
    }
}
