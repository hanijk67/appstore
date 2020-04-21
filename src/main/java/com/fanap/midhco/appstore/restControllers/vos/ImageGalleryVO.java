package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import org.codehaus.jackson.type.TypeReference;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by admin123 on 7/5/2016.
 */
public class ImageGalleryVO {

    private String product_packageName;
    private String imageUrl;
    private String thumbnailURL;


    public String getProduct_packageName() {
        return product_packageName;
    }

    public void setProduct_packageName(String product_packageName) {
        this.product_packageName = product_packageName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
}
