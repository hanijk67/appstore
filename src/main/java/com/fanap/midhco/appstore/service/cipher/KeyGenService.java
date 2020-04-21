package com.fanap.midhco.appstore.service.cipher;

import java.security.*;

/**
 * Created by admin123 on 7/17/2016.
 */
public class KeyGenService {

    public static class PUB_PRIV_Pair {
        public byte[] publicKey;
        public byte[] privateKey;

        public PUB_PRIV_Pair(byte[] publicKey, byte[] privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public byte[] getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(byte[] publicKey) {
            this.publicKey = publicKey;
        }

        public byte[] getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(byte[] privateKey) {
            this.privateKey = privateKey;
        }
    }

    public static PUB_PRIV_Pair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();

        return new PUB_PRIV_Pair(pub.getEncoded(), priv.getEncoded());

//        byte[] pubKeyEncoded = pub.getEncoded();
//        FileOutputStream fout = new FileOutputStream("d:/webKeys/public.key");
//        fout.write(pubKeyEncoded);
//        fout.close();
//
//        byte[] privKeyEncoded = priv.getEncoded();
//        fout = new FileOutputStream("d:/webKeys/private.key");
//        fout.write(privKeyEncoded);
//        fout.close();
    }

    public static void main(String[] args) throws Exception {
        generateKeyPair();
    }
}
