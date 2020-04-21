package com.fanap.midhco.appstore.restControllers.vos;

/**
 * Created by admin123 on 7/5/2016.
 */
public class ProductDetailInfoVO {

    private Integer id;
    private String id_product;
    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getId_product() {
        return id_product;
    }

    public void setId_product(String id_product) {
        this.id_product = id_product;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}