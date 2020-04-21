package com.fanap.midhco.appstore.entities;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.vos.FileVO;
import com.fanap.midhco.appstore.restControllers.vos.TimeLineVO;

import java.io.Serializable;
import java.util.List;

/**
 * Created by A.Moshiri on 9/16/2018.
 */
public class TimeLine implements Serializable {
    String id;
    String title;
    String description;
    String timeLineDescription;
    List<String> keywords;
    List<FileVO> fileVOList;
    String organizationId;
    String organizationNickName;
    String creatorUserName;
    String lastModificationUserName;
    DateTime startDateTime;
    List<TimeLineFileType> timeLineFileTypes;
    TimeLineFileType timeLineFileType;
    Long startShowTimeLine;
    Long endShowTimeLine;
    Long creatorUserId;
    Long creationDate;
    Long lastModificationUserId;
    Long lastModificationDate;
    Long fileType;

    Boolean isActive;
    Boolean showInChild;

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

    public String getOrganizationNickName() {
        return organizationNickName;
    }

    public void setOrganizationNickName(String organizationNickName) {
        this.organizationNickName = organizationNickName;
    }

    public void setLastModificationUserName(String lastModificationUserName) {
        this.lastModificationUserName = lastModificationUserName;
    }

    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(DateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public List<TimeLineFileType> getTimeLineFileTypes() {
        return timeLineFileTypes;
    }

    public void setTimeLineFileTypes(List<TimeLineFileType> timeLineFileTypes) {
        this.timeLineFileTypes = timeLineFileTypes;
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

    public TimeLineFileType getTimeLineFileType() {
        return timeLineFileType;
    }

    public void setTimeLineFileType(TimeLineFileType timeLineFileType) {
        this.timeLineFileType = timeLineFileType;
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

    public String getTimeLineDescription() {
        return timeLineDescription;
    }

    public void setTimeLineDescription(String timeLineDescription) {
        this.timeLineDescription = timeLineDescription;
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

    public static TimeLine getTimeLineFromTimeLineVO(TimeLineVO timeLineVO) {
        if (timeLineVO != null) {
            TimeLine timeLine = new TimeLine();
            timeLine.setTitle(timeLineVO.getTitle());
            timeLine.setId(timeLineVO.getId());
            if (timeLineVO.getStartShowTimeLine() != null) {
                timeLine.setStartShowTimeLine(timeLineVO.getStartShowTimeLine());
            } else if (timeLineVO.getStartDateTime() != null) {
                timeLine.setStartShowTimeLine(timeLineVO.getStartDateTime().getTime());
            }

            if (timeLineVO.getEndShowTimeLine() != null) {
                timeLine.setEndShowTimeLine(timeLineVO.getEndShowTimeLine());
            } else if (timeLineVO.getStartDateTime() != null) {
                timeLine.setEndShowTimeLine(DateTime.afterFrom(timeLineVO.getStartDateTime(),1).getTime());
            }

            if (timeLineVO.getOrganizationId() != null && !timeLineVO.getOrganizationId().trim().equals("")) {
                timeLine.setOrganizationId(timeLineVO.getOrganizationId());
            }

            if (timeLineVO.getOrganizationNickName() != null && !timeLineVO.getOrganizationNickName().trim().equals("")) {
                timeLine.setOrganizationNickName(timeLineVO.getOrganizationNickName());
            }

            if (timeLineVO.getActive() != null) {
                timeLine.setActive(timeLineVO.getActive());
            } else {
                timeLine.setActive(false);
            }
            timeLine.setCreationDate(timeLineVO.getCreationDate());
            timeLine.setDescription(timeLineVO.getDescription());
            timeLine.setTimeLineDescription(timeLineVO.getTimeLineDescription());

            if (timeLineVO.getFileVOList()!=null && !timeLineVO.getFileVOList().isEmpty()) {
                timeLine.setFileVOList(timeLineVO.getFileVOList());
            }
            if (timeLineVO.getFileType() != null) {
                timeLine.setFileType(timeLineVO.getFileType());
            } else if (timeLineVO.getFileType() != null) {
                timeLine.setFileType(timeLineVO.getFileType());
            }

            timeLine.setKeywords(timeLineVO.getKeywords());

            timeLine.setLastModificationDate(timeLineVO.getLastModificationDate());
            timeLine.setCreatorUserId(timeLineVO.getCreatorUserId());
            timeLine.setCreatorUserName(timeLineVO.getCreatorUserName());
            timeLine.setLastModificationUserId(timeLineVO.getLastModificationUserId());
            timeLine.setLastModificationUserName(timeLineVO.getLastModificationUserName());
            timeLine.setShowInChild(timeLineVO.getShowInChild());
            if (timeLine.getStartDateTime() == null) {
                timeLine.setStartDateTime(new DateTime(timeLineVO.getStartShowTimeLine()));
            }
            return timeLine;
        }
        return null;
    }
}
