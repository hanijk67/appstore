package com.fanap.midhco.appstore.service.app;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.App;
import com.fanap.midhco.appstore.entities.AppPackage;
import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.comment.CommentService;
import com.fanap.midhco.appstore.service.jest.JestService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.HistogramAggregation;
import org.apache.wicket.model.Model;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by admin123 on 8/7/2017.
 */
public class AppElasticService {
    public static AppElasticService Instance = new AppElasticService();
    final static String indexName = ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_INDEX_NAME);

    public enum InstallAction {
        INSTALL(0), UNINSTALL(1);

        int action;

        InstallAction(int action) {
            this.action = action;
        }

        public int getAction() {
            return action;
        }
    }

    private AppElasticService() {
    }

    public static class FavoriteAppVO {

        public FavoriteAppVO(String inputString) {
            JSONObject jsonObject = new JSONObject(inputString);
            try {
                if (jsonObject.has("packageName")) {
                    this.packageName = jsonObject.getString("packageName");
                }
                if (jsonObject.has("userId")) {
                    this.userId = jsonObject.getString("userId");
                }
            } finally {

            }
        }

        public FavoriteAppVO() {
        }

        String id;
        String userId;
        String appId;
        String packageName;
        String favoriteAppId;
        Long modifyDateTime;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getFavoriteAppId() {
            return favoriteAppId;
        }

        public void setFavoriteAppId(String favoriteAppId) {
            this.favoriteAppId = favoriteAppId;
        }

        public Long getModifyDateTime() {
            return modifyDateTime;
        }

        public void setModifyDateTime(Long modifyDateTime) {
            this.modifyDateTime = modifyDateTime;
        }
    }

    public static class FavoriteAppCriteria {
        String userId;
        String id;
        String appId;
        Long fromDate;
        Long toDate;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public Long getFromDate() {
            return fromDate;
        }

        public void setFromDate(Long fromDate) {
            this.fromDate = fromDate;
        }

        public Long getToDate() {
            return toDate;
        }

        public void setToDate(Long toDate) {
            this.toDate = toDate;
        }
    }

    public static class AppRelevancyRateVO {
        String id;
        Long appId;
        Long appComparedToId;
        Integer relevancyRate;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Long getAppId() {
            return appId;
        }

        public void setAppId(Long appId) {
            this.appId = appId;
        }

        public Long getAppComparedToId() {
            return appComparedToId;
        }

        public void setAppComparedToId(Long appComparedToId) {
            this.appComparedToId = appComparedToId;
        }

        public Integer getRelevancyRate() {
            return relevancyRate;
        }

        public void setRelevancyRate(Integer relevancyRate) {
            this.relevancyRate = relevancyRate;
        }
    }

    public static class AppRelevancyRateCriteria {
        Long appId;
        List<Long> excludedAppIds;
        Long appComparedToId;
        Integer relevancyRateFrom;
        Integer relevancyRateTo;
    }

    public static class AppKeyWordCriteria {
        String id;
        Long appId;
        String appPackageName;
        Long osTypeId;
        Long osId;
        List<String> keyWordList;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Long getAppId() {
            return appId;
        }

        public void setAppId(Long appId) {
            this.appId = appId;
        }

        public String getAppPackageName() {
            return appPackageName;
        }

        public void setAppPackageName(String appPackageName) {
            this.appPackageName = appPackageName;
        }

        public Long getOsTypeId() {
            return osTypeId;
        }

        public void setOsTypeId(Long osTypeId) {
            this.osTypeId = osTypeId;
        }

        public Long getOsId() {
            return osId;
        }

        public void setOsId(Long osId) {
            this.osId = osId;
        }

        public List<String> getKeyWordList() {
            return keyWordList;
        }

        public void setKeyWordList(List<String> keyWordList) {
            this.keyWordList = keyWordList;
        }
    }

    public static class AppKeyWordVO {
        String id;
        Long appId;
        String appPackageName;
        Long osTypeId;
        Long osId;
        String keyword;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Long getAppId() {
            return appId;
        }

        public void setAppId(Long appId) {
            this.appId = appId;
        }

        public String getAppPackageName() {
            return appPackageName;
        }

        public void setAppPackageName(String appPackageName) {
            this.appPackageName = appPackageName;
        }

        public Long getOsTypeId() {
            return osTypeId;
        }

        public void setOsTypeId(Long osTypeId) {
            this.osTypeId = osTypeId;
        }

        public Long getOsId() {
            return osId;
        }

        public void setOsId(Long osId) {
            this.osId = osId;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public List<String> splitAppKeyWord() {
            Pattern pattern = Pattern.compile("(\\s*#)");
            String[] splittedStringArray = pattern.split(keyword);

            List<String> retListString = new ArrayList<>();

            for (String splitted : splittedStringArray) {
                if (!splitted.trim().isEmpty()) {
                    retListString.add(splitted.trim());
                }
            }

            return retListString;
        }
    }

    public static class AppInstallCriteria implements Serializable {
        public Long appId;
        public String installAction;  // 0 means Install and 1 means unInstall

        public Long userId;
        public String userName;
        public String deviceId;
        public String appPackageName;
    }

    public static class AppInstallVO {
        String id;
        String deviceId;
        Long time;
        Long appId;
        String appPackageVersionCode;
        String appName;
        Long appPackageId;
        String appPackageVersionName;
        String appPackageName;
        Long osId;
        String osName;
        Long osTypeId;
        String osTypeName;
        String osVersion;
        Long userId;
        String userName;
        String action;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }

        public Long getAppId() {
            return appId;
        }

        public void setAppId(Long appId) {
            this.appId = appId;
        }

        public String getAppPackageVersionCode() {
            return appPackageVersionCode;
        }

        public void setAppPackageVersionCode(String appPackageVersionCode) {
            this.appPackageVersionCode = appPackageVersionCode;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public Long getAppPackageId() {
            return appPackageId;
        }

        public void setAppPackageId(Long appPackageId) {
            this.appPackageId = appPackageId;
        }

        public String getAppPackageVersionName() {
            return appPackageVersionName;
        }

        public void setAppPackageVersionName(String appPackageVersionName) {
            this.appPackageVersionName = appPackageVersionName;
        }

        public String getAppPackageName() {
            return appPackageName;
        }

        public void setAppPackageName(String appPackageName) {
            this.appPackageName = appPackageName;
        }

        public Long getOsId() {
            return osId;
        }

        public void setOsId(Long osId) {
            this.osId = osId;
        }

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public Long getOsTypeId() {
            return osTypeId;
        }

        public void setOsTypeId(Long osTypeId) {
            this.osTypeId = osTypeId;
        }

        public String getOsTypeName() {
            return osTypeName;
        }

        public void setOsTypeName(String osTypeName) {
            this.osTypeName = osTypeName;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }

    public AppInstallVO buildAppInstallVO(App app, String deviceId, Long time,
                                          String userName, Long userId) throws Exception {
        if (deviceId == null) {
            throw new Exception("no deviceId is given!");
        }

//        if(!DeviceElasticService.Instance.isDeviceMetaDataPresent(deviceId))
//            throw new Exception("no device with deviceId " + deviceId +" registered!");

        AppInstallVO appInstallVO = new AppInstallVO();
        appInstallVO.setAppId(app.getId());
        appInstallVO.setAppName(app.getTitle());
        appInstallVO.setTime(time);
        appInstallVO.setDeviceId(deviceId);
        appInstallVO.setAppPackageName(app.getPackageName());

        AppPackage appMainPackage = app.getMainPackage();
        appInstallVO.setAppPackageId(appMainPackage.getId());
        appInstallVO.setAppPackageVersionName(appMainPackage.getVersionName());
        appInstallVO.setAppPackageVersionCode(appMainPackage.getVersionCode());

        appInstallVO.setOsId(app.getOs() != null ? app.getOs().getId() : null);
        appInstallVO.setOsName(app.getOs() != null ? app.getOs().getOsName() : null);
        appInstallVO.setOsVersion(app.getOs() != null ? app.getOs().getOsVersion() : null);
        appInstallVO.setOsTypeId(app.getOsType().getId());
        appInstallVO.setOsTypeName(app.getOsType().getName());

        appInstallVO.setUserId(userId);
        appInstallVO.setUserName(userName);

        return appInstallVO;
    }

    public void deleteAllAppRelevencies(Long appId) throws Exception {
        AppRelevancyRateCriteria appRelevancyRateCriteria = new AppRelevancyRateCriteria();
        appRelevancyRateCriteria.appId = appId;

        List<AppRelevancyRateVO> relevancyRateVOs = listAppRelevencyRate(appRelevancyRateCriteria, 0, -1, null, true);
        for (AppRelevancyRateVO appRelevancyRateVO : relevancyRateVOs) {
            deleteRelevencyRate(appRelevancyRateVO.getId());
        }
    }

    public JestResult deleteRelevencyRate(String relevencyId) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            JestResult jestResult = jestClient.execute(new Delete.Builder(relevencyId)
                    .index(indexName)
                    .type("appRelevancyRate")
                    .build());
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public JestResult insertAppRelevancyRateVO(AppRelevancyRateVO appRelevancyRateVO) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            Index index = new Index.Builder(appRelevancyRateVO).index(indexName).type("appRelevancyRate")
                    .id(String.valueOf(appRelevancyRateVO.appId) + "_" + String.valueOf(appRelevancyRateVO.appComparedToId)).build();
            JestResult jestResult = jestClient.execute(index);
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public Long countAppRelevencyRate(AppRelevancyRateCriteria relevancyRateCriteria) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            List<AppRelevancyRateVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            if (relevancyRateCriteria != null)
                applyAppRelevencyRateCriteria(relevancyRateCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("appRelevancyRate")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                return result.getTotal();
            } else {
                throw new IOException(result.getErrorMessage());
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }


    public List<AppRelevancyRateVO> listAppRelevencyRate(AppRelevancyRateCriteria relevancyRateCriteria, int from, int size, String sortParam, boolean isAsc) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            List<AppRelevancyRateVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(from).size(size);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            if (relevancyRateCriteria != null)
                applyAppRelevencyRateCriteria(relevancyRateCriteria, boolQueryBuilder);

            if (sortParam != null && !sortParam.trim().isEmpty()) {
                searchSourceBuilder.sort(sortParam, isAsc ? SortOrder.ASC : SortOrder.DESC);
            }

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("appRelevancyRate")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<SearchResult.Hit<AppRelevancyRateVO, Void>> tempResultList = result.getHits(AppRelevancyRateVO.class);

                    for (SearchResult.Hit<AppRelevancyRateVO, Void> tempResult : tempResultList) {
                        AppRelevancyRateVO appRelevancyRateVO = tempResult.source;
                        appRelevancyRateVO.setId(tempResult.id);
                        retList.add(tempResult.source);
                    }
                }
            } else {
                throw new IOException(result.getErrorMessage());
            }
            return retList;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public JestResult insertAppKeyWordVO(AppKeyWordVO appKeyWordVO) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            Index index;
            index = new Index.Builder(appKeyWordVO).index(indexName).type("appKeyWords").id(appKeyWordVO.getAppId() + "_" + appKeyWordVO.getOsTypeId()).build();
            JestResult jestResult = jestClient.execute(index);
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public JestResult insertAppInstallVO(AppInstallVO appInstallVO) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            Index index = new Index.Builder(appInstallVO).index(indexName).type("appInstall")
                    .id(appInstallVO.deviceId + "_" + appInstallVO.getOsTypeId() + "_" + appInstallVO.appPackageName)
                    .build();
            JestResult jestResult = jestClient.execute(index);
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public void applyAppRelevencyRateCriteria(AppRelevancyRateCriteria appRelevancyRateCriteria, BoolQueryBuilder boolQueryBuilder) {
        if (appRelevancyRateCriteria.appId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appId", appRelevancyRateCriteria.appId));
        }

        if (appRelevancyRateCriteria.appComparedToId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appComparedToId", appRelevancyRateCriteria.appComparedToId));
        }

        if (appRelevancyRateCriteria.excludedAppIds != null && !appRelevancyRateCriteria.excludedAppIds.isEmpty()) {
            BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();

            for (Long appId : appRelevancyRateCriteria.excludedAppIds) {
                boolQueryBuilder1.mustNot(QueryBuilders.matchQuery("appComparedToId", appId));
            }

            boolQueryBuilder.must(
                    boolQueryBuilder1
            );
        }

        if (appRelevancyRateCriteria.relevancyRateFrom != null || appRelevancyRateCriteria.relevancyRateTo != null) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("relevancyRate");

            if (appRelevancyRateCriteria.relevancyRateFrom != null)
                rangeQueryBuilder.gte(appRelevancyRateCriteria.relevancyRateFrom);
            if (appRelevancyRateCriteria.relevancyRateTo != null)
                rangeQueryBuilder.lte(appRelevancyRateCriteria.relevancyRateTo);

            boolQueryBuilder.filter(rangeQueryBuilder);
        }
    }

    private void applyFavoriteAppCriteriaCriteria(FavoriteAppCriteria favoriteAppCriteria, BoolQueryBuilder boolQueryBuilder) {

        if (favoriteAppCriteria.id != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("_id", favoriteAppCriteria.id));
        }
        if (favoriteAppCriteria.userId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("userId", favoriteAppCriteria.userId));
        }
        if (favoriteAppCriteria.appId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("favoriteAppId", favoriteAppCriteria.appId));
        }

        if (favoriteAppCriteria.fromDate != null || favoriteAppCriteria.toDate != null) {

            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("modifyDateTime");

            if (favoriteAppCriteria.fromDate != null)
                rangeQueryBuilder.gte(favoriteAppCriteria.fromDate);
            if (favoriteAppCriteria.toDate != null)
                rangeQueryBuilder.lte(favoriteAppCriteria.toDate);

            boolQueryBuilder.filter(rangeQueryBuilder);

        }
    }

    public void applyAppKeyWordCriteriaCriteria(AppKeyWordCriteria appKeyWordCriteria, BoolQueryBuilder boolQueryBuilder) {
        if (appKeyWordCriteria.appId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appId", appKeyWordCriteria.appId));
        }

        if (appKeyWordCriteria.appPackageName != null && !appKeyWordCriteria.appPackageName.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appPackageName", appKeyWordCriteria.appPackageName));
        }

        if (appKeyWordCriteria.osId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("osId", appKeyWordCriteria.osId));
        }

        if (appKeyWordCriteria.osTypeId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("osTypeId", appKeyWordCriteria.osTypeId));
        }

        if (appKeyWordCriteria.keyWordList != null && !appKeyWordCriteria.keyWordList.isEmpty()) {
            boolQueryBuilder.should(QueryBuilders.termsQuery("keyword", (String[]) appKeyWordCriteria.keyWordList.toArray(new String[appKeyWordCriteria.keyWordList.size()])));
        }
    }

    public void applyInstallAppVOCriteria(AppInstallCriteria appInstallCriteria, BoolQueryBuilder boolQueryBuilder) {
        if (appInstallCriteria.appId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appId", appInstallCriteria.appId));
        }

        if (appInstallCriteria.userId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("userId", appInstallCriteria.userId));
        }

        if (appInstallCriteria.userName != null && !appInstallCriteria.userName.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("userName", appInstallCriteria.userName));
        }

        if (appInstallCriteria.installAction != null && !appInstallCriteria.installAction.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("action", appInstallCriteria.installAction));
        }

        if (appInstallCriteria.deviceId != null && !appInstallCriteria.deviceId.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("deviceId", appInstallCriteria.deviceId));
        }

        if (appInstallCriteria.appPackageName != null && !appInstallCriteria.appPackageName.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appPackageName", appInstallCriteria.appPackageName));
        }
    }

    public Long getInstallationCountForApp(AppInstallCriteria appInstallCriteria) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyInstallAppVOCriteria(appInstallCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("appInstall")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                return result.getTotal();
            } else {
                throw new IOException(result.getErrorMessage());
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public List<AppInstallVO> getInstallationInfoForApp(AppInstallCriteria appInstallCriteria, int from, int size) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        JestClient jestClient = JestService.Instance.getJestClient();

        try {
            List<AppInstallVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(from);

            if (size != -1)
                searchSourceBuilder.size(size);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyInstallAppVOCriteria(appInstallCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("appInstall")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<SearchResult.Hit<AppInstallVO, Void>> tempResultList = result.getHits(AppInstallVO.class);

                    for (SearchResult.Hit<AppInstallVO, Void> tempResult : tempResultList) {
                        AppInstallVO appInstallVO = tempResult.source;
                        appInstallVO.setId(tempResult.id);
                        retList.add(tempResult.source);
                    }
                }
            } else {
                throw new IOException(result.getErrorMessage());
            }

            return retList;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public Long countAppKeyWord(AppKeyWordCriteria appKeyWordCriteria) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            List<AppKeyWordVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyAppKeyWordCriteriaCriteria(appKeyWordCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("appKeyWords")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                return result.getTotal();
            } else {
                throw new IOException(result.getErrorMessage());
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public List<AppKeyWordVO> listAppKeyWord(AppKeyWordCriteria appKeyWordCriteria, int from, int size) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            List<AppKeyWordVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(from).size(size);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyAppKeyWordCriteriaCriteria(appKeyWordCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("appKeyWords")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<SearchResult.Hit<AppKeyWordVO, Void>> tempResultList = result.getHits(AppKeyWordVO.class);

                    for (SearchResult.Hit<AppKeyWordVO, Void> tempResult : tempResultList) {
                        AppKeyWordVO appKeyWordVO = tempResult.source;
                        appKeyWordVO.setId(tempResult.id);
                        retList.add(tempResult.source);
                    }
                }
            } else {
                throw new IOException(result.getErrorMessage());
            }

            return retList;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }


    public AppKeyWordCriteria buildAppKeyWordCriteriaByApp(App app) {
        AppElasticService.AppKeyWordCriteria appKeyWordCriteria = new AppElasticService.AppKeyWordCriteria();
        App loadedApp = (App) HibernateUtil.getCurrentSession().load(App.class, app.getId());

        appKeyWordCriteria.setAppId(loadedApp.getId());
        appKeyWordCriteria.setOsId(loadedApp.getOs() != null ? loadedApp.getOs().getId() : null);
        appKeyWordCriteria.setOsTypeId(loadedApp.getOsType().getId());
//        appKeyWordCriteria.setAppPackageName(loadedApp.getPackageName());
        return appKeyWordCriteria;
    }

    public Map<String, String> getKeyWordMap(AppKeyWordCriteria appKeyWordCriteria, int from, int count) {
        List<AppElasticService.AppKeyWordVO> appKeyWordVOList;
        Map<String, String> keyWordMap = new HashMap<>();
        try {
            appKeyWordVOList = AppElasticService.Instance.listAppKeyWord(appKeyWordCriteria, from, count);
        } catch (Exception e) {
            appKeyWordVOList = new ArrayList<>();
        }

        for (AppElasticService.AppKeyWordVO appKeyWordVO : appKeyWordVOList) {
            keyWordMap.put(appKeyWordVO.getKeyword(), appKeyWordVO.getId());
        }

        return keyWordMap;
    }

    public String setElasticKeyword(String keyWordModelString) {

        List<String> keyWordArray = Arrays.asList(keyWordModelString.split(","));
        StringBuilder keywordBuilder = new StringBuilder("");
        int keyWordSize = 0;
        for (String inputKeyWord : keyWordArray) {

            keywordBuilder.append("#").append(inputKeyWord.trim());
            if (keyWordSize != keyWordArray.size() - 1) {
                keywordBuilder.append(" ");
            }

            keyWordSize++;
        }
        return keywordBuilder.toString();
    }


    public Model<String> getKeywordModelByKeywordMap(Map<String, String> keyWordMap) {
        StringBuilder keyWordBuilder = new StringBuilder("");
        int keyWordSize = 0;
        String keywordModel = "";
        for (Map.Entry<String, String> entry : keyWordMap.entrySet()) {
            keywordModel = entry.getKey().replace("#", "");
        }
        String[] stringModels = keywordModel.split(" ");

        for (String str : stringModels) {
            keyWordSize++;
            keyWordBuilder.append(str);
            if (keyWordSize != stringModels.length) {
                keyWordBuilder.append(" ").append(",");
            }
        }

        return Model.of(keyWordBuilder.toString());
    }

    public List<AppInstallVO> getNotIncludedPackagesForOneDevice(String deviceId, OSType osType, List<String> packageNameList) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            List<AppInstallVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            boolQueryBuilder.must(QueryBuilders.matchQuery("osTypeId", osType.getId()));
            boolQueryBuilder.must(QueryBuilders.matchQuery("deviceId", deviceId));
            boolQueryBuilder.must(QueryBuilders.matchQuery("action", InstallAction.INSTALL.toString()));

            for (String packageName : packageNameList) {
                boolQueryBuilder.mustNot(QueryBuilders.matchQuery("appPackageName", packageName));
            }

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("appInstall")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<SearchResult.Hit<AppInstallVO, Void>> tempResultList = result.getHits(AppInstallVO.class);

                    for (SearchResult.Hit<AppInstallVO, Void> tempResult : tempResultList) {
                        AppInstallVO appInstallVO = tempResult.source;
                        retList.add(appInstallVO);
                    }
                }
            } else {
                throw new IOException(result.getErrorMessage());
            }

            return retList;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public double gerAverageInstallRate(AppInstallCriteria appInstallCriteria, int from, int size, String sortProp, boolean isAsc) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();

        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(from).size(size);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyInstallAppVOCriteria(appInstallCriteria, boolQueryBuilder);

            if (sortProp != null)
                searchSourceBuilder.sort(sortProp, isAsc ? SortOrder.ASC : SortOrder.DESC);

            searchSourceBuilder.query(boolQueryBuilder).aggregation(AggregationBuilders.avg("rateAverage").field("rateIndex"));
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("comment")
                    .build();


            SearchResult result = jestClient.execute(search);
            Double rateAverage = -1D;

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    JsonObject jsonObject = result.getJsonObject();
                    if (jsonObject.has("aggregations")) {
                        JsonElement jsonElement = jsonObject.get("aggregations");
                        String jsonString = jsonElement.toString();
                        JSONObject rateAverageJsonObject = new JSONObject(jsonString);
                        if (rateAverageJsonObject.has("rateAverage")) {
                            JSONObject valueJsonObject = rateAverageJsonObject.getJSONObject("rateAverage");
                            if (valueJsonObject.has("value")) {
                                rateAverage = Double.valueOf(valueJsonObject.getDouble("value"));
                            }
                        }
                    }
                }
            }

            return rateAverage;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public JestResult insertFavoriteAppVO(FavoriteAppVO favoriteAppVO) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            Index index = new Index.Builder(favoriteAppVO).index(indexName).type("favoriteApp")
                    .id(String.valueOf(favoriteAppVO.getId()))
                    .build();
            JestResult jestResult = jestClient.execute(index);
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public List<FavoriteAppVO> getFavoriteApp(FavoriteAppCriteria favoriteAppCriteria, int from, int size, String sortProp, boolean isAsc) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            searchSourceBuilder.from(from).size(size);

            applyFavoriteAppCriteriaCriteria(favoriteAppCriteria, boolQueryBuilder);

            if (sortProp != null)
                searchSourceBuilder.sort(sortProp, isAsc ? SortOrder.ASC : SortOrder.DESC);

            searchSourceBuilder.query(boolQueryBuilder);

            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("favoriteApp")
                    .build();


            SearchResult result = jestClient.execute(search);

            List<FavoriteAppVO> retList = new ArrayList<>();

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<SearchResult.Hit<FavoriteAppVO, Void>> tempResultList = result.getHits(FavoriteAppVO.class);
                    for (SearchResult.Hit<FavoriteAppVO, Void> tempResult : tempResultList) {
                        FavoriteAppVO favoriteAppVO = tempResult.source;
                        retList.add(favoriteAppVO);
                    }
                }
            } else {
                throw new IOException(result.getErrorMessage());
            }

            return retList;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }

    }

    public List<CommentService.HistogramBucketVO> getFavoriteHistogram(FavoriteAppCriteria favoriteAppCriteria, DateHistogramInterval dateHistogramInterval) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            applyFavoriteAppCriteriaCriteria(favoriteAppCriteria, boolQueryBuilder);

            DateHistogramAggregationBuilder date_histogram_aggregation = AggregationBuilders.dateHistogram("date_histogram");
            date_histogram_aggregation.field("lastModifyDate")
                    .minDocCount(1l).format("yyyy-MM-dd")
                    .dateHistogramInterval(dateHistogramInterval);
            searchSourceBuilder.query(boolQueryBuilder).aggregation(AggregationBuilders.terms("groupByDate").field("modifyDateTime"));
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("favoriteApp")
                    .build();

            SearchResult result = jestClient.execute(search);

            List<CommentService.HistogramBucketVO> bucketVOList = new ArrayList<>();

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {

                    List<HistogramAggregation.Histogram> histogramList = result.getAggregations().getAggregation("groupByDate", HistogramAggregation.class).getBuckets();

                    for (HistogramAggregation.Histogram histogram : histogramList) {
                        CommentService.HistogramBucketVO histogramRateBucketVO = new CommentService.HistogramBucketVO();

                        Long dateKey = histogram.getKey();
                        Long histCount = histogram.getCount();

                        Date date = new Date(dateKey);
                        DateTime dateTime = new DateTime(date);
                        histogramRateBucketVO.time = dateTime;
                        histogramRateBucketVO.count = histCount;
                        bucketVOList.add(histogramRateBucketVO);
                    }
                }
            } else {
                throw new IOException(result.getErrorMessage());
            }

            return bucketVOList;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }


    public void deleteFavoriteApp(FavoriteAppCriteria favoriteAppCriteria) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            JestResult jestResult = jestClient.execute(new Delete.Builder(favoriteAppCriteria.getId())
                    .index(indexName)
                    .type("favoriteApp")
                    .build());
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }

    }


    public void deleteAppKeyword(AppKeyWordCriteria appKeyWordCriteria) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            JestResult jestResult = jestClient.execute(new Delete.Builder(appKeyWordCriteria.getId())
                    .index(indexName)
                    .type("appKeyWords")
                    .build());
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }


    public static void main(String[] args) throws Exception {
//        AppKeyWordVO appKeyWordVO = new AppKeyWordVO();
//        appKeyWordVO.appId = 120l;
//        appKeyWordVO.appPackageName="com.fanap.se.we";
//        appKeyWordVO.osTypeId=50l;
//        appKeyWordVO.osId=40l;
//        appKeyWordVO.keyword="#f #rt #java #salam";
//
//        JestResult jestResult = Instance.insertAppKeyWordVO(appKeyWordVO);

//        AppKeyWordCriteria appKeyWordCriteria = new AppKeyWordCriteria();
//        appKeyWordCriteria.keyWordList = Arrays.asList("rtwewe", "ja3434va");
//
//        List<AppKeyWordVO> keyWordVOs = Instance.listAppKeyWord(appKeyWordCriteria, 0, 1000);


//        Pattern pattern = Pattern.compile("(\\s*#)");
//        String[] splittedString = pattern.split("#f #rt #java #salam ");
//
//        AppRelevancyRateVO relevancyRateVO = new AppRelevancyRateVO();
//        relevancyRateVO.appId = 1000l;
//        relevancyRateVO.appComparedToId = 3039l;
//        relevancyRateVO.relevancyRate = 20;
//
//        Instance.insertAppRelevancyRateVO(relevancyRateVO);


//        List ls = AppService.Instance.getRelatedApps(mainApp, session);
//
//        session.close();

      /*  Session session = HibernateUtil.getCurrentSession();
        App app = AppService.Instance.load(1062l, session);
        AppElasticService.AppInstallVO appInstallVO =
                AppElasticService.Instance.buildAppInstallVO(
                        app, "www344gh456",
                        DateTime.now().toDate().getTime(),
                        "2333443",
                        445l

                );

        appInstallVO.setAction(AppElasticService.InstallAction.INSTALL.toString());
        JestResult jestResult = AppElasticService.Instance.insertAppInstallVO(appInstallVO);


        session.close();*/

//
//        FavoriteAppVO favoriteAppVO = new FavoriteAppVO();
//        favoriteAppVO.setUserId("22");
//        favoriteAppVO.setFavoriteAppId("1000");
//        favoriteAppVO.setModifyDateTime(DateTime.now().getTime());
//        favoriteAppVO.setId("22"+"-"+"1000");
//        JestResult result = AppElasticService.Instance.insertFavoriteAppVO(favoriteAppVO);
//
//
//        FavoriteAppVO favoriteAppVO1 = new FavoriteAppVO();
//        favoriteAppVO1.setUserId("22");
//        favoriteAppVO1.setFavoriteAppId("1019");
//        favoriteAppVO1.setModifyDateTime(DateTime.now().getTime());
//        favoriteAppVO1.setId("22"+"-"+"1019");
//        result = AppElasticService.Instance.insertFavoriteAppVO(favoriteAppVO1);
//
//
//        FavoriteAppVO favoriteAppVO2 = new FavoriteAppVO();
//        favoriteAppVO2.setUserId("33");
//        favoriteAppVO2.setFavoriteAppId("1000");
//        favoriteAppVO2.setModifyDateTime(DateTime.now().getTime());
//        favoriteAppVO2.setId("33"+"-"+"1000");
//        result = AppElasticService.Instance.insertFavoriteAppVO(favoriteAppVO2);
//
//
//        FavoriteAppVO favoriteAppVO3 = new FavoriteAppVO();
//        favoriteAppVO3.setUserId("33");
//        favoriteAppVO3.setFavoriteAppId("1019");
//        favoriteAppVO3.setModifyDateTime(DateTime.now().getTime());
//        favoriteAppVO3.setId("33"+"-"+"1019");
//        result = AppElasticService.Instance.insertFavoriteAppVO(favoriteAppVO3);
//
//        FavoriteAppVO favoriteAppVO4 = new FavoriteAppVO();
//        favoriteAppVO4.setUserId("22");
//        favoriteAppVO4.setFavoriteAppId("1039");
//        favoriteAppVO4.setModifyDateTime(DateTime.now().getTime());
//        favoriteAppVO4.setId("22"+"-"+"1039");
//        result = AppElasticService.Instance.insertFavoriteAppVO(favoriteAppVO4);

        FavoriteAppCriteria favoriteAppCriteria = new FavoriteAppCriteria();
        favoriteAppCriteria.setFromDate(1504620006001l);
        favoriteAppCriteria.setToDate(1544622006000l);
        List<FavoriteAppVO> favoriteAppVOS = AppElasticService.Instance.getFavoriteApp(favoriteAppCriteria, 0, -1, "userId", false);
        for (FavoriteAppVO favoriteAppVO6 : favoriteAppVOS) {
            System.out.println("salam");
        }
        System.exit(0);
    }
}

