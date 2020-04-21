package com.fanap.midhco.ui.pages;

import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.ui.AppStoreSession;
import com.fanap.midhco.ui.MenuPanel;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.pages.security.AuthenticatedUserPanel;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public abstract class BasePage2 extends WebPage {
    private Label headerTitle;
    private BootStrapModal baseModalWindow = new BootStrapModal("baseModalWindow");

    protected BasePage2() {
        this(new Model());
    }

    protected BasePage2(IModel model) {
        final Logger logger = Logger.getLogger(BasePage.class);

        headerTitle = new Label("headerTitle", model);

        add(headerTitle);
        add(baseModalWindow);

        String username = "";

        User currentUser = PrincipalUtil.getCurrentUser();
        if (currentUser != null)
            username = currentUser.getUserName();

        AppStoreSession session = (AppStoreSession) AppStoreSession.get();
        boolean isAuth= false;
        isAuth = session.isAuthenticated();

        WebMarkupContainer sideBar = new WebMarkupContainer("sideBar");
        if (isAuth) {
            add(new AuthenticatedUserPanel("authenticatedUserPanel"));
            MenuPanel menuPanel = new MenuPanel("menuPanel");
            menuPanel.setMarkupId("main-menu");
            menuPanel.setOutputMarkupId(true);
            sideBar.add(menuPanel);
        } else {
            add(new Label("authenticatedUserPanel", ""));
            sideBar.add(new Label("menuPanel", ""));
            sideBar.setVisible(false);
        }

        add(sideBar);

        String msg = null;
        add(new WebMarkupContainer("messages").setVisible(isAuth && msg != null)); //{


        sideBar.add(new Label("authenticatedUser", currentUser != null ? currentUser.getFullName() : null));
    }

    public final BootStrapModal getBaseModalWindow() {
        return baseModalWindow;
    }

    protected final void setPageTitle(String t) {
        if (t != null)
            headerTitle.setDefaultModel(new Model(String.format("%s - %s", t, getMsg("label.headerTitleSuffix"))));
        else
            headerTitle.setDefaultModel(new Model(getMsg("label.headerTitleSuffix")));
    }

    protected final void setPageTitle(IModel model) {
        if (model != null)
            headerTitle.setDefaultModel(new Model(String.format("%s - %s", model.getObject().toString(), getMsg("label.headerTitleSuffix"))));
        else
            headerTitle.setDefaultModel(new Model(getMsg("label.headerTitleSuffix")));
    }

    protected String getMsg(String key) {
        return getLocalizer().getString(key, this, key);
    }

    protected String getCRUDMsg(String key, String entKey) {
        return String.format("%s %s", getMsg(key), getMsg(entKey));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
    }
}
