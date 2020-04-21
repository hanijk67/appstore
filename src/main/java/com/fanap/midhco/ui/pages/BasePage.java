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

public abstract class BasePage extends WebPage {
    static final Logger logger = Logger.getLogger(BasePage.class);
    private Label title, headerTitle;
    private BootStrapModal baseModalWindow = new BootStrapModal("baseModalWindow");

    protected BasePage() {
        this(new Model());
    }

    // Main Constructor
    protected BasePage(IModel model) {
        title = new Label("title", model);
        headerTitle = new Label("headerTitle", model);

        add(title);
        add(headerTitle);
        add(baseModalWindow);

        title.setVisible(true);


        String username = "";

        User currentUser = PrincipalUtil.getCurrentUser();
        if (currentUser != null)
            username = currentUser.getUserName();

        AppStoreSession session = (AppStoreSession) AppStoreSession.get();
        boolean isAuth = session.isAuthenticated();
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
        add(new WebMarkupContainer("messages").setVisible(isAuth && msg != null));


        sideBar.add(new Label("authenticatedUser", currentUser != null ? currentUser.getFullName() : null));
    }

    public final BootStrapModal getBaseModalWindow() {
        return baseModalWindow;
    }

    public final void setPageTitle(String t) {
        title.setDefaultModel(new Model(t));
        if (t != null)
            headerTitle.setDefaultModel(new Model(String.format("%s - %s", t, getString("label.headerTitleSuffix"))));
        else
            headerTitle.setDefaultModel(new Model(getString("label.headerTitleSuffix")));
    }

    protected final void setPageTitle(IModel model) {
        title.setDefaultModel(model);
        if (model != null)
            headerTitle.setDefaultModel(new Model(String.format("%s - %s", model.getObject().toString(), getString("label.headerTitleSuffix"))));
        else
            headerTitle.setDefaultModel(new Model(getString("label.headerTitleSuffix")));
    }

    public String getTitle() {
        return title == null ? "" : title.getDefaultModelObjectAsString();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
    }
}
