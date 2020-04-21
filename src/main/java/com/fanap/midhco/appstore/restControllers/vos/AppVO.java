package com.fanap.midhco.appstore.restControllers.vos;

import com.fanap.midhco.appstore.entities.App;

/**
 * Created by A.Moshiri on 7/17/2017.
 */
public class AppVO {
    Long appId;
    String appTitle;
    String packageName;

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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public static AppVO buildAppVO(App app) {
        AppVO appVO = new AppVO();
        appVO.setAppId(app.getId());
        appVO.setAppTitle(app.getTitle());
        appVO.setPackageName(app.getPackageName());
        return appVO;
    }
}
