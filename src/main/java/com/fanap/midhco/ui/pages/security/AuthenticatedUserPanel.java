package com.fanap.midhco.ui.pages.security;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.login.FanapSSOToken;
import com.fanap.midhco.ui.AppStoreSession;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.pages.Index;
import com.fanap.midhco.ui.pages.packagesList.PackageList;
import com.fanapium.keylead.client.vo.ClientCredentials;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class AuthenticatedUserPanel extends BasePanel implements IParentListner {
    BootStrapModal modalWindow;

    public AuthenticatedUserPanel(String id) {
        super(id);

        add(new BookmarkablePageLink("index", Index.class));

        add(new AjaxLink("logout") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                Session hibernateSession = HibernateUtil.getNewSession();
                Transaction tx = null;

                try {
                    AppStoreSession session = (AppStoreSession) getSession();

                    FanapSSOToken ssoToken = session.getSsotoken();

                    String client_id = ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_ID);
                    String client_secret = ConfigUtil.getProperty(ConfigUtil.OAUTH_CLIENT_SECRET);
                    ClientCredentials credentials = new ClientCredentials(client_id, client_secret, "");

                    session.setSsotoken(null);

                    User user = session.getUser();
                    tx = hibernateSession.beginTransaction();
                    user = (User)hibernateSession.load(User.class, user.getId());
                    user.setLogged(false);
                    hibernateSession.saveOrUpdate(user);
                    tx.commit();

                    session.invalidateNow();
                    setResponsePage(PackageList.class);

                } catch (Exception ex) {
                    logger.error("error logging out!", ex);
                    if(tx != null)
                        tx.rollback();
                } finally {
                    hibernateSession.close();
                }

            }
        });


        modalWindow = new BootStrapModal("modal");
        add(modalWindow);
//        add(new AjaxLink("changePassword") {
//            public void onClick(AjaxRequestTarget target) {
//                ChangePasswordPanel passwordPanel = new ChangePasswordPanel(modalWindow.getContentId(), PrincipalUtil.getCurrentUser()) {
//                    @Override
//                    public void update(AjaxRequestTarget target) {
//                        modalWindow.close(target);
//                    }
//                };
//                String title = getMsg("change.password");
//                modalWindow.setContent(passwordPanel);
//                modalWindow.show(target);
//            }
//        });
    }

    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        modalWindow.close(target);
    }
}
