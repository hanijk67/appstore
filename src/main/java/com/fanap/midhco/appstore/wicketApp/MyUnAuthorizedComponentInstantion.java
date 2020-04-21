package com.fanap.midhco.appstore.wicketApp;

import com.fanap.midhco.ui.AppStoreSession;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.pages.BasePage;
import com.fanap.midhco.ui.pages.Index;
import com.fanap.midhco.ui.pages.security.login.LoginPage;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Created by admin123 on 6/19/2016.
 */
public class MyUnAuthorizedComponentInstantion implements IUnauthorizedComponentInstantiationListener {
    public void onUnauthorizedInstantiation(Component component) {
        AppStoreSession session = (AppStoreSession) Session.get();
        if (!session.isAuthenticated() &&
                (component instanceof BasePage ||
                        //component instanceof BasePage2 ||
                component instanceof BasePanel) || component instanceof Index)
            throw new RestartResponseAtInterceptPageException(new LoginPage(new PageParameters()));
        else
            throw new UnauthorizedInstantiationException(component.getClass());
    }
}
