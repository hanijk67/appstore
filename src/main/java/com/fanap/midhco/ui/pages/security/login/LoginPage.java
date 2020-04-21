package com.fanap.midhco.ui.pages.security.login;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.login.FanapSSOToken;
import com.fanap.midhco.appstore.service.login.LoginService;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.appstore.service.myException.DisabledUserException;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.AppStoreApplication;
import com.fanap.midhco.ui.AppStoreSession;
import com.fanap.midhco.ui.access.Anonymous;
import com.fanap.midhco.ui.pages.Index;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.xerces.impl.dv.util.Base64;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Anonymous
public class LoginPage extends WebPage {
    static Logger logger = Logger.getLogger(LoginPage.class);

    public LoginPage(PageParameters parameters) {
//        add(new LoginForm("loginForm"));

        AppStoreSession appStoreSession = (AppStoreSession)AppStoreSession.get();
        Session hibernateSession = HibernateUtil.getNewSession();
        Transaction tx = null;

        try {
            StringValue fanapSSOTokenStringValue = parameters.get("fanapSSOToken");

            if (!fanapSSOTokenStringValue.isEmpty()) {
                String fanapSSOTokenAsString = fanapSSOTokenStringValue.toString();

                if (fanapSSOTokenAsString != null) {
                    FanapSSOToken fanapSSOToken = JsonUtil.getObject(fanapSSOTokenAsString, FanapSSOToken.class);
                    User user = UserService.Instance.findUser(fanapSSOToken.getUsername(), hibernateSession);
                    boolean isRoot = UserService.Instance.isUserRoot(user);
                    boolean isDeveloper = UserService.Instance.isUserDeveloper(user);

                    appStoreSession.setRootUser(isRoot);
                    appStoreSession.setUserDeveloper(isDeveloper);

                    tx = hibernateSession.beginTransaction();
                    user.setLogged(true);
                    UserService.Instance.populateUserPermissions(user);
                    hibernateSession.saveOrUpdate(user);
                    tx.commit();

                    appStoreSession.setSsotoken(fanapSSOToken);
                    appStoreSession.setUser(user);
                }
            }
        } catch (Exception ex) {
            logger.error("error processing code request parameter : ", ex);
            if(tx != null)
                tx.rollback();
            throw new AppStoreRuntimeException(ex);
        } finally {
            hibernateSession.close();
        }

        boolean isAuth = appStoreSession.isAuthenticated();
        if(!isAuth) {
            String redirectURL = ConfigUtil.getProperty(ConfigUtil.OAUTH_PORTAL_LOGIN_REDIRECT_URL);

            String authenticationURL = LoginService.Instance.getFanapSSORedirectURL(redirectURL, "");

            HttpServletResponse response = (HttpServletResponse) getResponse().getContainerResponse();
            response.setStatus(response.SC_MOVED_TEMPORARILY);
            response.setHeader("Location", authenticationURL);
            return;
        }

        setResponsePage(Index.class);
    }

    public final class LoginForm extends Form {
        private String username, password;

        public LoginForm(final String id) {
            super(id);

            add(new TextField("username", new PropertyModel(this, "username")).setLabel(new ResourceModel("User.username")).setRequired(true));
            add(new PasswordTextField("password", new PropertyModel(this, "password")).setLabel(new ResourceModel("User.password")).setRequired(true));

            String CTX = ConfigUtil.getProperty(ConfigUtil.APP_WEB_CONTEXT);

            AppStoreApplication appStoreApplication = (AppStoreApplication) AppStoreApplication.get();

            add(new FeedbackPanel("feedback"));

            add(new Button("login") {
                @Override
                public void onSubmit() {
                    HttpServletRequest servletRequest = (HttpServletRequest) ((WebRequest) getRequest()).getContainerRequest();
                    String ip = servletRequest.getRemoteHost();
                    Session session = HibernateUtil.getNewSession();
                    Transaction tx = null;

                    try {
                        tx = session.beginTransaction();

                        AppStoreSession appStoreSession = (AppStoreSession)AppStoreSession.get();

                        final User user = UserService.Instance.authenticate(username, password, ip, session, true);

                        appStoreSession.setUserDeveloper(UserService.Instance.isUserDeveloper(user));

                        appStoreSession.setAuthenticated(true);

                        tx.commit();

                        setResponsePage(Index.class);

                    } catch (LoginException e) {
                        error(getString("username.password.incorrect"));

                    } catch (DisabledUserException e) {
                        if(tx != null && tx.isActive()) {
                            logger.error("rolling back transaction on user Login Form ", e);
                            tx.rollback();
                        }
                        if (e.getUserStatus() == null || e.getUserStatus().equals(UserStatus.DISABLED))
                            error(getString("label.user.disabled"));
                        else if (e.getUserStatus().equals(UserStatus.SECURITY_BLOCKED))
                            error(getString("label.user.security_blocked"));
                        else if (e.getUserStatus().equals(UserStatus.ADMIN_BLOCKED))
                            error(getString("label.user.admin_blocked"));

                    } finally {
                        if(session.isOpen())
                            session.close();
                    }
                }
            });
        }
    }


    public static void main(String[] args) {
        System.out.println(Base64.encode("appstore".getBytes()));
    }
}