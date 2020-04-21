package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.TimeLine;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.timeLine.TimeLineElasticService;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by A.Moshiri on 9/25/2018.
 */
public class TimeLineVO implements Serializable {

    public TimeLineVO(String inputString) {
        JSONObject jsonObject = new JSONObject(inputString);
        try {
            if (jsonObject.has("id")) {
                this.id = jsonObject.getString("id");
            }
            if (jsonObject.has("title")) {
                this.title = jsonObject.getString("title");
            }
            if (jsonObject.has("description")) {
                this.description = jsonObject.getString("description");
            }

            if (jsonObject.has("timeLineDescription")) {
                this.timeLineDescription = jsonObject.getString("timeLineDescription");
            }

            if (jsonObject.has("keywords") && !jsonObject.get("keywords").equals(JSONObject.NULL)) {

                JSONArray keywordsJasonArray = jsonObject.getJSONArray("keywords");
                List<String> keywords = new ArrayList<>();

                for (Object keywordObj : keywordsJasonArray) {

                    if (keywordObj != null && !keywordObj.toString().trim().equals("")) {
                        keywords.add(keywordObj.toString());
                    }
                }
                if (keywords.size() > 0) {
                    this.keywords = new ArrayList<>(keywords);
                }
            }

            if (jsonObject.has("files") && !jsonObject.get("files").equals(JSONObject.NULL)) {

                JSONArray filesJasonArray = jsonObject.getJSONArray("files");
                Set<FileVO> fileVOS = new HashSet<>();

                for (Object fileObj : filesJasonArray) {
                    FileVO fileVO = new FileVO(fileObj.toString());
                    if (fileVO != null) {
                        fileVOS.add(fileVO);
                    }
                }
                if (fileVOS.size() > 0) {
                    this.fileVOList = new ArrayList<>(fileVOS);
                }
            }

            if (jsonObject.has("organizationId")) {
                this.organizationId = jsonObject.getString("organizationId");
            }

            if (jsonObject.has("organizationNickName")) {
                this.organizationNickName = jsonObject.getString("organizationNickName");
            }
            if (jsonObject.has("creatorUserName")) {
                this.creatorUserName = jsonObject.getString("creatorUserName");
            }
            if (jsonObject.has("lastModificationUserName")) {
                this.lastModificationUserName = jsonObject.getString("lastModificationUserName");
            }

            if (jsonObject.has("startShowTimeLine")) {
                this.startShowTimeLine = jsonObject.getLong("startShowTimeLine");
            }


         if (jsonObject.has("endShowTimeLine")) {
                this.endShowTimeLine = jsonObject.getLong("endShowTimeLine");
            }

            if (jsonObject.has("creatorUserId")) {
                this.creatorUserId = jsonObject.getLong("creatorUserId");
            }
            if (jsonObject.has("creationDate")) {
                this.creationDate = jsonObject.getLong("creationDate");
            }
            if (jsonObject.has("lastModificationUserId")) {
                this.lastModificationUserId = jsonObject.getLong("lastModificationUserId");
            }
            if (jsonObject.has("lastModificationDate")) {
                this.lastModificationDate = jsonObject.getLong("lastModificationDate");
            }
            if (jsonObject.has("fileType")) {
                this.fileType = jsonObject.getLong("fileType");
            }

            if (jsonObject.has("isActive")) {
                if (jsonObject.get("isActive") != null) {
                    this.isActive = jsonObject.getBoolean("isActive");
                }
            }


            if (jsonObject.has("searchInParents")) {
                if (jsonObject.get("searchInParents") != null) {
                    this.searchInParents = jsonObject.getBoolean("searchInParents");
                }
            }

        } finally {

        }
    }

    public TimeLineVO() {
    }

    String id;
    String organizationId;
    String title;
    String description;
    String timeLineDescription;
    List<FileVO> fileVOList;
    String organizationNickName;
    String creatorUserName;
    String lastModificationUserName;
    Long startShowTimeLine;
    Long endShowTimeLine;
    Long creatorUserId;
    Long creationDate;
    Long lastModificationUserId;
    Long lastModificationDate;
    Long fileType;
    DateTime startDateTime;
    Boolean isActive;
    Boolean showInChild;
    List<String> keywords;
    Boolean searchInParents;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimeLineDescription() {
        return timeLineDescription;
    }

    public void setTimeLineDescription(String timeLineDescription) {
        this.timeLineDescription = timeLineDescription;
    }

    public List<FileVO> getFileVOList() {
        return fileVOList;
    }

