package com.fanap.midhco.appstore.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

/**
 * Created by admin123 on 7/17/2016.
 */
@Embeddable
public class PublicPrivatePair {
    @Column(name = "PPUBLICKEY")
    @Lob
    String publicKey;

    @Column(name = "PPRIVKEY")
    @Lob
    String privateKey;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
