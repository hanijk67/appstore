package com.fanap.midhco.appstore.service.comment;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.jest.JestService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.FilterAggregation;
import io.searchbox.core.search.aggregation.HistogramAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.hibernate.Session;
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

/**
 * Created by admin123 on 7/31/2017.
 */
public class CommentService {
    static Logger logger = Logger.getLogger(CommentService.class);
    final static String indexName = ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_INDEX_NAME);

    public static CommentService Instance = new CommentService();

    private CommentService() {
    }

    public static class DeviceActivation {
        Long activationTime;

        public Long getActivationTime() {
            return activationTime;
        }

        public void setActivationTime(Long activationTime) {
            this.activationTime = activationTime;
        }
    }

    public static class ApproveVOCriteria {
        String id;
        String parentCommentId;
        Long userId;
        String userName;
        Long approveDateFrom;
        Long approveDateTo;
        Boolean isApproved;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getParentCommentId() {
            return parentCommentId;
        }

        public void setParentCommentId(String parentCommentId) {
            this.parentCommentId = parentCommentId;
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

        public Long getApproveDateFrom() {
            return approveDateFrom;
        }

        public void setApproveDateFrom(Long approveDateFrom) {
            this.approveDateFrom = approveDateFrom;
        }

        public Long getApproveDateTo() {
            return approveDateTo;
        }

        public void setApproveDateTo(Long approveDateTo) {
            this.approveDateTo = approveDateTo;
        }

        public Boolean getApproved() {
            return isApproved;
        }

        public void setApproved(Boolean approved) {
            isApproved = approved;
        }
    }

    public static class ElasticApproveVO {
        String id;
        String parentCommentId;
        Long userId;
        String userName;
        Long approveDate;
        Boolean isApproved;
        String description;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getParentCommentId() {
            return parentCommentId;
        }

        public void setParentCommentId(String parentCommentId) {
            this.parentCommentId = parentCommentId;
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

        public Long getApproveDate() {
            return approveDate;
        }

        public void setApproveDate(Long approveDate) {
            this.approveDate = approveDate;
        }

        public Boolean getApproved() {
            return isApproved;
        }

        public void setApproved(Boolean approved) {
            isApproved = approved;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class ElasticCommentLikeVO {
        String parentCommentId;
        Boolean like;
        Long userId;
        String userName;
        String fullName;
        Integer dislikeCategory;
        Long modifyDateTime;

        public String getParentCommentId() {
            return parentCommentId;
        }

        public void setParentCommentId(String parentCommentId) {
            this.parentCommentId = parentCommentId;
        }

        public Boolean getLike() {
            return like;
        }

        public void setLike(Boolean like) {
            this.like = like;
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

        public Integer getDislikeCategory() {
            return dislikeCategory;
        }

        public void setDislikeCategory(Integer dislikeCategory) {
            this.dislikeCategory = dislikeCategory;
        }

        public Long getModifyDateTime() {
            return modifyDateTime;
        }

        public void setModifyDateTime(Long modifyDateTime) {
            this.modifyDateTime = modifyDateTime;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    public static class ElasticCommentReportVO {
        String parentCommentId;
        String reportText;
        Long userId;
        String userName;

        public String getParentCommentId() {
            return parentCommentId;
        }

        public void setParentCommentId(String parentCommentId) {
            this.parentCommentId = parentCommentId;
        }

        public String getReportText() {
            return reportText;
        }

        public void setReportText(String reportText) {
            this.reportText = reportText;
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
    }

    public static class ElasticCommentVO implements Serializable {
        String id;
        String commentId;
        String deviceId;
        Long appId;
        String appTitle;
        String appPackageVersionName;
        String appPackageVersionCode;
        String appPackageName;
        Long appPackageId;
        Long osTypeId;
        String osTypeName;
        Long osId;
        String osName;
        String osVersion;
        String language;
        String commentText;
        Integer rateIndex;
        Long userId;
        String userName;
        String fullName;
        Long lastModifyDate;
        Boolean approved;
        Long likeCount;
        Map<Integer, Long> dislikeCat2Count;
        boolean hasUserLiked = false;
        boolean hasUserDisLiked = false;

        Date approvalDate;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCommentId() {
            return commentId;
        }

        public void setCommentId(String commentId) {
            this.commentId = commentId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public Long getAppId() {
            return appId;
        }

        public void setAppId(Long appId) {
            this.appId = appId;
        }

        public String getAppTitle() {
            return appTitle;
        }

        public void setAppTitle(String appTitle) {
            this.appTitle = appTitle;
        }

        public String getAppPackageVersionName() {
            return appPackageVersionName;
        }

        public void setAppPackageVersionName(String appPackageVersionName) {
            this.appPackageVersionName = appPackageVersionName;
        }

        public String getAppPackageVersionCode() {
            return appPackageVersionCode;
        }

        public void setAppPackageVersionCode(String appPackageVersionCode) {
            this.appPackageVersionCode = appPackageVersionCode;
        }

        public String getAppPackageName() {
            return appPackageName;
        }

        public void setAppPackageName(String appPackageName) {
            this.appPackageName = appPackageName;
        }

        public Long getAppPackageId() {
            return appPackageId;
        }

        public void setAppPackageId(Long appPackageId) {
            this.appPackageId = appPackageId;
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

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getCommentText() {
            return commentText;
        }

        public void setCommentText(String commentText) {
            this.commentText = commentText;
        }

        public Integer getRateIndex() {
            return rateIndex;
        }

        public void setRateIndex(Integer rateIndex) {
            this.rateIndex = rateIndex;
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

        public Long getLastModifyDate() {
            return lastModifyDate;
        }

        public void setLastModifyDate(Long lastModifyDate) {
            this.lastModifyDate = lastModifyDate;
        }

        public Boolean getApproved() {
            return approved;
        }

        public void setApproved(Boolean approved) {
            this.approved = approved;
        }

        public Long getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(Long likeCount) {
            this.likeCount = likeCount;
        }

        public Map<Integer, Long> getDislikeCat2Count() {
            return dislikeCat2Count;
        }

        public void setDislikeCat2Count(Map<Integer, Long> dislikeCat2Count) {
            this.dislikeCat2Count = dislikeCat2Count;
        }

        public boolean isHasUserLiked() {
            return hasUserLiked;
        }

        public void setHasUserLiked(boolean hasUserLiked) {
            this.hasUserLiked = hasUserLiked;
        }

        public boolean isHasUserDisLiked() {
            return hasUserDisLiked;
        }

        public void setHasUserDisLiked(boolean hasUserDisLiked) {
            this.hasUserDisLiked = hasUserDisLiked;
        }

        public Date getApprovalDate() {
            return approvalDate;
        }

        public void setApprovalDate(Date approvalDate) {
            this.approvalDate = approvalDate;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    public ElasticCommentVO buildCommentVOForElastic(String commentId, App app, String deviceId, String commentText, String language, Long modifyTimeStamp,
                                                     Long userId, RatingIndex ratingIndex, boolean approved) throws Exception {
        if (deviceId == null) {
            throw new Exception("no deviceId is given!");
        }

        ElasticCommentVO elasticCommentVO = null;
        Session session = null;
        try {
            session = HibernateUtil.getNewSession();
            User user = UserService.Instance.getUserBySSOId(userId, session);
            if (user != null) {
                elasticCommentVO = new ElasticCommentVO();
                ;
                elasticCommentVO.setCommentId(commentId);
                elasticCommentVO.setAppId(app.getId());
                elasticCommentVO.setDeviceId(deviceId);
                elasticCommentVO.setAppTitle(app.getTitle());
                elasticCommentVO.setAppPackageName(app.getPackageName());
                elasticCommentVO.setRateIndex(ratingIndex.getState());
                elasticCommentVO.setApproved(approved);

                OSType osType = app.getOsType();
                OS os = app.getOs();

                elasticCommentVO.setOsTypeId(osType.getId());
                elasticCommentVO.setOsTypeName(osType.getName());
                elasticCommentVO.setOsVersion(os != null ? os.getOsVersion() : null);

                elasticCommentVO.setOsId(os != null ? os.getId() : null);
                elasticCommentVO.setOsName(os != null ? os.getOsName() : null);

                elasticCommentVO.setCommentText(commentText);

                AppPackage app_MainPackage = app.getMainPackage();
                elasticCommentVO.setAppPackageId(app_MainPackage.getId());
                elasticCommentVO.setAppPackageVersionCode(app_MainPackage.getVersionCode());
                elasticCommentVO.setAppPackageVersionName(app_MainPackage.getVersionName());

                elasticCommentVO.setLanguage(language);
                elasticCommentVO.setLastModifyDate(modifyTimeStamp);
                elasticCommentVO.setUserName(user.getFullName());
                elasticCommentVO.setUserId(userId);

            }
            return elasticCommentVO;
        } catch (Exception e) {
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

    }

    public ElasticCommentLikeVO buildLikeVOForElastic(boolean like, Long userId, String parentCommentId) {
        ElasticCommentLikeVO elasticCommentLikeVO = null;
        Session session = null;
        try {
            session = HibernateUtil.getNewSession();
            User user = UserService.Instance.getUserBySSOId(userId, session);
            if (user != null) {
                elasticCommentLikeVO = new ElasticCommentLikeVO();
                elasticCommentLikeVO.setLike(like);

                elasticCommentLikeVO.setUserName(user.getUserName());
                elasticCommentLikeVO.setUserId(userId);
                elasticCommentLikeVO.setFullName(user.getFullName());
                elasticCommentLikeVO.setParentCommentId(parentCommentId);
            }
            return elasticCommentLikeVO;
        } catch (Exception e) {
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public ElasticCommentReportVO buildElasticReportVOForElastic(String parentCommentId, String reportText, Long userId, String userName) {
        ElasticCommentReportVO elasticCommentReportVO = new ElasticCommentReportVO();

        elasticCommentReportVO.setParentCommentId(parentCommentId);
        elasticCommentReportVO.setReportText(reportText);
        elasticCommentReportVO.setUserId(userId);
        elasticCommentReportVO.setUserName(userName);

        return elasticCommentReportVO;
    }

    public List<ElasticCommentVO> getApprovalCommentList(String commentId, boolean getApproval) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            List<ElasticCommentVO> elasticCommentVOList = new ArrayList<>();
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();


            BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();
            boolQueryBuilder1.must(QueryBuilders.hasParentQuery("comment", QueryBuilders.termQuery("_id", commentId), true));
            boolQueryBuilder1.must(QueryBuilders.matchQuery("isApproved", getApproval));
            boolQueryBuilder.should(boolQueryBuilder1);


//        AggregationBuilder bucketCountAggregationBuilder = AggregationBuilders.terms("approvalComment").field("_parent");
            AggregationBuilder bucketCountAggregationBuilder = AggregationBuilders.terms("approvalComment").field("_parent");
            searchSourceBuilder.query(boolQueryBuilder).aggregation(bucketCountAggregationBuilder);

            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("commentApprove")
                    .build();

            SearchResult result = jestClient.execute(search);


            List<TermsAggregation.Entry> approvalCommentEntryList =
                    result.getAggregations().getAggregation("approvalComment", TermsAggregation.class).getBuckets();


            for (TermsAggregation.Entry entry : approvalCommentEntryList) {

                ApproveVOCriteria approveVOCriteria = new ApproveVOCriteria();
                ElasticCommentVO elasticCommentVO = new ElasticCommentVO();

                approveVOCriteria.setId(entry.getKey());
                ElasticApproveVO resultElasticApproveVO = getApproveComment(approveVOCriteria);
                CommentVOCriteria commentVOCriteria = new CommentVOCriteria();
                commentVOCriteria.id = resultElasticApproveVO.getParentCommentId();
                List<ElasticCommentVO> elasticCommentVOS = getCommentsForApp(commentVOCriteria, 0, -1, "id", true);
                if (elasticCommentVOS != null && !elasticCommentVOS.isEmpty()) {
                    elasticCommentVO = elasticCommentVOS.get(0);
                    elasticCommentVO.setApproved(resultElasticApproveVO.getApproved());
                    elasticCommentVOList.add(elasticCommentVO);
                }
//            likeResultClass.count = entry.getCount();
//            elasticCommentVO.setUserName(entry.get);

            }

            return elasticCommentVOList;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }


    public Long getApproveCommentCountHistory(ApproveVOCriteria approveVOCriteria) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            List<ElasticCommentVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyApproveVOCriteria(approveVOCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("commentApprove")
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


    public ElasticApproveVO getApproveComment(ApproveVOCriteria approveVOCriteria) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyApproveVOCriteria(approveVOCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("commentApprove")
                    .build();


            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<SearchResult.Hit<ElasticApproveVO, Void>> tempResultList = result.getHits(ElasticApproveVO.class);

                    ElasticApproveVO elasticApproveVO = tempResultList.get(0).source;
                    elasticApproveVO.setId(tempResultList.get(0).id);
                    return elasticApproveVO;

                }
            } else {
                throw new IOException(result.getErrorMessage());
            }
            return null;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }


    public List<ElasticApproveVO> getApproveCommentHistory(ApproveVOCriteria approveVOCriteria, int from, int size) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            List<ElasticApproveVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(from).size(size);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyApproveVOCriteria(approveVOCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("commentApprove")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<SearchResult.Hit<ElasticApproveVO, Void>> tempResultList = result.getHits(ElasticApproveVO.class);

                    for (SearchResult.Hit<ElasticApproveVO, Void> tempResult : tempResultList) {
                        ElasticApproveVO elasticApproveVO = tempResult.source;
                        elasticApproveVO.setId(tempResult.id);
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

    public JestResult insertApproveCommentHistory(ElasticApproveVO elasticApproveVO) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            Index index;

            if (!isCommentPresent(elasticApproveVO.getParentCommentId()))
                throw new Exception("no comment with parentId " + elasticApproveVO.getParentCommentId() + " is present!");

            index = new Index.Builder(elasticApproveVO).index(indexName).type("commentApprove").setParameter("parent", elasticApproveVO.getParentCommentId()).build();

            JestResult jestResult = jestClient.execute(index);
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public JestResult insertCommentForAppMainPackage(ElasticCommentVO elasticCommentVO) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            Index index;
            if (elasticCommentVO.getId() != null && !elasticCommentVO.getId().trim().equals("")) {
                index = new Index.Builder(elasticCommentVO).index(indexName).type("comment").id(elasticCommentVO.getId()).build();
            } else {
                index = new Index.Builder(elasticCommentVO).index(indexName).type("comment").build();
            }
            JestResult jestResult = jestClient.execute(index);
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public JestResult deleteCommentLike(String parentCommentId, Long userId) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            JestResult jestResult = jestClient.execute(new Delete.Builder(parentCommentId + "_" + userId + "_" + "true")
                    .setParameter("routing", parentCommentId)
                    .index(indexName)
                    .type("commentLike")
                    .build());
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public JestResult insertLikeForComment(ElasticCommentLikeVO elasticCommentLikeVO) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            if (elasticCommentLikeVO.getParentCommentId() == null)
                throw new Exception("no comment parentId is given");

            if (!isCommentPresent(elasticCommentLikeVO.getParentCommentId()))
                throw new Exception("no comment with parentId " + elasticCommentLikeVO.getParentCommentId() + " is present!");

            Index index = new Index.Builder(elasticCommentLikeVO).index(indexName).type("commentLike")
                    .id(elasticCommentLikeVO.getParentCommentId() + "_" + elasticCommentLikeVO.getUserId() + "_" + elasticCommentLikeVO.getLike())
                    .setParameter("parent", elasticCommentLikeVO.getParentCommentId()).build();
            JestResult jestResult = jestClient.execute(index);
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public JestResult insertReportForComment(ElasticCommentReportVO elasticCommentReportVO) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            if (elasticCommentReportVO.getParentCommentId() == null)
                throw new Exception("no comment parentId is given");

            if (!isCommentPresent(elasticCommentReportVO.getParentCommentId()))
                throw new Exception("no comment with parentId " + elasticCommentReportVO.getParentCommentId() + " is present!");

            Index index = new Index.Builder(elasticCommentReportVO).index(indexName).type("commentReport")
                    .setParameter("parent", elasticCommentReportVO.getParentCommentId()).build();
            JestResult jestResult = jestClient.execute(index);
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public static class TopRatedAppsVO implements Serializable {
        String appId;
        Double averageRateIndex;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public Double getAverageRateIndex() {
            return averageRateIndex;
        }

        public void setAverageRateIndex(Double averageRateIndex) {
            this.averageRateIndex = averageRateIndex;
        }
    }

    public List<TopRatedAppsVO> getTopRatedApps(Long osTypeId, Integer from, Integer size, boolean isAsc) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();

        try {
            List<TopRatedAppsVO> ratedAppsVOList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            if (osTypeId != null) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("osTypeId", osTypeId));
            }

            AggregationBuilder appIdAggregationBuilder = AggregationBuilders.terms("sag").field("appId").size(size).order(Terms.Order.aggregation("rateIndexAvg", isAsc));
            appIdAggregationBuilder.subAggregation(AggregationBuilders.avg("rateIndexAvg").field("rateIndex"));

            searchSourceBuilder.query(boolQueryBuilder).from(from).size(size).aggregation(appIdAggregationBuilder);

            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("comment")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<TermsAggregation.Entry> termAggregationEntryList = result.getAggregations().getAggregation("sag", TermsAggregation.class).getBuckets();
                    for (TermsAggregation.Entry bucketEntry : termAggregationEntryList) {
                        String key = bucketEntry.getKey();
                        Double avgValue = bucketEntry.getAvgAggregation("rateIndexAvg").getAvg();

                        TopRatedAppsVO topRatedAppsVO = new TopRatedAppsVO();
                        topRatedAppsVO.appId = key;
                        topRatedAppsVO.averageRateIndex = avgValue;

                        ratedAppsVOList.add(topRatedAppsVO);
                    }
                }
            }

            return ratedAppsVOList;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public static class CommentVOCriteria implements Serializable {
        public String id;
        public Long appId;
        public Long modifyTimeForm;
        public Long modifyTimeTo;
        public Long userId;
        public String appPackageName;
        public String commentText;
        public Long osTypeId;
        public String osTypeName;
        public String osVersion;
        public Integer fromRateIndex;
        public Integer toRateIndex;
        public String userName;
        public Long appPackageId;
        public String appTitle;
        public String appPackageVersionCode;
        public String appPackageVersionName;
        public String language;
        public Boolean approved;
    }

    public static class LikeVOCriteria implements Serializable {
        public String parentCommentId;
        public Boolean like;
        public Long userId;
        public String userName;
        public DateTime[] modifyDateTime;
        public DislikeEnum dislikeEnum;
    }

    public static class ReportVOCriteria implements Serializable {
        String parentCommentId;
        String reportText;
        Long userId;
        String userName;
    }

    public void applyApproveVOCriteria(ApproveVOCriteria approveVOCriteria, BoolQueryBuilder boolQueryBuilder) {
        if (approveVOCriteria.id != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("id", approveVOCriteria.id));
        }

        if (approveVOCriteria.parentCommentId != null) {
            boolQueryBuilder.must(
                    QueryBuilders.parentId("comment", approveVOCriteria.parentCommentId)
            );
        }

        if (approveVOCriteria.approveDateFrom != null || approveVOCriteria.approveDateTo != null) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("lastModifyDate");

            if (approveVOCriteria.approveDateFrom != null)
                rangeQueryBuilder.gte(approveVOCriteria.approveDateFrom);
            if (approveVOCriteria.approveDateTo != null)
                rangeQueryBuilder.lte(approveVOCriteria.approveDateTo);

            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        if (approveVOCriteria.userId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("userId", approveVOCriteria.userId));
        }

        if (approveVOCriteria.userName != null && !approveVOCriteria.userName.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("userName", approveVOCriteria.userName));
        }
    }

    public void applyCommentVOCriteria(CommentVOCriteria commentVOCriteria, BoolQueryBuilder boolQueryBuilder) {
        if (commentVOCriteria.modifyTimeForm != null || commentVOCriteria.modifyTimeTo != null) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("lastModifyDate");

            if (commentVOCriteria.modifyTimeForm != null)
                rangeQueryBuilder.gte(commentVOCriteria.modifyTimeForm);
            if (commentVOCriteria.modifyTimeTo != null)
                rangeQueryBuilder.lt(commentVOCriteria.modifyTimeTo);

            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        if (commentVOCriteria.id != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("_id", commentVOCriteria.id));
        }
        if (commentVOCriteria.appId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appId", commentVOCriteria.appId));
        }

        if (commentVOCriteria.appPackageId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appPackageId", commentVOCriteria.appPackageId));
        }

        if (commentVOCriteria.appPackageName != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appPackageName", commentVOCriteria.appId));
        }

        if (commentVOCriteria.userId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("userId", commentVOCriteria.userId));
        }

        if (commentVOCriteria.commentText != null && !commentVOCriteria.commentText.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("commentText", commentVOCriteria.commentText));
        }

        if (commentVOCriteria.osTypeId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("osTypeId", commentVOCriteria.osTypeId));
        }

        if (commentVOCriteria.osTypeName != null && !commentVOCriteria.osTypeName.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("osTypeName", commentVOCriteria.osTypeName));
        }

        if (commentVOCriteria.osVersion != null && !commentVOCriteria.osVersion.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("osVersion", commentVOCriteria.osVersion));
        }

        if (commentVOCriteria.osVersion != null && !commentVOCriteria.osVersion.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("osVersion", commentVOCriteria.osVersion));
        }

        if (commentVOCriteria.fromRateIndex != null || commentVOCriteria.toRateIndex != null) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("rateIndex");

            if (commentVOCriteria.fromRateIndex != null)
                rangeQueryBuilder.gte(commentVOCriteria.fromRateIndex);
            if (commentVOCriteria.toRateIndex != null)
                rangeQueryBuilder.lte(commentVOCriteria.toRateIndex);

            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        if (commentVOCriteria.userName != null && !commentVOCriteria.userName.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("userName", commentVOCriteria.userName));
        }

        if (commentVOCriteria.appTitle != null && !commentVOCriteria.appTitle.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appTitle", commentVOCriteria.appTitle));
        }

        if (commentVOCriteria.appPackageVersionCode != null && !commentVOCriteria.appPackageVersionCode.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appPackageVersionCode", commentVOCriteria.appPackageVersionCode));
        }

        if (commentVOCriteria.appPackageVersionName != null && !commentVOCriteria.appPackageVersionName.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("appPackageVersionName", commentVOCriteria.appPackageVersionName));
        }

        if (commentVOCriteria.language != null && !commentVOCriteria.language.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("language", commentVOCriteria.language));
        }

        if (commentVOCriteria.approved != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("approved", commentVOCriteria.approved));
        }
    }

    public static class DisLikeResultClass {
        public Long count;
        public Boolean disLikedByUser = false;
        public Map<Integer, Long> disLikeCategory2NumberMap;
    }

    public Map<String, DisLikeResultClass> getDisLikeCategoryCountForCommentList(List<String> commentIdList, String userId) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            for (String commentId : commentIdList) {
                BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();
                boolQueryBuilder1.must(QueryBuilders.hasParentQuery("comment", QueryBuilders.termQuery("_id", commentId), true));
                boolQueryBuilder1.must(QueryBuilders.matchQuery("like", false));
                boolQueryBuilder.should(
                        boolQueryBuilder1
                );
            }

            AggregationBuilder bucketCountAggregationBuilder = AggregationBuilders.terms("dislikes").field("_parent");
            bucketCountAggregationBuilder.subAggregation(AggregationBuilders.terms("dislikeCategory").field("dislikeCategory"));

            if (userId != null) {
                AggregationBuilder disLikedByUserAggregation = AggregationBuilders.filter("disLikedByUserAgg", QueryBuilders.matchQuery("userId", userId));
                bucketCountAggregationBuilder.subAggregation(disLikedByUserAggregation);
            }

            searchSourceBuilder.query(boolQueryBuilder).aggregation(bucketCountAggregationBuilder);

            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("commentLike")
                    .build();

            SearchResult result = jestClient.execute(search);

            List<TermsAggregation.Entry> entryList =
                    result.getAggregations().getAggregation("dislikes", TermsAggregation.class).getBuckets();

            Map<String, DisLikeResultClass> retMap = new HashMap<>();

            for (TermsAggregation.Entry entry : entryList) {
                DisLikeResultClass disLikeResultClass = new DisLikeResultClass();

                List<TermsAggregation.Entry> disLikeCategoryEntryList =
                        entry.getAggregation("dislikeCategory", TermsAggregation.class).getBuckets();

                FilterAggregation filterAggregation =
                        entry.getAggregation("disLikedByUserAgg", FilterAggregation.class);

                Map<Integer, Long> dislikeCat2CountMap = new HashMap<>();
                for (TermsAggregation.Entry disLikeEntry : disLikeCategoryEntryList) {
                    dislikeCat2CountMap.put(Integer.parseInt(disLikeEntry.getKey()), disLikeEntry.getCount());
                }

                disLikeResultClass.count = entry.getCount();

                disLikeResultClass.disLikeCategory2NumberMap = dislikeCat2CountMap;

                if (filterAggregation != null) {
                    disLikeResultClass.disLikedByUser = filterAggregation.getCount() <= 0 ? false : true;
                }

                retMap.put(entry.getKey(), disLikeResultClass);
            }

            return retMap;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public static class LikeResultClass {
        public Long count;
        public Boolean likedByUser = false;
    }

    public Map<String, LikeResultClass> getLikeCountForCommentList(List<String> commentIdList, String userId) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            for (String commentId : commentIdList) {
                BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();
                boolQueryBuilder1.must(QueryBuilders.hasParentQuery("comment", QueryBuilders.termQuery("_id", commentId), true));
                boolQueryBuilder1.must(QueryBuilders.matchQuery("like", true));
                boolQueryBuilder.should(
                        boolQueryBuilder1
                );
            }

            AggregationBuilder bucketCountAggregationBuilder = AggregationBuilders.terms("likes").field("_parent");
            searchSourceBuilder.query(boolQueryBuilder).aggregation(bucketCountAggregationBuilder);

            if (userId != null) {
                AggregationBuilder likedByUserAggregation = AggregationBuilders.filter("likedByUserAgg", QueryBuilders.matchQuery("userId", userId));
                bucketCountAggregationBuilder.subAggregation(likedByUserAggregation);
            }

            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("commentLike")
                    .build();

            SearchResult result = jestClient.execute(search);

            Map<String, LikeResultClass> retMap = new HashMap<>();

            List<TermsAggregation.Entry> likesEntryList =
                    result.getAggregations().getAggregation("likes", TermsAggregation.class).getBuckets();


            for (TermsAggregation.Entry entry : likesEntryList) {
                LikeResultClass likeResultClass = new LikeResultClass();
                FilterAggregation filterAggregation =
                        entry.getAggregation("likedByUserAgg", FilterAggregation.class);
                if (filterAggregation != null) {
                    Long likedByUserCount = filterAggregation.getCount();
                    likeResultClass.likedByUser = likedByUserCount <= 0 ? false : true;
                }
                likeResultClass.count = entry.getCount();

                retMap.put(entry.getKey(), likeResultClass);
            }

            return retMap;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }


    public void applyLikeVOCriteria(LikeVOCriteria likeVOCriteria, BoolQueryBuilder boolQueryBuilder) {
        if (likeVOCriteria.parentCommentId != null) {
            boolQueryBuilder.must(
                    QueryBuilders.hasParentQuery("comment", QueryBuilders.termQuery("_id", likeVOCriteria.parentCommentId), true)
            );
        }

        if (likeVOCriteria.userId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("userId", likeVOCriteria.userId));
        }

        if (likeVOCriteria.userName != null && !likeVOCriteria.userName.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("userName", likeVOCriteria.userName));
        }

        if (likeVOCriteria.like != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("like", likeVOCriteria.like));
        }

        if (likeVOCriteria.modifyDateTime != null) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("modifyDateTime");

            if (likeVOCriteria.modifyDateTime[0] != null)
                rangeQueryBuilder.gte(likeVOCriteria.modifyDateTime[0].toDate().getTime());
            if (likeVOCriteria.modifyDateTime[1] != null)
                rangeQueryBuilder.lte(likeVOCriteria.modifyDateTime[1].toDate().getTime());

            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        if (likeVOCriteria.dislikeEnum != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("dislikeCategory", likeVOCriteria.dislikeEnum.getState()));
        }
    }

    public void applyReportVOCriteria(ReportVOCriteria reportVOCriteria, BoolQueryBuilder boolQueryBuilder) {
        if (reportVOCriteria.parentCommentId != null) {
            boolQueryBuilder.must(
                    QueryBuilders.parentId("comment", reportVOCriteria.parentCommentId)
            );
        }

        if (reportVOCriteria.reportText != null && !reportVOCriteria.reportText.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("reportText", reportVOCriteria.reportText));
        }

        if (reportVOCriteria.userId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("userId", reportVOCriteria.userId));
        }

        if (reportVOCriteria.userName != null && !reportVOCriteria.userName.trim().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("userName", reportVOCriteria.userName));
        }
    }

    public Long getCommentsCountForApp(CommentVOCriteria commentVOCriteria) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            List<ElasticCommentVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyCommentVOCriteria(commentVOCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("comment")
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

    public List<ElasticCommentVO> getCommentsForApp(CommentVOCriteria commentVOCriteria, int from, int size, String sortProp, boolean isAsc) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            List<ElasticCommentVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(from);
            if (size != -1)
                searchSourceBuilder.size(size);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyCommentVOCriteria(commentVOCriteria, boolQueryBuilder);

            if (sortProp != null)
                searchSourceBuilder.sort(sortProp, isAsc ? SortOrder.ASC : SortOrder.DESC);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("comment")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<SearchResult.Hit<ElasticCommentVO, Void>> tempResultList = result.getHits(ElasticCommentVO.class);

                    for (SearchResult.Hit<ElasticCommentVO, Void> tempResult : tempResultList) {
                        ElasticCommentVO elasticCommentVO = tempResult.source;
                        elasticCommentVO.setId(tempResult.id);
                        retList.add(tempResult.source);
                    }
                }
            } else {
                throw new IOException(result.getErrorMessage());
            }
            for (ElasticCommentVO elasticCommentVO : retList) {
                if (elasticCommentVO.getCommentId() == null || elasticCommentVO.getCommentId().trim().equals("")) {
                    elasticCommentVO.setCommentId(elasticCommentVO.getId());
                }
            }
            return retList;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }


    public Double getAppRateAverage(CommentVOCriteria commentVOCriteria, int from, int size, String sortProp, boolean isAsc) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyCommentVOCriteria(commentVOCriteria, boolQueryBuilder);

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

    public Long getLikesCountForComment(LikeVOCriteria likeVOCriteria) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyLikeVOCriteria(likeVOCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("commentLike")
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

    public List<ElasticCommentLikeVO> getLikesForComment(LikeVOCriteria likeVOCriteria, int from, int size, boolean getReportCount) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();

        try {
            List<ElasticCommentLikeVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(from).size(size);

            if (size != -1)
                searchSourceBuilder.size(size);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyLikeVOCriteria(likeVOCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("commentLike")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<SearchResult.Hit<ElasticCommentLikeVO, java.lang.Void>> tempResultList = result.getHits(ElasticCommentLikeVO.class);

                    for (SearchResult.Hit<ElasticCommentLikeVO, java.lang.Void> tempResult : tempResultList) {
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

    public Long getReportsCountForComment(ReportVOCriteria reportVOCriteria) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyReportVOCriteria(reportVOCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("commentReport")
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

    public List<ElasticCommentReportVO> getReportsForComment(ReportVOCriteria reportVOCriteria, int from, int size) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();

        try {
            List<ElasticCommentReportVO> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(from).size(size);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyReportVOCriteria(reportVOCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("commentReport")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<SearchResult.Hit<ElasticCommentReportVO, java.lang.Void>> tempResultList = result.getHits(ElasticCommentReportVO.class);

                    for (SearchResult.Hit<ElasticCommentReportVO, java.lang.Void> tempResult : tempResultList) {
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

    public boolean isCommentPresent(String commentId) {
        CommentVOCriteria commentVOCriteria = new CommentVOCriteria();
        commentVOCriteria.id = commentId;

        try {
            Integer deviceCount = getCommentsCountForApp(commentVOCriteria).intValue();
            if (deviceCount > 0)
                return true;
        } catch (Exception ex) {
            logger.error("error occured in getting device count ", ex);
        }
        return false;
    }

    public JestResult approveComment(String commentId, ElasticApproveVO elasticApproveVO) throws Exception {
        if (elasticApproveVO.getApproved() == null)
            throw new Exception("approveStatus is not specified!");

        JestClient jestClient = JestService.Instance.getJestClient();

        try {

            CommentVOCriteria commentVOCriteria = new CommentVOCriteria();
            commentVOCriteria.id = commentId;

            List<ElasticCommentVO> commentVOList = getCommentsForApp(commentVOCriteria, 0, 1, null, true);
            if (commentVOList == null)
                throw new Exception("no such comment with id " + commentId + " exists!");

            Boolean approveStatus = elasticApproveVO.isApproved;

            ElasticCommentVO intendedCommentVO = commentVOList.get(0);
            intendedCommentVO.setApproved(approveStatus);

            Index index;
            index = new Index.Builder(intendedCommentVO).index(indexName).type("comment").id(commentId).build();

            insertApproveCommentHistory(elasticApproveVO);

            JestResult jestResult = jestClient.execute(index);
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public static class HistogramBucketVO {
        public DateTime time;
        public Long count;
        public Double avgRate;
    }

    public List<HistogramBucketVO> getRateHistogram(CommentVOCriteria commentVOCriteria, DateHistogramInterval dateHistogramInterval) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();

        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            applyCommentVOCriteria(commentVOCriteria, boolQueryBuilder);

            DateHistogramAggregationBuilder date_histogram_aggregation = AggregationBuilders.dateHistogram("date_histogram");
            date_histogram_aggregation.field("lastModifyDate")
                    .minDocCount(1l).format("yyyy-MM-dd")
                    .dateHistogramInterval(dateHistogramInterval);
            searchSourceBuilder.query(boolQueryBuilder).aggregation(AggregationBuilders.terms("groupByDate").field("lastModifyDate").
                    subAggregation(AggregationBuilders.avg("rateAverage").field("rateIndex")));

            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("comment")
                    .build();

            SearchResult result = jestClient.execute(search);

            List<HistogramBucketVO> bucketVOList = new ArrayList<>();


            HashMap<Long, Double> avgRateInSelectedDate = new HashMap<>();

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {

                    List<HistogramAggregation.Histogram> histogramList = result.getAggregations().getAggregation("groupByDate", HistogramAggregation.class).getBuckets();
                    JsonObject jsonObject = result.getJsonObject();//                    JsonObject jsonObject = result.getJsonObject();
                    if (jsonObject.has("aggregations")) {
                        JsonElement jsonElement = jsonObject.get("aggregations");
                        String jsonString = jsonElement.toString();
                        JSONObject rateAverageJsonObject = new JSONObject(jsonString);
                        if (rateAverageJsonObject.has("groupByDate")) {
                            JSONObject groupByDateJsonObject = rateAverageJsonObject.getJSONObject("groupByDate");
                            if (groupByDateJsonObject.has("buckets")) {
                                JSONArray buckets = (JSONArray) groupByDateJsonObject.get("buckets");
                                for (Object object : buckets) {
                                    JSONObject bucketJson = new JSONObject(object.toString());
                                    Long key = null;
                                    Double avgRate = null;
                                    if (bucketJson.has("rateAverage")) {
                                        JSONObject valueJsonObject = bucketJson.getJSONObject("rateAverage");
                                        if (valueJsonObject.has("value")) {
                                            avgRate = Double.valueOf(valueJsonObject.getDouble("value"));
                                        }
                                    }
                                    if (bucketJson.has("key")) {
                                        key = Long.valueOf(bucketJson.get("key").toString());
                                        avgRateInSelectedDate.put(key, avgRate);
                                    }

                                }
                            }
                        }
                    }
                    for (HistogramAggregation.Histogram histogram : histogramList) {
                        HistogramBucketVO histogramRateBucketVO = new HistogramBucketVO();

                        Long dateKey = histogram.getKey();
                        Long histCount = histogram.getCount();

                        Date date = new Date(dateKey);
                        DateTime dateTime = new DateTime(date);

                        histogramRateBucketVO.time = dateTime;
                        histogramRateBucketVO.count = histCount;
                        histogramRateBucketVO.avgRate = avgRateInSelectedDate.get(dateKey);

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

    public List<HistogramBucketVO> getLikeHistogram(LikeVOCriteria likeVOCriteria, DateHistogramInterval dateHistogramInterval) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            applyLikeVOCriteria(likeVOCriteria, boolQueryBuilder);

            DateHistogramAggregationBuilder date_histogram_aggregation = AggregationBuilders.dateHistogram("date_histogram");
            date_histogram_aggregation.field("modifyDateTime")
                    .minDocCount(1l).format("yyyy-MM-dd")
                    .dateHistogramInterval(dateHistogramInterval);
            date_histogram_aggregation.order(Histogram.Order.COUNT_DESC);

            searchSourceBuilder.query(boolQueryBuilder).aggregation(date_histogram_aggregation);

            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("commentLike")
                    .build();

            SearchResult result = jestClient.execute(search);

            List<HistogramBucketVO> bucketVOList = new ArrayList<>();

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<HistogramAggregation.Histogram> histogramList = result.getAggregations().getAggregation("date_histogram", HistogramAggregation.class).getBuckets();
                    for (HistogramAggregation.Histogram histogram : histogramList) {
                        HistogramBucketVO histogramRateBucketVO = new HistogramBucketVO();

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

    public ElasticApproveVO buildApproveVoByElasticCommentVo(ElasticCommentVO elasticCommentVO) {
        ElasticApproveVO elasticApproveVO = new ElasticApproveVO();
        if (elasticCommentVO != null) {
            elasticApproveVO.setParentCommentId(elasticCommentVO.getId());
            User currentUser = PrincipalUtil.getCurrentUser();
            elasticApproveVO.setUserId(currentUser.getUserId());
            elasticApproveVO.setUserName(currentUser.getUserName());
            elasticApproveVO.setApproved(elasticCommentVO.getApproved());
            elasticApproveVO.setApproveDate(DateTime.now().getTime());
        }
        return elasticApproveVO;
    }

    public static void main(String[] args) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        OSType osType = OSTypeService.Instance.getOSTypeByName("ANDROID", session);
        App app = AppService.Instance.getAppByPackageName(osType, "io.cordova.cartable", false, session);
////
//        DeviceElasticService.DeviceMetaData deviceMetaData = new DeviceElasticService.DeviceMetaData();
//        deviceMetaData.setSerialNumber("22231dfg");
//        deviceMetaData.setRAM("12G");
//        deviceMetaData.setCpuModel("ARM 2011");
//        deviceMetaData.setDeviceClass("ANDROID");
//        deviceMetaData.setManufacturer("SAMSUNG");
//        deviceMetaData.setProductName("dffddf");
//        deviceMetaData.setScreenDensityDpi("218");
//        deviceMetaData.setScreenHeight("36");
//        deviceMetaData.setScreenWidth("12");
//        DeviceElasticService.Instance.insertDeviceMetaData(deviceMetaData);
//

        ElasticCommentVO elasticCommentVO =
                Instance.buildCommentVOForElastic(null, app, "54545666754", "salam sag!", "EN", DateUtils.addDays(new Date(), -5).getTime(), 22l, RatingIndex.bad, false);
        elasticCommentVO.setOsTypeId(osType.getId());
////
        JestResult jestResult = Instance.insertCommentForAppMainPackage(elasticCommentVO);

//        ElasticCommentLikeVO elasticCommentLikeVO = new ElasticCommentLikeVO();
//        elasticCommentLikeVO.setLike(true);
//        elasticCommentLikeVO.setParentCommentId("AV5GsUO5Wby20ugy7rG4");
//        elasticCommentLikeVO.setUserId(22l);
//        elasticCommentLikeVO.setUserName("admin");
//        JestResult jestResult1 = Instance.insertLikeForComment(elasticCommentLikeVO);

//        ElasticApproveVO elasticApproveVO = new ElasticApproveVO();
//        elasticApproveVO.setParentCommentId("AV4Eo7XMHPDFedfwerfwerf8cACWXN-");
//        elasticApproveVO.setUserId(22l);
//        elasticApproveVO.setApproveDate(new DateTime().getTime());
//        elasticApproveVO.setUserName("admin");
//        elasticApproveVO.setApproved(true);
//
//        Instance.approveComment("AV4JOAg6Wby20ugy7rF3", elasticApproveVO);

//        CommentVOCriteria commentVOCriteria = new CommentVOCriteria();
//        commentVOCriteria.appId = 1062l;
//        Double rateAverage = Instance.getAppRateAverage(commentVOCriteria);

//        List<TopRatedAppsVO> appsVOList = Instance.getTopRatedApps(2, false);
//
//        System.out.println("");

//        CommentVOCriteria commentVOCriteria = new CommentVOCriteria();
//        commentVOCriteria.appId = 1020l;
//        Instance.getCommentsForApp(commentVOCriteria, 0, 10, null, true);
//
//
//        ApproveVOCriteria approveVOCriteria = new ApproveVOCriteria();
//        Instance.getApproveCommentCountHistory(approveVOCriteria);

//        ElasticCommentLikeVO elasticCommentLikeVO = new ElasticCommentLikeVO();
//        elasticCommentLikeVO.setLike(true);
//        elasticCommentLikeVO.setParentCommentId("AV56B0Ry8t1XZ99E-e7R");
//        elasticCommentLikeVO.setUserId(29l);
//        elasticCommentLikeVO.setUserName("hamid");
//        elasticCommentLikeVO.setDislikeCategory(9);
//        Instance.insertLikeForComment(elasticCommentLikeVO);


//        Instance.deleteCommentLike("AV56A7su8t1XZ99E-e7Q", 23l);

//        Instance.getDisLikeCategoryCountForCommentList(Arrays.asList("AV56B0Ry8t1XZ99E-e7R"), "28");

//        ElasticCommentLikeVO elasticCommentLikeVO = new ElasticCommentLikeVO();
//        elasticCommentLikeVO.setLike(false);
//        elasticCommentLikeVO.setDislikeCategory(0);
//        elasticCommentLikeVO.setParentCommentId("AV6LU6ww8t1XZ99E-e7n");
//        elasticCommentLikeVO.setUserId(26l);
//        elasticCommentLikeVO.setUserName("hamid3");
//        elasticCommentLikeVO.setModifyDateTime(new Date().getTime());
//        Instance.insertLikeForComment(elasticCommentLikeVO);

//        LikeVOCriteria likeVOCriteria = new LikeVOCriteria();
//        likeVOCriteria.like = true;
//        Instance.getLikeHistogram(likeVOCriteria, DateHistogramInterval.MONTH);

//        RateHistogramCriteria rateHistogramCriteria = new RateHistogramCriteria();
//
//        Instance.getRateHistogram(rateHistogramCriteria, DateHistogramInterval.MONTH);


        System.exit(0);
    }
}
