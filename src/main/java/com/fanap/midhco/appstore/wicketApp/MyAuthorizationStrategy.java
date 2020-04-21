package com.fanap.midhco.appstore.wicketApp;

import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.AppStoreSession;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Anonymous;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.pages.BasePage;
import com.fanap.midhco.ui.pages.Index;
import com.fanap.midhco.ui.pages.security.login.LoginPage;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;

/**
 * Created by admin123 on 6/19/2016.
 */
public class MyAuthorizationStrategy implements IAuthorizationStrategy {
    static Logger logger = Logger.getLogger(MyAuthorizationStrategy.class);

    public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass) {
        AppStoreSession currentSession = (AppStoreSession) Session.get();

        if (!BasePanel.class.isAssignableFrom(componentClass) &&
                !BasePage.class.isAssignableFrom(componentClass)
//                && !BasePage2.class.isAssignableFrom(componentClass)
                && !(componentClass.equals(Index.class)))
            return true;

        AppStoreSession session = (AppStoreSession) AppStoreSession.get();

        if (componentClass.isAnnotationPresent(Anonymous.class)) {
            if(componentClass.isAssignableFrom(LoginPage.class) && session.isAuthenticated())
                throw new RestartResponseAtInterceptPageException(Index.class);
            return true;
        }

        if (session.isAuthenticated()) {
            Authorize authorize = (Authorize) componentClass.getAnnotation(Authorize.class);
            boolean isRootUser = session.isRootUser();

            boolean hasAccess = authorize == null ||
                    (!authorize.view().equals(Access.NULL) && UserService.Instance.hasPermission(session.getUser(), authorize.view()))
                    ||
                    (isRootUser)
                    ||
                    (authorize.views() != null && authorize.views().length > 0 && UserService.Instance.hasPermission(session.getUser(), authorize.views()));
            if (authorize != null && logger.isDebugEnabled())
                logger.debug(String.format("@Authorizing [%s]: %s", componentClass.getName(), hasAccess));
            return hasAccess;
        }

        return false;
    }

    public boolean isActionAuthorized(Component component, Action action) {
        return true;
    }

    public boolean isResourceAuthorized(IResource iResource, PageParameters pageParameters) {
        return true;
    }
}
