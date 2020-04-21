package com.fanap.midhco.ui.pages.security;

import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.AppStoreSession;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import java.io.Serializable;

/**
 * Created by admin123 on 6/19/2016.
 */
public class ChangePasswordPanel extends BasePanel {
    public ChangePasswordPanel() {
        this(MAIN_PANEL_ID, PrincipalUtil.getCurrentUser());
    }

    protected ChangePasswordPanel(String id, final User user) {
        super(id);

        Form userForm = new Form("form", new CompoundPropertyModel(new PasswordModel()));

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setFilter(new ContainerFeedbackMessageFilter(userForm));

        userForm.add(new Label("username", user.getUserName()));
        userForm.add(feedbackPanel);

        AjaxButton submit = new AjaxButton("submit", userForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                PasswordModel model = (PasswordModel) form.getModelObject();


                if (checkOldPassword(user) && !UserService.Instance.verifyUserPassword(user, model.oldPassword)) {
                    error(new StringResourceModel("wrong.old.password", this, null).getString());
                    return;
                }

                if (!model.newPassword.equals(model.retypedNewPassword)) {
                    error(new StringResourceModel("diffrent.new.passwords", this, null).getString());
                    return;
                }

                if (UserService.Instance.userNewPassEqualsOldPass(user, model.newPassword)) {
                    error(new StringResourceModel("equal.old.new.passwords", this, null).getString());
                    return;
                }

                UserService.Instance.changeUserPassword(user, model.newPassword);

                AppStoreSession appStoreSession = (AppStoreSession)AppStoreSession.get();

                HibernateUtil.saveOrUpdate(user);
                showMessageByKey(target, "label.saveSuccessfully");
                update(target);
            }
        };

        AjaxLink cancel = new AjaxLink("cancel") {
            public void onClick(AjaxRequestTarget target) {
                update(target);
            }
        };

        WebMarkupContainer oldPassContainer = new WebMarkupContainer("oldPassContainer");
        oldPassContainer.add(new PasswordTextField("oldPassword").setRequired(true).setLabel(new ResourceModel("label.oldPassword")));
        oldPassContainer.setVisible(checkOldPassword(user));
        userForm.add(oldPassContainer);
        userForm.add(new PasswordTextField("newPassword").setRequired(true).setLabel(new ResourceModel("label.newPassword")));
        userForm.add(new PasswordTextField("retypedNewPassword").setRequired(true).setLabel(new ResourceModel("label.confirmPassword")));
        userForm.add(submit);
        userForm.add(cancel);

        add(userForm);
    }

    private boolean checkOldPassword(User user) {
        if (PrincipalUtil.isRootUser())
            return UserService.Instance.isRoot(user);
        if (!UserService.Instance.isRoot(user) && PrincipalUtil.hasPermission(Access.CHANGE_PASSWORD))
            return user.getUserName().equals(PrincipalUtil.getCurrentUser().getUserName());
        return true;
    }

    private class PasswordModel implements Serializable {
        private String oldPassword;
        private String newPassword;
        private String retypedNewPassword;
    }

    protected void update(AjaxRequestTarget target) {}
}
