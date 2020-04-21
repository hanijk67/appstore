package com.fanap.midhco.ui;

import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.ui.appStoreMenu.AppStoreMenu;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValueConversionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by admin123 on 6/28/2016.
 */
public class BaseMain {
    static final Logger logger = Logger.getLogger(BaseMain.class);

    public static void processMainRequest(PageParameters parameters, WebPage pageInstanceForProcess) {
        String panelId = null;
        Class panelClass = null;

        try {
            panelId = parameters.get("pageId").toString();
        } catch (StringValueConversionException e) {
            logger.debug(
                    String.format("Main panel: Wrong pageId=[%s]", parameters.get("pageId")), e);
        }

        if (panelId == null)
            throw new AppStoreRuntimeException(pageInstanceForProcess.getString("error.pageNotFound"));

        panelClass = AppStoreMenu.Instance.getPanelClass(panelId);

        if (panelClass == null)
            throw new AppStoreRuntimeException(pageInstanceForProcess.getString("error.pageNotFound"));

        try {
            Constructor constructor = panelClass.getDeclaredConstructor();
            Component component = (Component) constructor.newInstance();
            pageInstanceForProcess.add(component);
        } catch (NoSuchMethodException e) {
            logger.error("Main(NoSuchMethodException)", e);
            throw new AppStoreRuntimeException(pageInstanceForProcess.getString("error.general"));
        } catch (InstantiationException e) {
            logger.error("Main(InstantiationException)", e);
            throw new AppStoreRuntimeException(pageInstanceForProcess.getString("error.general"));
        } catch (IllegalAccessException e) {
            logger.error("Main(IllegalAccessException)", e);
            throw new AppStoreRuntimeException(pageInstanceForProcess.getString("error.general"));
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof UnauthorizedInstantiationException)
                throw new AppStoreRuntimeException(pageInstanceForProcess.getString("error.notAuthorized"));
            else if (e.getTargetException() instanceof RestartResponseAtInterceptPageException)
                throw (RestartResponseAtInterceptPageException) e.getTargetException();
            else if (e.getTargetException() instanceof RuntimeException) {
            } else {
                logger.error("Main(InvocationTargetException)", e);
                throw new AppStoreRuntimeException(pageInstanceForProcess.getString("error.general"));
            }
        }
    }
}
