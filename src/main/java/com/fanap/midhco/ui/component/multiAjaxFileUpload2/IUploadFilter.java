package com.fanap.midhco.ui.component.multiAjaxFileUpload2;

import org.json.*;

import java.io.Serializable;
import java.util.List;

/**
 * Created by admin123 on 7/1/2016.
 */
public interface IUploadFilter extends Serializable {
    public String getFilterTitle();
    public List<String> getFilterList();

    public static String serializeAsJSONString(List<IUploadFilter> uploadFilters) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(IUploadFilter uploadFilter : uploadFilters) {
            JSONObject tempJsonObject = new JSONObject();

            List<String> filterList = uploadFilter.getFilterList();
            String filterValues = filterList.stream().reduce("", (p1, p2) -> {return (p1.trim().isEmpty() ? "" : (p1 + ",")) + p2;});

            tempJsonObject.put("title", uploadFilter.getFilterTitle());
            tempJsonObject.put("extensions", filterValues);
            jsonArray.put(tempJsonObject);
        }

        return jsonArray.toString();
    }
}
