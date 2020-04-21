package com.fanap.midhco.appstore.service.timeLine;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.TimeLineFileType;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.vos.FileVO;
import com.fanap.midhco.appstore.restControllers.vos.OrganizationVO;
import com.fanap.midhco.appstore.restControllers.vos.TimeLineVO;
import com.fanap.midhco.appstore.service.engine.EngineOrganizationService;
import com.fanap.midhco.appstore.service.jest.JestService;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by A.Moshiri on 9/16/2018.
 */
public class TimeLineElasticService {
    public static TimeLineElasticService Instance = new TimeLineElasticService();
    final static String indexName = ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_INDEX_NAME);


    public static class TimeLineCriteria implements Serializable {
        String id;
        String title;
        String organizationId;
        String description;
        String timeLineDescription;
        String creatorUserName;
        String organizationNickName;
        String lastModificationUserName;
        Long creatorUserId;
        Long lastModificationUserId;
        List<Long> organizationIds;

        public DateTime[] startShowTimeLineDateTime = new DateTime[2];
        List<TimeLineFileType> timeLineFileTypes;
        TimeLineFileType timeLineFileType;
        Boolean isActive;
        Boolean showInChild;
        Boolean searchInParents;
        public List<String> keywords;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<TimeLineFileType> getTimeLineFileTypes() {
            return timeLineFileTypes;
        }

        public void setTimeLineFileTypes(List<TimeLineFileType> timeLineFileTypes) {
            this.timeLineFileTypes = timeLineFileTypes;
        }

        public DateTime[] getStartShowTimeLineDateTime() {
            return startShowTimeLineDateTime;
        }

        public void setStartShowTimeLineDateTime(DateTime[] startShowTimeLineDateTime) {
            this.startShowTimeLineDateTime = startShowTimeLineDateTime;
        }

        public String getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(String organizationId) {
            this.organizationId = organizationId;
        }

        public Boolean getActive() {
            return isActive;
        }

        public void setActive(Boolean active) {
            isActive = active;
        }

        public Boolean getShowInChild() {
            return showInChild;
        }

        public void setShowInChild(Boolean showInChild) {
            this.showInChild = showInChild;
        }

        public Boolean getSearchInParents() {
            return searchInParents;
        }

        public void setSearchInParents(Boolean searchInParents) {
            this.searchInParents = searchInParents;
        }

        public TimeLineFileType getTimeLineFileType() {
            return timeLineFileType;
        }

        public void setTimeLineFileType(TimeLineFileType timeLineFileType) {
            this.timeLineFileType = timeLineFileType;
        }

        public String getCreatorUserName() {
            return creatorUserName;
        }

        public void setCreatorUserName(String creatorUserName) {
            this.creatorUserName = creatorUserName;
        }

        public String getLastModificationUserName() {
            return lastModificationUserName;
        }

        public void setLastModificationUserName(String lastModificationUserName) {
            this.lastModificationUserName = lastModificationUserName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getCreatorUserId() {
            return creatorUserId;
        }

        public void setCreatorUserId(Long creatorUserId) {
            this.creatorUserId = creatorUserId;
        }

        public Long getLastModificationUserId() {
            return lastModificationUserId;
        }

        public void setLastModificationUserId(Long lastModificationUserId) {
            this.lastModificationUserId = lastModificationUserId;
        }

        public List<Long> getOrganizationIds() {
            return organizationIds;
        }

        public void setOrganizationIds(List<Long> organizationIds) {
            this.organizationIds = organizationIds;
        }

        public String getTimeLineDescription() {
            return timeLineDescription;
        }

        public void setTimeLineDescription(String timeLineDescription) {
            this.timeLineDescription = timeLineDescription;
        }

        public String getOrganizationNickName() {
            return organizationNickName;
        }

        public void setOrganizationNickName(String organizationNickName) {
            this.organizationNickName = organizationNickName;
        }

        public List<String> getKeywords() {
            return keywords;
        }

        public void setKeywords(List<String> keywords) {
            this.keywords = keywords;
        }
    }

    public JestResult insertTimeLine(TimeLineVO timeLineVO) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        JestClient jestClient = JestService.Instance.getJestClient();

        try {
            Index index;
            if (timeLineVO.getId() != null && !timeLineVO.getId().trim().equals("")) {
                index = new Index.Builder(timeLineVO).index(indexName).type("timeLine").id(timeLineVO.getId()).build();
            } else {
                index = new Index.Builder(timeLineVO).index(indexName).type("timeLine").build();
            }
            JestResult jestResult = jestClient.execute(index);
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public long count(TimeLineCriteria timeLineCriteria, Integer first, Integer count, String sortParam, boolean isAsc) throws Exception {
        TimeLineCriteria tmpTimeLineCriteria = timeLineCriteria;
        SearchResult result = getSearchResultForTimeLineByTimeLineCriteria(tmpTimeLineCriteria, first, count, sortParam, isAsc);

        if (result.isSucceeded()) {
            long resultLong = result.getTotal();

            if (tmpTimeLineCriteria != null && tmpTimeLineCriteria.getSearchInParents() != null && tmpTimeLineCriteria.getSearchInParents()) {
                if (tmpTimeLineCriteria.getOrganizationIds() != null) {
                    Map<Long, OrganizationVO> organizationVOMap = new HashMap<>();
                    Map<String, OrganizationVO> organizationVoNameMap = new HashMap<>();
                    List<OrganizationVO> allOrganizations = EngineOrganizationService.Instance.getAllOrganization(organizationVOMap, organizationVoNameMap);
                    if (allOrganizations == null) {
                        return Long.valueOf(-1);
                    }
                    tmpTimeLineCriteria.setSearchInParents(false);
                    setRelatedIdToCriteriaDependOnItsOrganizationName(tmpTimeLineCriteria, organizationVoNameMap);
                    tmpTimeLineCriteria.setOrganizationNickName(null);
                    for (Long childOrganizationId : tmpTimeLineCriteria.getOrganizationIds()) {
                        List<Long> parentIds = new ArrayList<>();
                        OrganizationVO chileOrganizationVO = organizationVOMap.get(childOrganizationId);
                        while (chileOrganizationVO != null && chileOrganizationVO.getParent() != null && chileOrganizationVO.getParent().getId() != Long.valueOf(-1)) {
                            parentIds.add(chileOrganizationVO.getParent().getId());
                            chileOrganizationVO = chileOrganizationVO.getParent();
                        }
                        tmpTimeLineCriteria.setOrganizationIds(parentIds);
                        tmpTimeLineCriteria.setShowInChild(true);
                        resultLong += count(tmpTimeLineCriteria, first, count, sortParam, isAsc);
                    }
                }
            }


            return resultLong;
        } else {
            throw new IOException(result.getErrorMessage());
        }
    }

    private void applyTimeLinetCriteria(TimeLineCriteria timeLineCriteria, BoolQueryBuilder boolQueryBuilder) {
        try {
            List<QueryBuilder> queryBuilderList = new ArrayList<>();

            if (timeLineCriteria.id != null) {
                boolQueryBuilder.must().add(QueryBuilders.matchQuery("id", timeLineCriteria.id));
            }
            if (timeLineCriteria.creatorUserName != null && !timeLineCriteria.creatorUserName.trim().isEmpty()) {
                boolQueryBuilder.must().add(QueryBuilders.matchQuery("creatorUserName", timeLineCriteria.creatorUserName));
            }

            Boolean hasSpecificId = false;
            try {
                if (timeLineCriteria.organizationId != null && !timeLineCriteria.organizationId.trim().isEmpty() && !timeLineCriteria.organizationId.trim().equals("[]")) {
                    Long organizationId = Long.parseLong(timeLineCriteria.organizationId);
                    boolQueryBuilder.must().add(QueryBuilders.matchQuery("organizationId", organizationId));
                    hasSpecificId = true;
                }
            } catch (Exception e) {
                hasSpecificId = false;
            }
            if (!hasSpecificId && timeLineCriteria.organizationIds != null && !timeLineCriteria.organizationIds.isEmpty()) {
                BoolQueryBuilder organizationBoolQueryBuilder = new BoolQueryBuilder();
                for (long orgId : timeLineCriteria.organizationIds) {
                    QueryBuilder organizationQueryBuilder = QueryBuilders.matchQuery("organizationId", String.valueOf(orgId));
                    organizationBoolQueryBuilder.should().add(organizationQueryBuilder);
                }

                queryBuilderList.add(organizationBoolQueryBuilder);
            }

            if (timeLineCriteria.lastModificationUserName != null && !timeLineCriteria.lastModificationUserName.trim().isEmpty()) {
                boolQueryBuilder.must().add(QueryBuilders.matchQuery("lastModificationUserName", timeLineCriteria.lastModificationUserName));
            }

            if (timeLineCriteria.organizationNickName != null && !timeLineCriteria.organizationNickName.trim().isEmpty()) {
                boolQueryBuilder.must().add(QueryBuilders.matchQuery("organizationNickName", timeLineCriteria.organizationNickName));
            }

            if (timeLineCriteria.title != null && !timeLineCriteria.title.isEmpty()) {
                StringBuffer regexTitle = new StringBuffer();
                regexTitle.append(".*").append(timeLineCriteria.title).append(".*");
                boolQueryBuilder.must().add(QueryBuilders.regexpQuery("title", regexTitle.toString()));
            }


            if (timeLineCriteria.timeLineDescription != null && !timeLineCriteria.timeLineDescription.isEmpty()) {
                StringBuffer regexTimeLineDescription = new StringBuffer();
                regexTimeLineDescription.append(".*").append(timeLineCriteria.timeLineDescription).append(".*");
                boolQueryBuilder.must().add(QueryBuilders.regexpQuery("timeLineDescription", regexTimeLineDescription.toString()));
            }

            if (timeLineCriteria.startShowTimeLineDateTime != null && timeLineCriteria.startShowTimeLineDateTime[1] != null && !timeLineCriteria.startShowTimeLineDateTime[1].equals(DateTime.MIN_DATE_TIME)) {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("startShowTimeLine");

                if (timeLineCriteria.startShowTimeLineDateTime[0] != null)
                    rangeQueryBuilder.gte(timeLineCriteria.startShowTimeLineDateTime[0].getTime());
                if (timeLineCriteria.startShowTimeLineDateTime[1] != null)
                    rangeQueryBuilder.lte(timeLineCriteria.startShowTimeLineDateTime[1].getTime());

                boolQueryBuilder.filter().add(rangeQueryBuilder);
            }

            if (timeLineCriteria.creatorUserId != null) {
                boolQueryBuilder.must().add(QueryBuilders.matchQuery("creatorUserId", timeLineCriteria.creatorUserId));
            }
            if (timeLineCriteria.lastModificationUserId != null) {
                boolQueryBuilder.must().add(QueryBuilders.matchQuery("lastModificationUserId", timeLineCriteria.lastModificationUserId));
            }
            if (timeLineCriteria.timeLineFileType != null) {
                boolQueryBuilder.must().add(QueryBuilders.matchQuery("fileType", timeLineCriteria.timeLineFileType.getState()));
            } else if (timeLineCriteria.timeLineFileTypes != null && !timeLineCriteria.timeLineFileTypes.isEmpty()) {

                BoolQueryBuilder fileTypeBoolQueryBuilder = new BoolQueryBuilder();
                for (TimeLineFileType fileType : timeLineCriteria.getTimeLineFileTypes()) {
                    QueryBuilder fileTypeQueryBuilder = QueryBuilders.matchQuery("fileType", fileType.getState().toString());
                    fileTypeBoolQueryBuilder.should().add(fileTypeQueryBuilder);
                }
                queryBuilderList.add(fileTypeBoolQueryBuilder);
            }

            if (timeLineCriteria.getActive() != null) {
                boolQueryBuilder.must().add(QueryBuilders.matchQuery("isActive", timeLineCriteria.isActive));
            }

            if (timeLineCriteria.getShowInChild() != null) {
                boolQueryBuilder.must().add(QueryBuilders.matchQuery("showInChild", timeLineCriteria.showInChild));
            } else {
                BoolQueryBuilder showInChildBoolQueryBuilder = new BoolQueryBuilder();
                QueryBuilder firstQueryBuilder = QueryBuilders.matchQuery("showInChild", "null");
                showInChildBoolQueryBuilder.should().add(firstQueryBuilder);

                QueryBuilder secondQueryBuilder = QueryBuilders.matchQuery("showInChild", false);
                showInChildBoolQueryBuilder.should().add(secondQueryBuilder);

                queryBuilderList.add(showInChildBoolQueryBuilder);
            }

            if (timeLineCriteria.keywords != null && !timeLineCriteria.keywords.isEmpty()) {
                BoolQueryBuilder keywordBoolQueryBuilder = new BoolQueryBuilder();
                for (String keyword : timeLineCriteria.getKeywords()) {
                    QueryBuilder keywordQueryBuilder = QueryBuilders.matchQuery("keywords", keyword);
                    keywordBoolQueryBuilder.should().add(keywordQueryBuilder);
                }
                queryBuilderList.add(keywordBoolQueryBuilder);
            }


            if (!queryBuilderList.isEmpty()) {
                boolQueryBuilder.must().addAll(queryBuilderList);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public List<TimeLineVO> searchTimeLine(TimeLineCriteria timeLineCriteria, int first, int count, String sortParam, boolean ascending) throws Exception {

        SearchResult result = getSearchResultForTimeLineByTimeLineCriteria(timeLineCriteria, first, count, sortParam, ascending);
        List<TimeLineVO> retList = new ArrayList<>();

        try {
            addResultsToTimeLineList(result, retList);
        } catch (IOException e) {
            throw e;
        }

        if (timeLineCriteria != null && timeLineCriteria.getSearchInParents() != null && timeLineCriteria.getSearchInParents()) {
            Map<Long, OrganizationVO> organizationVOMap = new HashMap<>();
            Map<String, OrganizationVO> organizationVoNameMap = new HashMap<>();
            List<OrganizationVO> allOrganizations = EngineOrganizationService.Instance.getAllOrganization(organizationVOMap, organizationVoNameMap);
            if (allOrganizations == null) {
                throw new Exception("error has occurred in get all Organization");
            }

            setRelatedIdToCriteriaDependOnItsOrganizationName(timeLineCriteria, organizationVoNameMap);

            for (Long childOrganizationId : timeLineCriteria.getOrganizationIds()) {
                List<Long> parentIds = new ArrayList<>();
                OrganizationVO chileOrganizationVO = organizationVOMap.get(childOrganizationId);
                while (chileOrganizationVO != null && chileOrganizationVO.getParent() != null && chileOrganizationVO.getParent().getId() != Long.valueOf(-1)) {
                    parentIds.add(chileOrganizationVO.getParent().getId());
                    chileOrganizationVO = chileOrganizationVO.getParent();
                }
                timeLineCriteria.setOrganizationIds(parentIds);
                timeLineCriteria.setShowInChild(true);
                timeLineCriteria.setOrganizationNickName(null);
                SearchResult resultInLoop = getSearchResultForTimeLineByTimeLineCriteria(timeLineCriteria, first, count, sortParam, ascending);

                try {
                    addResultsToTimeLineList(resultInLoop, retList);
                } catch (IOException e) {
                    throw e;
                }
            }
        }

        String downLoadUrl = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH);
        for (TimeLineVO timeLineVO : retList) {
            if (timeLineVO.getStartShowTimeLine() != null) {
                timeLineVO.setStartDateTime(new DateTime(timeLineVO.getStartShowTimeLine()));
            }
            if (timeLineVO.getFileVOList() != null && !timeLineVO.getFileVOList().isEmpty()) {
                for (FileVO fileVO : timeLineVO.getFileVOList()) {
                    fileVO.setFileKey(downLoadUrl.replace("${key}", fileVO.getFileKey()));
                }
            }
        }
        return retList;

    }

    private void setRelatedIdToCriteriaDependOnItsOrganizationName(TimeLineCriteria timeLineCriteria, Map<String, OrganizationVO> organizationVoNameMap) {
        if (timeLineCriteria.getOrganizationIds() == null || timeLineCriteria.getOrganizationIds().isEmpty()) {
            List<Long> orgId = new ArrayList<>();
            if (timeLineCriteria.getOrganizationNickName() != null && !timeLineCriteria.getOrganizationNickName().trim().equals("")) {
                OrganizationVO organizationVO = organizationVoNameMap.get(timeLineCriteria.getOrganizationNickName());
                if (organizationVO != null) {
                    orgId.add(organizationVO.getId());
                    timeLineCriteria.setOrganizationIds(orgId);
                }
            }
        }
    }

    private void addResultsToTimeLineList(SearchResult result, List<TimeLineVO> retList) throws IOException {
        if (result.isSucceeded()) {
            if (result.getTotal() > 0) {
                List<SearchResult.Hit<TimeLineVO, Void>> tempResultList = result.getHits(TimeLineVO.class);
                for (SearchResult.Hit<TimeLineVO, Void> tempResult : tempResultList) {
                    TimeLineVO appRelevancyRateVO = tempResult.source;
                    appRelevancyRateVO.setId(tempResult.id);
                    retList.add(tempResult.source);
                }
            }
        } else {
            throw new IOException(result.getErrorMessage());
        }
    }

    private SearchResult getSearchResultForTimeLineByTimeLineCriteria(TimeLineCriteria timeLineCriteria, Integer first, Integer count, String sortParam, boolean isAsc) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();

        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            if (first == null) {
                first = 0;
            }
            if (count == null) {
                count = Integer.valueOf(-1);
            }
            searchSourceBuilder.from(first).size(count);

            if (sortParam != null && !sortParam.trim().isEmpty()) {
                searchSourceBuilder.sort(sortParam, isAsc ? SortOrder.ASC : SortOrder.DESC);
            }

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            if (timeLineCriteria != null)
                applyTimeLinetCriteria(timeLineCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex(indexName)
                    .addType("timeLine")
                    .build();

            return jestClient.execute(search);
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public TimeLineCriteria BuildCriteriaByVO(TimeLineVO timeLineVO) {

        if (timeLineVO != null) {
            TimeLineCriteria timeLineCriteria = new TimeLineCriteria();
            timeLineCriteria.setOrganizationId(timeLineVO.getOrganizationId());
            timeLineCriteria.setOrganizationNickName(timeLineVO.getOrganizationNickName());
            timeLineCriteria.setId(timeLineVO.getId());
            timeLineCriteria.setTimeLineDescription(timeLineVO.getTimeLineDescription());
            if (timeLineVO.getFileType() != null) {
                timeLineCriteria.setTimeLineFileType(new TimeLineFileType(timeLineVO.getFileType()));
            }
            if (timeLineVO.getActive() != null) {
                timeLineCriteria.setActive(timeLineVO.getActive());
            }

            if (timeLineVO.getSearchInParents() != null) {
                timeLineCriteria.setSearchInParents(timeLineVO.getSearchInParents());
            }

            DateTime[] timeLineShowDateTime = new DateTime[2];
            DateTime startDateTime = DateTime.MIN_DATE_TIME;
            DateTime endDateTime = DateTime.MAX_DATE_TIME;
            if (timeLineVO.getStartShowTimeLine() != null) {
                startDateTime = new DateTime(timeLineVO.getStartShowTimeLine() - DateTime.ONE_HOUR_MILLIS);
            } else if (timeLineVO.getStartDateTime() != null) {
                startDateTime = new DateTime(timeLineVO.getStartDateTime().getTime() - DateTime.ONE_HOUR_MILLIS);
            }

            if (timeLineVO.getEndShowTimeLine() != null) {
                endDateTime = new DateTime(timeLineVO.getEndShowTimeLine() - DateTime.ONE_HOUR_MILLIS);
            } else if (timeLineVO.getStartDateTime() != null) {
                endDateTime = DateTime.afterFrom(timeLineVO.getStartDateTime(), 1);
            }
            timeLineShowDateTime = bouidDateTimeArray(startDateTime, endDateTime);

            timeLineCriteria.setStartShowTimeLineDateTime(timeLineShowDateTime);

            if (timeLineVO.getTitle() != null && !timeLineVO.getTitle().trim().equals("")) {
                timeLineCriteria.setTitle(timeLineVO.getTitle());
            }

            if (timeLineVO.getKeywords() != null) {
                timeLineCriteria.setKeywords(timeLineVO.getKeywords());
            }

            return timeLineCriteria;

        } else {
            return null;
        }
    }

    private DateTime[] bouidDateTimeArray(DateTime firstDateTime, DateTime secondDateTime) {
        DateTime[] dateTime = new DateTime[2];
        dateTime[0] = firstDateTime;
        dateTime[1] = secondDateTime;

        return dateTime;
    }

}


