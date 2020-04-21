package com.fanap.midhco.appstore.service.anouncement;

import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.vos.ProductVO;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.appcategory.AppCategoryService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.EnumCaptionHelper;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by admin123 on 8/28/2017.
 */
public class AppSearchAnouncementActionDescriptor implements IAnouncementActionDescriptor<String> {

    public AppSearchAnouncementActionDescriptor() {}

    @Override
    public ITaskResult<String> doAction(String actionDescriptor, Map<String, String> parametersMap) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        try {
            AppService.AppSearchCriteria searchCriteria = new AppService.AppSearchCriteria();

            JSONObject jsonObject = new JSONObject(actionDescriptor);
            if(jsonObject.getJSONArray("creatorIDList") != null && !jsonObject.get("creatorIDList").equals(JSONObject.NULL)) {
                JSONArray creatorIDJsonArray = jsonObject.getJSONArray("creatorIDList");
                searchCriteria.creatorUsers = new ArrayList<>();
                for(int i = 0; i < creatorIDJsonArray.length(); i++) {
                    User user = (User)session.load(User.class, creatorIDJsonArray.getLong(i));
                    searchCriteria.creatorUsers.add(user);
                }
            }

            if(jsonObject.has("keyWords") && !jsonObject.get("keyWords").equals(JSONObject.NULL)) {
                searchCriteria.keyword = new ArrayList<>();
                JSONArray keyWordsJsonArray = jsonObject.getJSONArray("keyWords");
                for(int  i =0; i < keyWordsJsonArray.length(); i++) {
                    searchCriteria.keyword.add(keyWordsJsonArray.getString(i).trim());
                }
            }

            if(jsonObject.has("appCategoryIDList") && jsonObject.get("appCategoryIDList") != null && !jsonObject.get("appCategoryIDList").equals(JSONObject.NULL)) {
                searchCriteria.appCategory = new ArrayList<>();
                JSONArray appCategoryJsonArray = jsonObject.getJSONArray("appCategoryIDList");
                for(int i = 0; i < appCategoryJsonArray.length(); i++) {
                    AppCategory appCategory = AppCategoryService.Instance.loadCategoryById(appCategoryJsonArray.getLong(i), session);
                    searchCriteria.appCategory.add(appCategory);
                }
            }

            if(jsonObject.has("developerIDList") && jsonObject.get("developerIDList") != null && !jsonObject.get("developerIDList").equals(JSONObject.NULL)) {
                searchCriteria.developers = new ArrayList<>();
                JSONArray developerIDListJsonArray = jsonObject.getJSONArray("developerIDList");
                for(int i = 0; i < developerIDListJsonArray.length(); i++) {
                    User user = UserService.Instance.findUserWithUserId(developerIDListJsonArray.getLong(i), session);
                    searchCriteria.developers.add(user);
                }
            }

            if(jsonObject.has("osTypeIDList") && jsonObject.get("osTypeIDList") != null && !jsonObject.get("osTypeIDList").equals(JSONObject.NULL)) {
                searchCriteria.osType = new ArrayList<>();
                JSONArray osTypeIDListIDListJsonArray = jsonObject.getJSONArray("osTypeIDList");
                for(int i = 0; i < osTypeIDListIDListJsonArray.length(); i++) {
                    OSType osType = (OSType)session.load(OSType.class, osTypeIDListIDListJsonArray.getLong(i));
                    searchCriteria.osType.add(osType);
                }
            }

            if(jsonObject.has("packageName") && jsonObject.get("packageName") != null && !jsonObject.get("packageName").equals(JSONObject.NULL)) {
                searchCriteria.packageName = jsonObject.getString("packageName");
            }

            if(jsonObject.has("title") && jsonObject.get("title") != null && !jsonObject.get("title").equals(JSONObject.NULL)) {
                searchCriteria.title = jsonObject.getString("title");
            }

            if(jsonObject.has("versionName") && jsonObject.get("versionName") != null && !jsonObject.get("versionName").equals(JSONObject.NULL)) {
                searchCriteria.versionName = jsonObject.getString("versionName");
            }

            if(jsonObject.has("versionCode") && jsonObject.get("versionCode") != null && !jsonObject.get("versionCode").equals(JSONObject.NULL)) {
                searchCriteria.versionCode = jsonObject.getString("versionCode");
            }

            if(jsonObject.has("osIDList") && jsonObject.get("osIDList") != null && !jsonObject.get("osIDList").equals(JSONObject.NULL)) {
                searchCriteria.os = new ArrayList<>();
                JSONArray osIDListIDListJsonArray = jsonObject.getJSONArray("osIDList");
                for(int i = 0; i < osIDListIDListJsonArray.length(); i++) {
                    OS os = (OS)session.load(OS.class, osIDListIDListJsonArray.getLong(i));
                    searchCriteria.os.add(os);
                }
            }

//            if(jsonObject.has("bytePublishStates") && jsonObject.get("bytePublishStates") != null && !jsonObject.get("bytePublishStates").equals(JSONObject.NULL)) {
//                searchCriteria.publishStates = new ArrayList<>();
//                JSONArray publisJsonArray = jsonObject.getJSONArray("bytePublishStates");
//                for(int i = 0; i < publisJsonArray.length(); i++) {
//                    PublishState publishState = new PublishState(Byte.valueOf(publisJsonArray.get(i).toString()));
//                    searchCriteria.publishStates.add(publishState);
//                }
//            }
                searchCriteria.publishStates = new ArrayList<>();
            searchCriteria.publishStates.add(PublishState.PUBLISHED);

            if(jsonObject.has("creationDateTime") && jsonObject.get("creationDateTime") != null && !jsonObject.get("creationDateTime").equals(JSONObject.NULL)) {
                searchCriteria.creationDateTime = new DateTime[2];
                JSONArray creationDateTimeArray = jsonObject.getJSONArray("creationDateTime");

                if(creationDateTimeArray.get(0) != null && !creationDateTimeArray.get(0).equals(JSONObject.NULL)) {
                    Long creationFromLong = creationDateTimeArray.getLong(0);
                    searchCriteria.creationDateTime[0] = new DateTime(new Date(creationFromLong));
                } else {
                    searchCriteria.creationDateTime[0] = null;
                }

                if(creationDateTimeArray.get(1) != null && !creationDateTimeArray.get(1).equals(JSONObject.NULL)) {
                    Long creationToLong = creationDateTimeArray.getLong(1);
                    searchCriteria.creationDateTime[1] = new DateTime(new Date(creationToLong));
                } else {
                    searchCriteria.creationDateTime[1] = null;
                }
            }


            int from = Integer.parseInt((parametersMap!=null && parametersMap.size()>0 && parametersMap.get("from")!=null) ? parametersMap.get("from") : "0" );
            int to = Integer.parseInt((parametersMap!=null && parametersMap.size()>0 && parametersMap.get("to")!=null) ? parametersMap.get("to") : "-1" );

            to = to - from;

            User currentUser = PrincipalUtil.getCurrentUser();
            if (currentUser!=null || !PrincipalUtil.hasPermission(Access.APP_REMOVE)){
                searchCriteria.setDeleted(false);
            }
            List<AppService.AppSearchResultModel> resultObjects =
                    AppService.Instance.list(searchCriteria, from, to, null, true, session);

            Stream<ProductVO> productVOStream =
                    resultObjects.stream().map(appSearchResultModel -> {
                        try {
                            return ProductVO.buildProductVO(appSearchResultModel);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    });
            List<ProductVO> productVOList = productVOStream.collect(Collectors.<ProductVO>toList());

            ITaskResult taskResult = new ITaskResult() {
                String resultString = JsonUtil.getJson(productVOList);

                @Override
                public Object getResult() {
                    return resultString;
                }
            };

            return taskResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            if(session != null)
                session.close();
        }
    }

    @Override
    public String toString() {
        return EnumCaptionHelper.ANOUNCEMENTMAP.get(this.getClass());
    }
}
