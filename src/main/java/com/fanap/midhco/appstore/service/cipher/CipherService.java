package com.fanap.midhco.appstore.service.cipher;

import com.fanap.midhco.appstore.encoders.Hex;
import com.fanap.midhco.appstore.service.myException.appBundle.AppBundleNotSignedException;
import com.fanap.midhco.appstore.service.myException.appBundle.AppBundleSignNotValidException;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import sun.security.x509.X509CertImpl;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by IntelliJ IDEA.
 * User: Hamid Reza Khanmirza
 * Date: 2/16/13
 * Time: 12:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class CipherService {

    public static String encryptWithPEMPublic(String toBeEncryptedString, InputStream ins) throws Exception {
        byte[] encKey = new byte[ins.available()];
        ins.read(encKey);
        ins.close();

        String encodedMsg = new String(encKey);
        encKey = Base64.decode(encodedMsg);

        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(encKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(pubSpec);

        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        CipherOutputStream cout = new CipherOutputStream(bout, rsaCipher);
        cout.write(toBeEncryptedString.getBytes());
        cout.close();

        return Base64.encode(bout.toByteArray());
    }

    public static String encryptWithPublic(String toBeEncryptedString, InputStream ins) throws Exception {
        byte[] encKey = new byte[ins.available()];
        ins.read(encKey);
        ins.close();

        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(encKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(pubSpec);

        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        CipherOutputStream cout = new CipherOutputStream(bout, rsaCipher);
        cout.write(toBeEncryptedString.getBytes());
        cout.close();

        return Base64.encode(bout.toByteArray());
    }

    public static String encryptWithPublic(String toBeEncryptedString, String keyFileName) throws Exception {
        FileInputStream ins = new FileInputStream(keyFileName);
        return encryptWithPublic(toBeEncryptedString, ins);
    }

    public static String encryptWithPrivate(String toBeEncryptedString, String keyFileName) throws Exception {
        FileInputStream ins = new FileInputStream(keyFileName);
        byte[] encKey = new byte[ins.available()];
        ins.read(encKey);
        ins.close();

        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(encKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privKey = keyFactory.generatePrivate(privSpec);

        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.ENCRYPT_MODE, privKey);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        CipherOutputStream cout = new CipherOutputStream(bout, rsaCipher);
        cout.write(toBeEncryptedString.getBytes());
        cout.close();

        return Base64.encode(bout.toByteArray());
    }

    public static String decrypt(String toBeDecryptedString, String keyFileName) throws Exception {
        FileInputStream ins = new FileInputStream(keyFileName);
        byte[] encKey = new byte[ins.available()];
        ins.read(encKey);
        ins.close();

        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(encKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privKey = keyFactory.generatePrivate(privSpec);

        byte[] encryptedBytesWithPub = Base64.decode(toBeDecryptedString);
        Cipher rsaCipher = Cipher.getInstance("rsa");
        rsaCipher.init(Cipher.DECRYPT_MODE, privKey);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        CipherOutputStream cout = new CipherOutputStream(bout, rsaCipher);
        cout.write(encryptedBytesWithPub);
        cout.close();

        return bout.toString();
    }

    public static String sign(String message, String privateKeyFilePath) throws Exception {
        FileInputStream in = new FileInputStream(CipherService.class.getResource("/webServiceKeys/private.key").getPath());
        byte[] encKey = new byte[in.available()];
        in.read(encKey);
        in.close();

        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(encKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privKey = keyFactory.generatePrivate(privSpec);

        Signature sig2 = Signature.getInstance("SHA1withRSA");
        sig2.initSign(privKey);
        sig2.update(message.getBytes());
        byte[] signed = sig2.sign();
        return Base64.encode(signed);
    }

    public static boolean verify(String message, String sign, byte[] publicKey) throws Exception {
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(pubSpec);

        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(pubKey);
        sig.update(message.getBytes());

        byte[] signedBytes = Base64.decode(sign);
        return sig.verify(signedBytes);
    }

    private static byte[] append(byte[] prefix, byte[] suffix){
        byte[] toReturn = new byte[prefix.length + suffix.length];
        for (int i=0; i< prefix.length; i++){
            toReturn[i] = prefix[i];
        }
        for (int i=0; i< suffix.length; i++){
            toReturn[i+prefix.length] = suffix[i];
        }
        return toReturn;
    }

    private static byte[] blockCipher(byte[] bytes, int mode, Cipher cipher) throws Exception {
        // string initialize 2 buffers.
        // scrambled will hold intermediate results
        byte[] scrambled = new byte[0];

        // toReturn will hold the total result
        byte[] toReturn = new byte[0];
        // if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
        int length = (mode == Cipher.ENCRYPT_MODE)? 100 : 128;

        // another buffer. this one will hold the bytes that have to be modified in this step
        byte[] buffer = new byte[length];

        for (int i=0; i< bytes.length; i++){

            // if we filled our buffer array we have our block ready for de- or encryption
            if ((i > 0) && (i % length == 0)){
                //execute the operation
                scrambled = cipher.doFinal(buffer);
                // add the result to our total result.
                toReturn = append(toReturn,scrambled);
                // here we calculate the length of the next buffer required
                int newlength = length;

                // if newlength would be longer than remaining bytes in the bytes array we shorten it.
                if (i + length > bytes.length) {
                    newlength = bytes.length - i;
                }
                // clean the buffer array
                buffer = new byte[newlength];
            }
            // copy byte into our buffer.
            buffer[i%length] = bytes[i];
        }

        // this step is needed if we had a trailing buffer. should only happen when encrypting.
        // example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes. they are in the buffer array
        scrambled = cipher.doFinal(buffer);

        // final step before we can return the modified data.
        toReturn = append(toReturn,scrambled);

        return toReturn;
    }

    public static void main(String[] args) throws Exception {
        InputStream is = null;

        byte[] signature = getSignature("D:/app-release.apk");;

        ZipFile zip = new ZipFile("D:/app-release.apk");

        try {
            ZipEntry certSFFile = zip.getEntry("META-INF/MANIFEST.MF");

            is = zip.getInputStream(certSFFile);
            byte[] buf = new byte[is.available()];
            int bytesRead = is.read(buf);
            is.close();

            String certFileContent = new String(buf);
            StringReader stringReader;
            stringReader = new StringReader(certFileContent);
            BufferedReader bufReader = new BufferedReader(new StringReader(certFileContent));
            String data = bufReader.readLine();

            while (data != null) {
                if (data.startsWith("SHA1-Digest-Manifest")) {
                    String[] dataSplitted = data.split(":");
                    String digestSignature = dataSplitted[1].trim();

                    ZipEntry manifestEntry = zip.getEntry("META-INF/MANIFEST.MF");

                    is = zip.getInputStream(manifestEntry);
                    byte[] manifestByteArray = new byte[is.available()];
                    is.read(manifestByteArray);
                    is.close();

                    MessageDigest digest = MessageDigest.getInstance("SHA1");
                    digest.update(manifestByteArray);
                    byte[] hashtext = digest.digest();
                    Hex.bytesToHex(hashtext);
                }

                data = bufReader.readLine();
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (zip != null) {
                zip.close();
            }
        }

//        FileInputStream fis = new FileInputStream("D:/temp.jks");
//        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//        Collection c = cf.generateCertificates(fis);
//        Iterator i = c.iterator();
//        while (i.hasNext()) {
//            X509CertImpl cert = (X509CertImpl)i.next();
//            System.out.println(Base64.encode(cert.getSignature()));
//        }

//        KeyStore ks = KeyStore.getInstance("JKS");
//        ks.load(new FileInputStream("D:/temp.jks"), "123456".toCharArray());
//        java.security.cert.Certificate[] cchain = ks.getCertificateChain("temp");
//        List mylist = new ArrayList();
//        for (int i = 0; i < cchain.length; i++) {
//            mylist.add(cchain[i]);
//        }
//        Base64.encode(((X509CertImpl)mylist.get(0)).getSubjectKeyId().getIdentifier());
//
//        System.out.println("");
    }

    public static byte[] getSignature(String apkFileName) throws Exception {
        InputStream is;

        ZipFile zip = new ZipFile(apkFileName);

        try {
            ZipEntry certRSAFile = zip.getEntry("META-INF/CERT.RSA");
            if(certRSAFile == null) {
                throw new AppBundleNotSignedException("META-INF/CERT.RSA file not found!");
            }

            is = zip.getInputStream(certRSAFile);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection c = cf.generateCertificates(is);

            if(c.size() > 1)
                throw new AppBundleSignNotValidException("more than one certificate found in app sign file");

            Iterator i = c.iterator();
            if (i.hasNext()) {
                X509CertImpl cert = (X509CertImpl)i.next();
                return cert.getSignature();
            } else {
                throw new AppBundleSignNotValidException("no certificate found in app sign file!");
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if(zip != null)
                zip.close();
        }
    }
}
