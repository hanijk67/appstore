package com.fanap.midhco.ui.pages.environment;

import com.fanap.midhco.appstore.entities.OSEnvironment;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Created by A.Moshiri on 2/26/2018.
 */
@Authorize(views = {Access.ENVIRONMENT_ADD, Access.ENVIRONMENT_EDIT})
public class EnvironmentForm extends BasePanel {
    Form form;
    FeedbackPanel feedbackPanel;
    LimitedTextField envNameTextField;

    protected EnvironmentForm(String id, OSEnvironment environment) {
        super(id);


        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        form = new Form("form", new CompoundPropertyModel(environment));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);


        envNameTextField = new LimitedTextField("envName",true , false,false,false,false,30,getString("osEnvironment.name"));
        envNameTextField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        envNameTextField.setRequired(true);
        envNameTextField.setLabel(new ResourceModel("osEnvironment.name"));
        form.add(envNameTextField);


        form.add(new AjaxFormButton("save", form, feedbackPanel) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                String validationString = "";
                Session session = HibernateUtil.getCurrentSession();

                try {

                    if (environment.getEnvName() == null || environment.getEnvName().trim().equals("")) {
                        validationString += " - " +
                                getString("Required").replace("${label}", getString("osEnvironment.name")) + "<br/>";
                    }

                } catch (Exception e) {
                    validationString += " - " +
                            getString("error.generalErr") + "<br/>";
                }

                if (envNameTextField != null && envNameTextField.getValidatorString() != null && !envNameTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : envNameTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }

                try {
                    Transaction tx = null;
                    try {
                        OSEnvironment envToSave = null;
                            tx = session.beginTransaction();
                            session.evict(environment);
                            envToSave = environment.getId() != null ? (OSEnvironment) session.load(OSEnvironment.class, environment.getId()) : new OSEnvironment();
                            envToSave.setEnvName(environment.getEnvName());
                            BaseEntityService.Instance.saveOrUpdate(envToSave, session);
                            tx.commit();

                        childFinished(target, new Model<>(envToSave), form.get("save"));

                    } catch (Exception ex) {
                        processException(target, ex);
                    }

                } finally {
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            }
        });

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, form.get("cancel"));
            }
        });

        add(form);

    }
}
