package com.fanap.midhco.ui.pages.role;

import com.fanap.midhco.appstore.entities.Role;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.security.AccessService;
import com.fanap.midhco.appstore.service.user.RoleService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.treeview.AccessTreeViewPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Created by admin123 on 6/22/2016.
 */
public class RoleForm extends BasePanel {
    public RoleForm(String id, Role role) {
        super(id, "CreateRolePage.title");

        final boolean isNewRole = role.getId() == null;

        FeedbackPanel errorfeedback = new FeedbackPanel("feedbackPabel");
        errorfeedback.setOutputMarkupId(true);
        add(errorfeedback);

        Form form = new Form("form", new CompoundPropertyModel(role));
        form.add(new RequiredTextField("name").setLabel(new ResourceModel("Role.name")));
        if(role.getEditable() == null || !role.getEditable()) {
            form.get("name").setEnabled(false);
            form.get("name").isEnabled();
        }

        final TextField roleNameField = new TextField("name");
        final AccessTreeViewPanel permissions = new AccessTreeViewPanel("accessCodes", false, null);
        permissions.setModel(new Model(AccessService.getPermissionsBytes(role.getAccessCodes())));
        form.add(permissions);

        AjaxButton submit = new AjaxFormButton("save", form, errorfeedback) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                Session session = HibernateUtil.getCurrentSession();
                Transaction tx = null;
                Role role = (Role) form.getModelObject();

            String validationString = "";
            boolean inValidRoleName =RoleService.Instance.roleNameExist(role,session);
            if (inValidRoleName) {
                validationString += " - " +
                        getString("error.role.exist") + "  <br/>";
            }

            if (!validationString.isEmpty()) {
                target.appendJavaScript("showMessage('" + validationString + "');");
                return;
            }

                role.setLastModifyDate(DateTime.now());
                role.setCreatorUser(PrincipalUtil.getCurrentUser());
                if(permissions.getConvertedInput() != null)
                    role.setAccessCodes(AccessService.encode((byte[])permissions.getConvertedInput()));
                else
                    role.setAccessCodes(null);
                try {
                    tx = session.beginTransaction();
                    if (isNewRole)
                        role.setId(null);

                Role loadedRole = role.getId()==null ? new Role() : (Role) session.load(Role.class , role.getId());
                loadedRole.setAccessCodes(role.getAccessCodes());
                loadedRole.setEditable(role.getEditable());
                loadedRole.setName(role.getName());


                RoleService.Instance.saveOrUpdate(loadedRole, session);
                    HibernateUtil.endTransaction();
                    childFinished(target, null, this);
                } catch (Exception ex) {
                    if(tx != null && !tx.isActive()) {
                        logger.debug("rolling back transaction!", ex);
                        tx.rollback();
                    }
                    processException(target, ex);
                }
            }
        };

        AjaxLink cancel = new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                childFinished(ajaxRequestTarget, null, null);
            }
        };

        form.add(submit);
        form.add(cancel);
        add(form);
    }
}
