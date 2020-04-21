package com.fanap.midhco.appstore.applicationUtils;

import java.io.Serializable;

/**
 * Created by admin123 on 7/13/2016.
 */
public enum ImageFormat implements Serializable {
    PNG("PNG"),GIF("GIF"),JPG("JPG"),TIFF("TIFF"),UNKNOWN("");

    String value;

    ImageFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
