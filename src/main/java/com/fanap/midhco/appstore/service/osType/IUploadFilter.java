package com.fanap.midhco.appstore.service.osType;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin123 on 7/1/2016.
 */
public interface IUploadFilter extends Serializable {
    public String getFilterTitle();
    public List<String> getFilterList();

    public static IUploadFilter getImageUploadFilter() {
        return new IUploadFilter() {

            @Override
            public String getFilterTitle() {
                return "image files";
            }

            @Override
            public List<String> getFilterList() {
                return Arrays.asList("jpg", "png");
            }
        };
    }

    public static IUploadFilter getVideoUploadFilter() {
        return new IUploadFilter() {

            @Override
            public String getFilterTitle() {
                return "video files";
            }

            @Override
            public List<String> getFilterList() {
                return Arrays.asList("mp4", "avi","flv","wmv");
            }
        };
    }

    static IUploadFilter getImageAndVideoUploadFilter() {
        return new IUploadFilter() {

            @Override
            public String getFilterTitle() {
                return "image and video files";
            }

            @Override
            public List<String> getFilterList() {
                return Arrays.asList("mp4", "avi","flv","wmv","jpg", "png");
            }
        };
    }

    public static IUploadFilter getHTMLUploadFilter() {
        return new IUploadFilter() {

            @Override
            public String getFilterTitle() {
                return "html files";
            }

            @Override
            public List<String> getFilterList() {
                return Arrays.asList("html");
            }
        };
    }


    public static IUploadFilter getAnyUploadFilter() {
        return new IUploadFilter() {

            @Override
            public String getFilterTitle() {
                return "label.file";
            }

            @Override
            public List<String> getFilterList() {
                return Arrays.asList("*");
            }
        };
    }

    public static IUploadFilter getExcelUploadFilter() {
        return new IUploadFilter() {

            @Override
            public String getFilterTitle() {
                return "excel files";
            }

            @Override
            public List<String> getFilterList() {
                return Arrays.asList("xlsx", "xls");
            }
        };
    }

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
