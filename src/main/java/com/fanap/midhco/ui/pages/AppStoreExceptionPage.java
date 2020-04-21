package com.fanap.midhco.ui.pages;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.appstore.wicketApp.AppStoreApplication;
import com.fanap.midhco.ui.access.Anonymous;
import org.apache.log4j.Logger;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.PageExpiredException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

@Anonymous
public class AppStoreExceptionPage extends BasePage {
    private static Logger logger = Logger.getLogger(AppStoreExceptionPage.class);

    public AppStoreExceptionPage() {
        this(new PageExpiredException(""));
    }

    public AppStoreExceptionPage(Exception e) {
        String msg = getLocalizer().getString("error.general", this);
        setPageTitle(msg);
        TextArea excStackTrace = new TextArea("excStackTrace", new Model());

        AppStoreApplication appStoreApplication = (AppStoreApplication) AppStoreApplication.get();

        excStackTrace.setVisible(appStoreApplication.isDevelopmentMode());

        if (appStoreApplication.isDevelopmentMode()) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            excStackTrace.setModel(new Model(writer.toString()));
        }

        Form form = new Form("form");
        form.setOutputMarkupId(true);

        AjaxButton mainPage = new AjaxButton("mainPage", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget ajaxRequestTarget, Form form) {
                String CTX = ConfigUtil.getProperty(ConfigUtil.APP_WEB_CONTEXT);
                throw new RestartResponseAtInterceptPageException(new RedirectPage(CTX));
            }
        };
        mainPage.setOutputMarkupId(true);
        form.add(mainPage);
        add(form);

        if (e instanceof PageExpiredException) {
            logger.debug("Page Expired Exception");
            msg = getString("error.expiredSession");
        } else if (e instanceof UnauthorizedInstantiationException)
            msg = getString("error.notAuthorized");
        else if (e instanceof WicketRuntimeException) {
            if (e.getCause() instanceof InvocationTargetException) {
                InvocationTargetException ite = (InvocationTargetException) e.getCause();
                if (ite.getTargetException() instanceof AppStoreRuntimeException)
                    msg = ite.getTargetException().getMessage();
                else
                    logger.warn("AppStoreExceptionPage", e);
            } else if (e.getMessage().contains("is still locked by"))
                msg = getString("error.pageMap.locked");
            else
                logger.warn("AppStoreExceptionPage", e);
        } else
            logger.error("AppStoreExceptionPage", e);
        add(new Label("message", msg));
        add(excStackTrace);
    }
}
