package com.fanap.midhco.appstore.service.login;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by admin123 on 2/14/2017.
 */
public class PortalLoginListenerServlet extends HttpServlet {
    final static Logger logger = LogManager.getLogger();

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Session hibernateSession = HibernateUtil.getNewSession();
        HttpSession session = req.getSession(true);
        try {
            String code = req.getParameter("code");
            String state = req.getParameter("state");
            String token =null;

            logger.debug("code recieved is " + code);
            String fanapSSOTokenStr =null;

            if (code != null) {
//                String client_id = ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_ID);
//                String client_secret = ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_SECRET);
//                String redirectURL = ConfigUtil.getProperty(ConfigUtil.OAUTH_PORTAL_LOGIN_REDIRECT_URL);
//                ClientCredentials clientCredentials = new ClientCredentials(client_id, client_secret, redirectURL);
//
//                LonglifeToken longlifeToken = Tokens.fromAuthorizationCode(code, clientCredentials);
//                ModifiableUser user = Users.fromAccessToken(longlifeToken);
//                System.out.println("Old id token: " + longlifeToken.getOAuthResponse().getId_token());
//                String username = user.getUserInfo().getPreferred_username();

                FanapSSOToken fanapSSOToken = LoginService.Instance.getSSOToken(code);
                token = fanapSSOToken.id_token;
                String username = JWTService.extractUserInfo(fanapSSOToken.id_token).getUserName();
                String userId = JWTService.extractUserInfo(fanapSSOToken.id_token).getUserId();
                fanapSSOToken.setUserId(userId);
                fanapSSOToken.setUsername(username);
                fanapSSOTokenStr = JsonUtil.getJson(fanapSSOToken);
                logger.debug("fanapSSOToken recieved is " + fanapSSOTokenStr);

                User portalUser = UserService.Instance.findUserWithUserId(Long.valueOf(userId), hibernateSession);
                if (portalUser != null) {
                    if (portalUser.getUserStatus().equals(UserStatus.ENABLED)) {
                    session.setAttribute("fanapSSOToken", JsonUtil.getJson(fanapSSOToken));
                    session.setAttribute("application_path", ConfigUtil.getProperty(ConfigUtil.APPLICATION_PATH));
                }else {
                        session.setAttribute("userStatusError", AppStorePropertyReader.getString("label.user.disabled"));
                        session.setAttribute("logoutUser", SSOUserService.SSO_URL_USER_LOGOUT);
                        session.setAttribute("btnCloseLabel", AppStorePropertyReader.getString("label.exit.sso"));
                    }
                } else {
                    session.setAttribute("userName",username);
                    String message = AppStorePropertyReader.getString("label.sso.user.name.error");
                    message = message.replace("${userName}", username);
                    session.setAttribute("userNameError", message);
                    session.setAttribute("logoutUser", SSOUserService.SSO_URL_USER_LOGOUT);
                    session.setAttribute("btnCloseLabel",  AppStorePropertyReader.getString("label.exit.sso"));
                }
            }

            if (state != null && !state.trim().equals("")) {
                StringBuffer url = new StringBuffer();
                url.append(state).append("?").append(URLEncoder.encode(fanapSSOTokenStr));
                resp.sendRedirect(url.toString());
                resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                resp.setHeader("Location", url.toString());

//                req.getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
            }else {
                req.getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
            }
        } catch (Exception ex) {
            logger.error("Error occured in PortalLoginListenerServlet: ", ex);
            session.setAttribute("generalErroInPortal",AppStorePropertyReader.getString("error.generalErr"));
//            req.getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
        } finally {
            hibernateSession.close();
        }
    }
}