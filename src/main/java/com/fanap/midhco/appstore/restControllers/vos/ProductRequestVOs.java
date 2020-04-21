package com.fanap.midhco.appstore.restControllers.vos;

import org.json.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin123 on 7/20/2016.
 */
public class ProductRequestVOs implements Serializable {
    List<ProductRequestVO> productRequestVOList = new ArrayList<>();

    public ProductRequestVOs(String request) {
        JSONObject jsonObject = new JSONObject(request);
        if(jsonObject.has("productRequestVOList")) {
            JSONArray productRequestVOJSONArray = jsonObject.getJSONArray("productRequestVOList");
            for(int i = 0; i < productRequestVOJSONArray.length(); i++) {
                ProductRequestVO productRequestVO = new ProductRequestVO(productRequestVOJSONArray.getJSONObject(i).toString());
                productRequestVOList.add(productRequestVO);
            }
        }
    }

    public ProductRequestVOs() {}

    public List<ProductRequestVO> getProductRequestVOList() {
        return productRequestVOList;
    }

    public void setProductRequestVOList(List<ProductRequestVO> productRequestVOList) {
        this.productRequestVOList = productRequestVOList;
    }
}
