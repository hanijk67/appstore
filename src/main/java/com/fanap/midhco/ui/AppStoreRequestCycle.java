package com.fanap.midhco.ui;

import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.pages.AppStoreExceptionPage;
import com.fanap.midhco.ui.pages.security.login.LoginPage;
import org.apache.log4j.Logger;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.*;
import org.hibernate.Session;

/**
 * Created by admin123 on 6/15/2016.
 */
public class AppStoreRequestCycle extends AbstractRequestCycleListener {
    private static final Logger logger = Logger.getLogger(AppStoreRequestCycle.class);
    private static ThreadLocal<Long> incomingTime = new ThreadLocal<Long>();

    @Override
    public void onBeginRequest(RequestCycle requestCycle) {
        AppStoreSession appStoreSession = (AppStoreSession) AppStoreSession.get();
        logger.debug("Assigning user to threal local variable " +
                ((appStoreSession.getUser() != null ? appStoreSession.getUser().getUserName() : "")
                        + "- REQ_session Id is " + appStoreSession.getId()));
        PrincipalUtil.setCurrentUser(appStoreSession.getUser());
        PrincipalUtil.setIsUserDeveloper(appStoreSession.isUserDeveloper());
        PrincipalUtil.setIsUserRoot(appStoreSession.isRootUser());

        incomingTime.set(System.currentTimeMillis());
    }

    @Override
    public void onEndRequest(RequestCycle requestCycle) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            HibernateUtil.endTransaction();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(session != null && session.isOpen())
                session.close();
        }
    }

    @Override
    public IRequestHandler onException(RequestCycle requestCycle, Exception e) {
        try {
            HibernateUtil.rollback();
        } catch (Exception ex) {
            logger.error("Error occured in requestCycle rollback ", ex);
        }

//        HibernateUtil.getCurrentSession().close();

        if(e instanceof PageExpiredException) {
            return new RenderPageRequestHandler(new PageProvider(LoginPage.class));
        } else if (e instanceof RuntimeException) {
            return new RenderPageRequestHandler(new PageProvider(new AppStoreExceptionPage(e)));
        } else
            return super.onException(requestCycle, e);
    }
}
