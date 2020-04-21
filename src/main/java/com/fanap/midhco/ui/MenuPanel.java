package com.fanap.midhco.ui;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.appStoreMenu.AppStoreMenu;
import com.fanap.midhco.ui.appStoreMenu.IMenuItem;
import com.fanap.midhco.ui.pages.anouncement.AnouncementList;
import com.fanap.midhco.ui.pages.anouncement.AnouncementManagement;
import com.fanap.midhco.ui.pages.app.AppList;
import com.fanap.midhco.ui.pages.appcategory.AppCategoryList;
import com.fanap.midhco.ui.pages.device.DevList;
import com.fanap.midhco.ui.pages.environment.EnvironmentList;
import com.fanap.midhco.ui.pages.org.OrgList;
import com.fanap.midhco.ui.pages.os.OSList;
import com.fanap.midhco.ui.pages.os.OSManagement;
import com.fanap.midhco.ui.pages.os.OSTypeList;
import com.fanap.midhco.ui.pages.role.RoleList;
import com.fanap.midhco.ui.pages.timeLine.TimeLineList;
import com.fanap.midhco.ui.pages.user.UserList;
import com.fanap.midhco.ui.wicket.jafarnezhad.Hani;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

import java.io.Serializable;


public class MenuPanel extends BasePanel implements IHeaderContributor {
    private WebMarkupContainer basic_data = new WebMarkupContainer("basic_data");
    private WebMarkupContainer user_management = new WebMarkupContainer("user_management");
    private WebMarkupContainer userList = new WebMarkupContainer("userList");
    private WebMarkupContainer roleList = new WebMarkupContainer("roleList");
    private WebMarkupContainer osManagement = new WebMarkupContainer("osManagement");
    private WebMarkupContainer app_management = new WebMarkupContainer("app_management");
    private WebMarkupContainer appList = new WebMarkupContainer("appList");
    private WebMarkupContainer appCategoryList = new WebMarkupContainer("appCategoryList");
    private WebMarkupContainer deviceManagement = new WebMarkupContainer("deviceManagement");
    private WebMarkupContainer anouncement_management = new WebMarkupContainer("anouncement_management");
    private WebMarkupContainer anouncementMgn = new WebMarkupContainer("anouncementMgn");
    private WebMarkupContainer organizationList = new WebMarkupContainer("organizationList");
    private WebMarkupContainer environmentList = new WebMarkupContainer("environmentList");
    private WebMarkupContainer timeLineList = new WebMarkupContainer("timeLineList");
    private WebMarkupContainer haniJafarnezhad = new WebMarkupContainer("haniJafarnezhad");
    private WebMarkupContainer hani = new WebMarkupContainer("hani");


