package com.fanap.midhco.appstore.entities.issue.testIssue;

import com.fanap.midhco.appstore.entities.App;
import com.fanap.midhco.appstore.entities.issue.Issue;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by admin123 on 12/14/2016.
 */
public class AppTestIssue extends Issue {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAPPID")
    App app;

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }
}
