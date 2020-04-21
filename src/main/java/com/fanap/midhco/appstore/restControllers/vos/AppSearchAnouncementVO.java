package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.app.AppService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by A.Moshiri on 7/19/2017.
 */
public class AppSearchAnouncementVO {

    public AppSearchAnouncementVO() {
    }

    public String title;
    public String versionCode;
    public String versionName;
    public String packageName;
    public Collection<Long> osIDList;
    public Collection<Long> osTypeIDList;
    public Collection<PublishState> publishStateList;
    public Collection<Byte> bytePublishStates;

    public Collection<Long> developerIDList;
    public Collection<Long> creatorIDList;
    public Collection<Long> appCategoryIDList;
    public Collection<String> keyWords;
    public Long[] creationDateTime;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Collection<Long> getOsIDList() {
        return osIDList;
    }

    public void setOsIDList(Collection<Long> osIDList) {
        this.osIDList = osIDList;
    }

    public Collection<PublishState> getPublishStateList() {
        return publishStateList;
    }

    public void setPublishStateList(Collection<PublishState> publishStateList) {
        this.publishStateList = publishStateList;
    }

    public Collection<Byte> getBytePublishStates() {
        return bytePublishStates;
    }

    public void setBytePublishStates(Collection<Byte> bytePublishStates) {
        this.bytePublishStates = bytePublishStates;
    }

    public Collection<Long> getOsTypeIDList() {
        return osTypeIDList;
    }

    public void setOsTypeIDList(Collection<Long> osTypeIDList) {
        this.osTypeIDList = osTypeIDList;
    }

    public Collection<Long> getDeveloperIDList() {
        return developerIDList;
    }

    public void setDeveloperIDList(Collection<Long> developerIDList) {
        this.developerIDList = developerIDList;
    }

    public Collection<Long> getCreatorIDList() {
        return creatorIDList;
    }

    public void setCreatorIDList(Collection<Long> creatorIDList) {
        this.creatorIDList = creatorIDList;
    }

    public Collection<Long> getAppCategoryIDList() {
        return appCategoryIDList;
    }

    public void setAppCategoryIDList(Collection<Long> appCategoryIDList) {
        this.appCategoryIDList = appCategoryIDList;
    }

    public Collection<String> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(Collection<String> keyWords) {
        this.keyWords = keyWords;
    }

    public Long[] getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Long[] creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public static AppSearchAnouncementVO createAppSearchAnouncementVoFromCriteria(AppService.AppSearchCriteria appSearchCriteria) {
        if (appSearchCriteria == null)
            return null;
        AppSearchAnouncementVO appSearchAnouncementVO = new AppSearchAnouncementVO();
        if (appSearchCriteria.getAppCategory() != null) {
            List<Long> appCategoryIdList = new ArrayList<>();
            for (AppCategory appCategory : appSearchCriteria.getAppCategory()) {
                appCategoryIdList.add(appCategory.getId());
            }
            appSearchAnouncementVO.setAppCategoryIDList(appCategoryIdList);
        }
        if (appSearchCriteria.getCreatorUsers() != null) {
            List<Long> creatorUserIdList = new ArrayList<>();
            for (User creator : appSearchCriteria.getCreatorUsers()) {
                creatorUserIdList.add(creator.getId());
            }
            appSearchAnouncementVO.setCreatorIDList(creatorUserIdList);
        }

        if (appSearchCriteria.getDevelopers() != null) {
            List<Long> developerUserIdList = new ArrayList<>();
            for (User developer : appSearchCriteria.getDevelopers()) {
                developerUserIdList.add(developer.getId());
            }
            appSearchAnouncementVO.setDeveloperIDList(developerUserIdList);
        }
        if (appSearchCriteria.getOs() != null) {
            List<Long> osIdList = new ArrayList<>();
            for (OS os : appSearchCriteria.getOs()) {
                osIdList.add(os.getId());
            }
            appSearchAnouncementVO.setOsIDList(osIdList);
        }
        if (appSearchCriteria.getOsType() != null) {
            List<Long> osTypeIdList = new ArrayList<>();
            for (OSType osType : appSearchCriteria.getOsType()) {
                osTypeIdList.add(osType.getId());
            }
            appSearchAnouncementVO.setOsTypeIDList(osTypeIdList);
        }

        List<Byte> bytePublishStateList = new ArrayList<>();
        if (appSearchCriteria.getPublishStates() != null) {
        appSearchAnouncementVO.setPublishStateList(appSearchCriteria.getPublishStates());
            for (PublishState publishState : appSearchCriteria.getPublishStates()) {

                if (publishState.equals(PublishState.PUBLISHED)) {
                    bytePublishStateList.add(Byte.valueOf("2"));
                }
                if (publishState.equals(PublishState.UNPUBLISHED)) {
                    bytePublishStateList.add(Byte.valueOf("1"));
                }
            }
        }
        appSearchAnouncementVO.setBytePublishStates(bytePublishStateList);
        appSearchAnouncementVO.setKeyWords(appSearchCriteria.getKeyword());

        Long[] creationDateTime = null;
        if(appSearchCriteria.creationDateTime != null) {
            creationDateTime = new Long[2];

            if(appSearchCriteria.creationDateTime[0] != null && !appSearchCriteria.creationDateTime[0].equals(DateTime.MIN_DATE_TIME))
                creationDateTime[0] = appSearchCriteria.creationDateTime[0].toDate().getTime();

            if(appSearchCriteria.creationDateTime[1] != null && !appSearchCriteria.creationDateTime[1].equals(DateTime.MIN_DATE_TIME))
                creationDateTime[1] = appSearchCriteria.creationDateTime[1].toDate().getTime();
        }

        appSearchAnouncementVO.setCreationDateTime(creationDateTime);
        appSearchAnouncementVO.setPackageName(appSearchCriteria.getPackageName());
        appSearchAnouncementVO.setVersionCode(appSearchCriteria.getVersionCode());
        appSearchAnouncementVO.setVersionName(appSearchCriteria.getVersionName());
        appSearchAnouncementVO.setTitle(appSearchCriteria.getTitle());

        return appSearchAnouncementVO;
    }

}
