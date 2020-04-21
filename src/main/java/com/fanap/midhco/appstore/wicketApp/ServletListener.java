package com.fanap.midhco.appstore.wicketApp;

import com.fanap.midhco.appstore.persistence.Dao;
import com.fanap.midhco.ui.access.PrincipalUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by admin123 on 7/13/2017.
 */
public class ServletListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Dao.Instance.closeSessionFactory();
        StartupJobs.shutDown();
        PrincipalUtil.shutdown();
    }
}