    public void setFileVOList(List<FileVO> fileVOList) {
        this.fileVOList = fileVOList;
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


    public Long getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(Long creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Long getLastModificationUserId() {
        return lastModificationUserId;
    }

    public void setLastModificationUserId(Long lastModificationUserId) {
        this.lastModificationUserId = lastModificationUserId;
    }

    public Long getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public Long getFileType() {
        return fileType;
    }

    public void setFileType(Long fileType) {
        this.fileType = fileType;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationNickName() {
        return organizationNickName;
    }

    public void setOrganizationNickName(String organizationNickName) {
        this.organizationNickName = organizationNickName;
    }

    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(DateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
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


    public Long getStartShowTimeLine() {
        return startShowTimeLine;
    }

    public void setStartShowTimeLine(Long startShowTimeLine) {
        this.startShowTimeLine = startShowTimeLine;
    }

    public Long getEndShowTimeLine() {
        return endShowTimeLine;
    }

    public void setEndShowTimeLine(Long endShowTimeLine) {
        this.endShowTimeLine = endShowTimeLine;
    }

    public static TimeLineVO buildTimeLineVoByTimeLine(TimeLine timeLine) throws Exception {
        if (timeLine != null) {
            TimeLineVO timeLineVO = new TimeLineVO();
            timeLineVO.setTitle(timeLine.getTitle());
            timeLineVO.setId(timeLine.getId());
            if (timeLine.getOrganizationId() != null && !timeLine.getOrganizationId().trim().equals("")) {
                timeLineVO.setOrganizationId(timeLine.getOrganizationId());
            }

            if (timeLine.getOrganizationNickName() != null && !timeLine.getOrganizationNickName().trim().equals("")) {
                timeLineVO.setOrganizationNickName(timeLine.getOrganizationNickName());
            }

            if (timeLine.getActive() != null) {
                timeLineVO.setActive(timeLine.getActive());
            } else {
                timeLine.setActive(false);
            }
            timeLineVO.setCreationDate(timeLine.getCreationDate());
            timeLineVO.setDescription(timeLine.getDescription());
            timeLineVO.setTimeLineDescription(timeLine.getTimeLineDescription());
            if (timeLine.getStartShowTimeLine() != null) {
                timeLineVO.setStartShowTimeLine(timeLine.getStartShowTimeLine());
            } else if (timeLine.getStartDateTime() != null) {
                timeLineVO.setStartShowTimeLine(timeLine.getStartDateTime().getTime());
            }

            if (timeLine.getEndShowTimeLine() != null) {
                timeLineVO.setEndShowTimeLine(timeLine.getEndShowTimeLine());
            } else if (timeLine.getStartDateTime() != null) {
                timeLineVO.setEndShowTimeLine(DateTime.afterFrom(timeLine.getStartDateTime(),1) .getTime());
            }


            if (timeLine.getFileVOList() != null && !timeLine.getFileVOList().isEmpty()) {
                timeLineVO.setFileVOList(timeLine.getFileVOList());
            }

            if (timeLine.getFileType() != null) {
                timeLineVO.setFileType(timeLine.getFileType());
            } else if (timeLine.getTimeLineFileType() != null) {
                timeLineVO.setFileType(timeLine.getTimeLineFileType().getState());
            }
            timeLineVO.setLastModificationDate(timeLine.getLastModificationDate());
            timeLineVO.setCreatorUserId(timeLine.getCreatorUserId());
            timeLineVO.setCreatorUserName(timeLine.getCreatorUserName());
            timeLineVO.setLastModificationUserId(timeLine.getLastModificationUserId());
            timeLineVO.setLastModificationUserName(timeLine.getLastModificationUserName());
            timeLineVO.setStartDateTime(timeLine.getStartDateTime());
            timeLineVO.setKeywords(timeLine.getKeywords());
            timeLineVO.setShowInChild(timeLine.getShowInChild());
            TimeLineElasticService.TimeLineCriteria timeLineCriteria = new TimeLineElasticService.TimeLineCriteria();
            String timeLineId = getTimeLineIdByTimeLine(timeLine);
            timeLineCriteria.setId(timeLineId);
            timeLineCriteria.setCreatorUserId(timeLine.getCreatorUserId());
            List<TimeLineVO> timeLineVOList = TimeLineElasticService.Instance.searchTimeLine(timeLineCriteria, 0, -1, null, true);
            if (timeLineVOList != null && !timeLineVOList.isEmpty()) {
                if (timeLineVOList.size() > 1) {
                    throw new Exception(ResultStatus.INVALID_DATA.toString());
                } else {
                    timeLineVO.setId(timeLineVOList.get(0).getId());
                }
            }

            return timeLineVO;
        }
        return null;
    }

    public static String getTimeLineIdByTimeLine(TimeLine inputTimeLine) {

        if (inputTimeLine != null) {
            StringBuffer timeLineId = new StringBuffer();
            timeLineId.append(inputTimeLine.getCreatorUserId()).append("_").append(inputTimeLine.getCreatorUserName()).append("_").append(inputTimeLine.getCreationDate());
            return timeLineId.toString();
        } else {
            return null;
        }
    }


}
