package com.fanap.midhco.appstore.wicketApp;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.login.SSOUserService;
import com.fanap.midhco.ui.AppStoreRequestCycle;
import com.fanap.midhco.ui.AppStoreSession;
import com.fanap.midhco.ui.Main;
import com.fanap.midhco.ui.Main2;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.appStoreMenu.AppStoreMenu;
import com.fanap.midhco.ui.pages.AppStoreExceptionPage;
import com.fanap.midhco.ui.pages.packagesList.PackageList;
import com.fanap.midhco.ui.pages.security.login.LoginPage;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.settings.ExceptionSettings;
import org.apache.wicket.settings.RequestCycleSettings;
import org.hibernate.Query;
import org.hibernate.Transaction;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AppStoreApplication extends WebApplication {
    static Logger logger = Logger.getLogger(AppStoreApplication.class);
    Session session = null;

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage() {
        return PackageList.class;
    }

    /**
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init() {
        logger.info("###########################################################");
        logger.info("##### ---- Midhco AppStore Server UI Starting Up ---- #####");
        logger.info("###########################################################");

        super.init();
        getMarkupSettings().setStripWicketTags(true);
        getMarkupSettings().setCompressWhitespace(true);
        getMarkupSettings().setStripComments(true);
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

        // this setting shows the exception stacktrace
        getExceptionSettings().setUnexpectedExceptionDisplay(
                ExceptionSettings.SHOW_EXCEPTION_PAGE);
        //to go to custom page on expired Error
        getApplicationSettings().setPageExpiredErrorPage(AppStoreExceptionPage.class);

        // this setting shows the "internal error" page specified below
        getExceptionSettings().setUnexpectedExceptionDisplay(
                ExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
        //to go to custom page on internal Error
        getApplicationSettings().setInternalErrorPage(AppStoreExceptionPage.class);

        getExceptionSettings().setUnexpectedExceptionDisplay( ExceptionSettings.SHOW_NO_EXCEPTION_PAGE );

        MyAuthorizationStrategy myAuthorizationStrategy = new MyAuthorizationStrategy();
        MyUnAuthorizedComponentInstantion myUnAuthorizedComponentInstantion = new MyUnAuthorizedComponentInstantion();

        getSecuritySettings().setAuthorizationStrategy(myAuthorizationStrategy);
        getSecuritySettings().setUnauthorizedComponentInstantiationListener(myUnAuthorizedComponentInstantion);

        getRequestCycleListeners().add(new AppStoreRequestCycle());

        getDebugSettings().setAjaxDebugModeEnabled(false);

        mountPage("/main", Main.class);
        mountPage("/main2", Main2.class);
        mountPage("/loginPage", LoginPage.class);

        HibernateUtil.init();
        logger.info("# Hibernate Inited #");

        Access.init();
        logger.info("# Accessed Inited #");

        AppStoreMenu.Instance.init();
        logger.info("# AppStoreMenu Inited #");

        StartupJobs.init();

        KeybodConfig.init();

        getRequestCycleSettings().setRenderStrategy(
                RequestCycleSettings.RenderStrategy.ONE_PASS_RENDER);

        logger.info("#check primitive Data#");


        Boolean insertedPrimitiveDate = checkIsInitialized();
        if (!insertedPrimitiveDate) {
            logger.error("#problem in checking primitive data#");
        }


    }

    private Boolean checkIsInitialized() {


        org.hibernate.Session session = HibernateUtil.getNewSession();
        Transaction tx = session.beginTransaction();
        try {

            HQLBuilder hqlBuilder = new HQLBuilder(session, "select ent ", "from FlagForInsertData ent");
            Query query = hqlBuilder.createQuery();
            List<FlagForInsertData> flagForInsertDataList = query.list();
            if (flagForInsertDataList != null && !flagForInsertDataList.isEmpty()) {
                FlagForInsertData flagForInsertData = flagForInsertDataList.get(0);
                if (flagForInsertData.getIsLaunched()) {
                    return true;
                }
            }
            Role rootRole = null;
            Role developerRole = null;

            rootRole = new Role();
            rootRole.setAccessCodes(null);
            rootRole.setEditable(false);
            rootRole.setName(ConfigUtil.getProperty(ConfigUtil.ROOT_ROLE_NAME));
            rootRole.setEditable(false);
            session.saveOrUpdate(rootRole);

            developerRole = new Role();
            developerRole.setAccessCodes(ConfigUtil.getProperty(ConfigUtil.DEVELOPER_ROLE_ACCESS));
            developerRole.setEditable(false);
            developerRole.setName(ConfigUtil.getProperty(ConfigUtil.DEVELOPER_ROLE_NAME));
            developerRole.setEditable(false);

            session.saveOrUpdate(developerRole);

            User adminUser = new User();
            Set<Role> roleList = new HashSet<>();
            roleList.add(rootRole);
            adminUser.setRoles(roleList);

            Long userId = SSOUserService.Instance.getUserIdByUserName(ConfigUtil.getProperty(ConfigUtil.ADMIN_USER_NAME));
            if(userId==Long.valueOf(-1)){
                throw new Exception(ResultStatus.INVALID_USER.toString());
            }
            Contact contact = new Contact();
            contact.setFirstName(ConfigUtil.getProperty(ConfigUtil.ADMIN_USER_NAME)+"FirstName");
            contact.setLastName(ConfigUtil.getProperty(ConfigUtil.ADMIN_USER_NAME)+"LastName");
            adminUser.setContact(contact);
            adminUser.setUserName(ConfigUtil.getProperty(ConfigUtil.ADMIN_USER_NAME));
            adminUser.setUserStatus(new UserStatus(Byte.valueOf(String.valueOf(1))));
            adminUser.setUserId(userId);
            adminUser.setLogged(false);

            session.saveOrUpdate(adminUser);

            AppCategory rootCategory = new AppCategory();
            rootCategory.setCategoryName(ConfigUtil.getProperty(ConfigUtil.CATEGORY_ROOT_NAME));
            rootCategory.setAssignable(false);
            rootCategory.setEnabled(true);
            rootCategory.setParent(null);

            session.saveOrUpdate(rootCategory);
            OSEnvironment osEnvironment = new OSEnvironment();

            osEnvironment.setEnvName(ConfigUtil.getProperty(ConfigUtil.DEFAULT_OS_ENVIRONMENT_NAME));

            session.saveOrUpdate(osEnvironment);

            FlagForInsertData flagForInsertData = new FlagForInsertData();
            flagForInsertData.setIsLaunched(true);
            session.saveOrUpdate(flagForInsertData);
            tx.commit();

            logger.info("# insert primitive Data#");
            return true;

        } catch (Exception e) {
            tx.rollback();

            logger.error("occurd error in insert primitive Data : "+ ExceptionUtils.getFullStackTrace(e));


            e.printStackTrace();
            return false;
        }


    }


    @Override
    public Session newSession(Request request, Response response) {
        AppStoreSession session = new AppStoreSession(request);

        String defaultLocaleABBRV = ConfigUtil.getProperty(ConfigUtil.APP_DEFAULT_LOCALE_LANG);
        String defaultLocaleCountry = ConfigUtil.getProperty(ConfigUtil.APP_DEFAULT_LOCALE_COUNTRY);

        session.setLocale(new Locale(defaultLocaleABBRV, defaultLocaleCountry));
        return session;
    }

    public static boolean isInTestMode() {
        String appIsInTestMode = ConfigUtil.getProperty(ConfigUtil.APP_IS_IN_TEST_MODE);
        return Boolean.parseBoolean(appIsInTestMode);
    }

    public boolean isDevelopmentMode() {
        return Boolean.parseBoolean(ConfigUtil.getProperty(ConfigUtil.APP_IS_IN_DEVELOPMENTMODE));
    }
}