    public MenuPanel(String id) {
        super(id);

        add(basic_data);
        basic_data.add(user_management);
        add(app_management);
        app_management.add(appList);

        add(anouncement_management);
        anouncement_management.add(anouncementMgn);

        add(haniJafarnezhad);
        haniJafarnezhad.add(hani);

        User currentUser = PrincipalUtil.getCurrentUser();

        String CTX = ConfigUtil.getProperty(ConfigUtil.APP_WEB_CONTEXT);

        IMenuItem menuItem = AppStoreMenu.Instance.getMenuItem(UserList.class);
        String temp = String.format("%smain?pageId=%s", CTX, menuItem.getIdCode());
        WebMarkupContainer inner_a = new WebMarkupContainer("inner_a");
        inner_a.add(new AttributeModifier("href", new Model(temp)));
        inner_a.add(new WebMarkupContainer("title", new Model<Serializable>(menuItem.getTitle())));
        userList.add(inner_a);
        userList.setMarkupId("Menu_" + menuItem.getIdCode());
        userList.setVisible(hasAccess(currentUser, UserList.class));

        user_management.add(userList);

        menuItem = AppStoreMenu.Instance.getMenuItem(RoleList.class);
        temp = String.format("%smain?pageId=%s", CTX, menuItem.getIdCode());
        inner_a = new WebMarkupContainer("inner_a");
        inner_a.add(new AttributeModifier("href", new Model(temp)));
        inner_a.add(new WebMarkupContainer("title", new Model<Serializable>(menuItem.getTitle())));
        roleList.add(inner_a);
        roleList.setMarkupId("Menu_" + menuItem.getIdCode());
        roleList.setVisible(hasAccess(currentUser, RoleList.class));

        user_management.add(roleList);

        user_management.setVisible(userList.isVisible() || roleList.isVisible());

        menuItem = AppStoreMenu.Instance.getMenuItem(OSManagement.class);
        temp = String.format("%smain2?pageId=%s", CTX, menuItem.getIdCode());
        inner_a = new WebMarkupContainer("inner_a");
        inner_a.add(new AttributeModifier("href", new Model(temp)));
        inner_a.add(new WebMarkupContainer("title", new Model<Serializable>(menuItem.getTitle())));
        osManagement.add(inner_a);
        osManagement.setMarkupId("Menu_" + menuItem.getIdCode());
        osManagement.setVisible(hasAccess(currentUser, OSTypeList.class) || hasAccess(currentUser, OSList.class));
        basic_data.add(osManagement);

        menuItem = AppStoreMenu.Instance.getMenuItem(AppCategoryList.class);
        temp = String.format("%smain?pageId=%s", CTX, menuItem.getIdCode());
        inner_a = new WebMarkupContainer("inner_a");
        inner_a.add(new AttributeModifier("href", new Model(temp)));
        inner_a.add(new WebMarkupContainer("title", new Model<Serializable>(menuItem.getTitle())));
        appCategoryList.add(inner_a);
        appCategoryList.setMarkupId("Menu_" + menuItem.getIdCode());
        appCategoryList.setVisible(hasAccess(currentUser, AppCategoryList.class));
        basic_data.add(appCategoryList);

        menuItem = AppStoreMenu.Instance.getMenuItem(OrgList.class);
        temp = String.format("%smain?pageId=%s", CTX, menuItem.getIdCode());
        inner_a = new WebMarkupContainer("inner_a");
        inner_a.add(new AttributeModifier("href", new Model(temp)));
        inner_a.add(new WebMarkupContainer("title", new Model<Serializable>(menuItem.getTitle())));
        organizationList.add(inner_a);
        organizationList.setMarkupId("Menu_" + menuItem.getIdCode());
        organizationList.setVisible(hasAccess(currentUser, OrgList.class));
        basic_data.add(organizationList);

        menuItem = AppStoreMenu.Instance.getMenuItem(EnvironmentList.class);
        temp = String.format("%smain?pageId=%s", CTX, menuItem.getIdCode());
        inner_a = new WebMarkupContainer("inner_a");
        inner_a.add(new AttributeModifier("href", new Model(temp)));
        inner_a.add(new WebMarkupContainer("title", new Model<Serializable>(menuItem.getTitle())));
        environmentList.add(inner_a);
        environmentList.setMarkupId("Menu_" + menuItem.getIdCode());
        environmentList.setVisible(hasAccess(currentUser, EnvironmentList.class));
        basic_data.add(environmentList);

        menuItem = AppStoreMenu.Instance.getMenuItem(TimeLineList.class);
        temp = String.format("%smain?pageId=%s", CTX, menuItem.getIdCode());
        inner_a = new WebMarkupContainer("inner_a");
        inner_a.add(new AttributeModifier("href", new Model(temp)));
        inner_a.add(new WebMarkupContainer("title", new Model<Serializable>(menuItem.getTitle())));
        timeLineList.add(inner_a);
        timeLineList.setMarkupId("Menu_" + menuItem.getIdCode());
        timeLineList.setVisible(hasAccess(currentUser, TimeLineList.class));
        basic_data.add(timeLineList);

        menuItem = AppStoreMenu.Instance.getMenuItem(AppList.class);
        temp = String.format("%smain?pageId=%s", CTX, menuItem.getIdCode());
        inner_a = new WebMarkupContainer("inner_a");
        inner_a.add(new AttributeModifier("href", new Model(temp)));
        inner_a.add(new WebMarkupContainer("title", new Model<Serializable>(menuItem.getTitle())));
        appList.add(inner_a);
        appList.setMarkupId("Menu_" + menuItem.getIdCode());
        appList.setVisible(hasAccess(currentUser, AppList.class));
        app_management.add(appList);

        app_management.setVisible(appList.isVisible());

        menuItem = AppStoreMenu.Instance.getMenuItem(DevList.class);
        temp = String.format("%smain?pageId=%s", CTX, menuItem.getIdCode());
        inner_a = new WebMarkupContainer("inner_a");
        inner_a.add(new AttributeModifier("href", new Model(temp)));
        inner_a.add(new WebMarkupContainer("title", new Model<Serializable>(menuItem.getTitle())));
        deviceManagement.add(inner_a);
        deviceManagement.setMarkupId("Menu_" + menuItem.getIdCode());
        basic_data.add(deviceManagement);
        deviceManagement.setVisible(hasAccess(currentUser, DevList.class));

        menuItem = AppStoreMenu.Instance.getMenuItem(AnouncementManagement.class);
        temp = String.format("%smain?pageId=%s", CTX, menuItem.getIdCode());
        inner_a = new WebMarkupContainer("inner_a");
        inner_a.add(new AttributeModifier("href", new Model(temp)));
        inner_a.add(new WebMarkupContainer("title", new Model<Serializable>(menuItem.getTitle())));
        anouncementMgn.add(inner_a);
        anouncementMgn.setMarkupId("Menu_" + menuItem.getIdCode());
        anouncementMgn.setVisible(hasAccess(currentUser, AnouncementList.class));

        anouncement_management.add(anouncementMgn);
        anouncement_management.setVisible(anouncementMgn.isVisible());

        menuItem = AppStoreMenu.Instance.getMenuItem(Hani.class);
        temp = String.format("%smain?pageId=%s", CTX, menuItem.getIdCode());
        inner_a = new WebMarkupContainer("inner_a");
        inner_a.add(new AttributeModifier("href", new Model(temp)));
        inner_a.add(new WebMarkupContainer("title", new Model<Serializable>(menuItem.getTitle())));
        hani.add(inner_a);
        hani.setMarkupId("Menu_" + menuItem.getIdCode());
        hani.setVisible(true);


        haniJafarnezhad.setVisible(hani.isVisible());

        basic_data.setVisible(user_management.isVisible() || osManagement.isVisible() || deviceManagement.isVisible() || organizationList.isVisible() ||
                environmentList.isVisible() || appCategoryList.isVisible() || deviceManagement.isVisible()|| timeLineList.isVisible());
    }

    public Class getPanelClass(Integer panelHashCode) {
        return null;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
    }

    private boolean hasAccess(User currentUser, Class menuClass) {
        boolean isRootUser = ((AppStoreSession) (AppStoreSession.get())).isRootUser();
        if (isRootUser)
            return true;

        Authorize authorizeAnntotation = (Authorize) menuClass.getAnnotation(Authorize.class);
        boolean hasAccess1 = false;
        boolean hasAccess2 = false;

        if (authorizeAnntotation != null) {
            if (authorizeAnntotation.views() != null)
                hasAccess1 = UserService.Instance.hasPermission(currentUser, authorizeAnntotation.views());

            if (authorizeAnntotation.view() != null)
                hasAccess2 = UserService.Instance.hasPermission(currentUser, authorizeAnntotation.view());
        }
        return hasAccess1 || hasAccess2;
    }
}
