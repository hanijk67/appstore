package com.fanap.midhco.appstore.service.security;

import com.fanap.midhco.appstore.service.myException.EncryptionException;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class PasswordService {

    private static final int SALT_LENGTH = 8;
    private static final String ALLOWED_SALT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    public static String generateSalt() {
        final SecureRandom random = new SecureRandom();
        final StringBuilder salt = new StringBuilder();
        for (int i = 0; i < SALT_LENGTH; i++) {
            salt.append(ALLOWED_SALT_CHARS.charAt(random.nextInt(ALLOWED_SALT_CHARS.length())));
        }
        return salt.toString();
    }

    public static String encrypt(final String plaintext, final String salt) {
        if (plaintext == null) {
            throw new NullPointerException();
        }
        if (salt == null) {
            throw new NullPointerException();
        }

        try {
            final MessageDigest md = MessageDigest.getInstance("SHA");
            md.update((plaintext + salt).getBytes("UTF-8"));
            return new BASE64Encoder().encode(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException(e);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptionException(e);
        }
    }

    public static String encrypt(final String plaintext) {
        if (plaintext == null) {
            throw new NullPointerException();
        }

        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update((plaintext).getBytes("UTF-8"));
            return convertToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException(e);
        }
        catch (UnsupportedEncodingException e) {
            throw new EncryptionException(e);
        }
    }

    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('A' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }


}
