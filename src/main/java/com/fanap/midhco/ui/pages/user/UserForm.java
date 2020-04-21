package com.fanap.midhco.ui.pages.user;

import com.fanap.midhco.appstore.entities.Role;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.entities.helperClasses.UserStatus;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.login.SSOUserService;
import com.fanap.midhco.appstore.service.myException.SSOServerException;
import com.fanap.midhco.appstore.service.user.RoleService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.component.ISelectable;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.selectionpanel.SelectionPanel;
import com.fanap.midhco.ui.pages.role.RoleList;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by admin123 on 2/6/15.
 */
@Authorize(views = {Access.USER_CREATE, Access.USER_EDIT})
public class UserForm extends BasePanel {
    Form form;
    FeedbackPanel feedbackPanel;
    boolean hasId = false;
    Long userIdFromSSO = null;

    public UserForm(String id, User user) {
        super(id);

        hasId = user.getId() != null;

        feedbackPanel = new FeedbackPanel("feedbackPabel");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        form = new Form("form", new CompoundPropertyModel(user));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        final TextField userNameField = new TextField("userName");
        userNameField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        userNameField.setRequired(true);
        userNameField.setLabel(new ResourceModel("User.userName"));
        form.add(userNameField);

        final LimitedTextField firstNameField = new LimitedTextField("contact.firstName", null, false, false, true, false, 40, getString("Contact.firstName"));

        firstNameField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        firstNameField.setRequired(true);
        firstNameField.setLabel(new ResourceModel("Contact.firstName"));
        form.add(firstNameField);

        final LimitedTextField lastNameField = new LimitedTextField("contact.lastName", null, false, false, true, false, 40, getString("Contact.lastName"));

        lastNameField.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        lastNameField.setRequired(true);
        lastNameField.setLabel(new ResourceModel("Contact.lastName"));
        form.add(lastNameField);

        UserStatus chosenStatus = user.getUserStatus();
        final MyDropDownChoicePanel statusDropDown =
                new MyDropDownChoicePanel("userStatus", UserStatus.listAll(),
                        false, true, getMsg("label.select"), 1, false, null);
        statusDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        statusDropDown.setRequired(true);
        statusDropDown.setLabel(new ResourceModel("User.userStatus"));
        form.add(statusDropDown);

        List<Role> roles = null;
        if (hasId)
            roles = new ArrayList<Role>(user.getRoles());

        SelectionPanel rolesPanel = new SelectionPanel("roles", SelectionMode.Multiple) {
            @Override
            public ISelectable getSelectable(String panelId) {
                return new RoleList(panelId, new RoleService.RoleCriteria(), SelectionMode.Multiple);
            }
        };


        rolesPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        rolesPanel.setRequired(true);
        rolesPanel.setModel(new Model((Serializable) roles));
        rolesPanel.setLabel(new ResourceModel("User.roles"));
        form.add(rolesPanel);

        form.add(new AbstractFormValidator() {
            @Override
            public FormComponent[] getDependentFormComponents() {
                return new FormComponent[0];
            }

            @Override
            public void validate(Form form) {


            }
        });

        form.add(new AjaxFormButton("save", form, feedbackPanel) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                String validationString = "";
                if (firstNameField != null && firstNameField.getValidatorString() != null && !firstNameField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : firstNameField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (lastNameField != null && lastNameField.getValidatorString() != null && !lastNameField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : lastNameField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

//                if (userNameField != null && userNameField.getValidatorString() != null && !userNameField.getValidatorString().isEmpty()) {
//                    for (String validationStringInList : userNameField.getValidatorString()) {
//                        validationString += " - " +
//                                validationStringInList + "<br/>";
//                    }
//                }
                if (validationString.isEmpty()) {
                    if (userNameField != null && !userNameField.toString().trim().equals("") && (userNameField.getConvertedInput().toString().contains("]") || userNameField.getConvertedInput().toString().contains("["))) {
                        validationString += " - " +
                                getString("invalid.data.char").replace("${label}", userNameField.getLabel().getObject()) + "<br/>";
                    }

                    if (firstNameField != null && !firstNameField.toString().trim().equals("") && (firstNameField.getConvertedInput().toString().contains("]") || firstNameField.getConvertedInput().toString().contains("["))) {
                        validationString += " - " +
                                getString("invalid.data.char").replace("${label}", firstNameField.getLabel().getObject()) + "<br/>";
                    }

                    if (lastNameField != null && !lastNameField.toString().trim().equals("") && (lastNameField.getConvertedInput().toString().contains("]") || lastNameField.getConvertedInput().toString().contains("["))) {
                        validationString += " - " +
                                getString("invalid.data.char").replace("${label}", lastNameField.getLabel().getObject()) + "<br/>";
                    }
                }
                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }

//                if (!hasId) {
                String userName = (String) userNameField.getConvertedInput();
                if (userName != null && !userName.trim().isEmpty()) {
                    Session session = HibernateUtil.getCurrentSession();
                    User user = UserService.Instance.findUser(userName, session);
                    if (user != null) {
                        User inputUser = (User) form.getModelObject();
                        if (!user.getId().equals(inputUser.getId())) {
                            validationString += " - " +
                                    AppStorePropertyReader.getString("User.userNameExists") + "<br/>";
                        }
                    }
                    try {
                        userIdFromSSO = SSOUserService.Instance.getUserIdByUserName(userName);
                        if (userIdFromSSO.compareTo(Long.valueOf(1)) == -1) {
                            validationString += " - " +
                                    AppStorePropertyReader.getString("User.userNameExists") + "<br/>";

                        }
                    } catch (SSOServerException e) {
                        logger.error("error getting user Id from SSO service ", e);
                        if (e.getErrorMessage() != null && !e.getErrorMessage().trim().isEmpty()) {
                            if (e.getErrorMessage().equals(AppStorePropertyReader.getString("sso.invalid.request")) || e.getErrorMessage().equals(AppStorePropertyReader.getString("sso.userName.notFound"))) {
                                validationString += " - " +
                                        AppStorePropertyReader.getString("User.userName.notExistInSSOServer") + "<br/>";


                            } else {
                                validationString += " - " +
                                        AppStorePropertyReader.getString("error.generalErr") + "<br/>";
                            }

                        }
                    } catch (Exception e) {
                        logger.error("general error occurred in  getting user Id from SSO service ", e);
                        form.error(e.getMessage());
                    }
                }
//                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }


                Session session = HibernateUtil.getCurrentSession();
                Transaction tx = null;
                try {
                    tx = session.beginTransaction();

                    User user = (User) form.getModelObject();
//                    if (!hasId)
                    user.setUserId(userIdFromSSO);
                    Collection<Role> roleSet = (Collection<Role>) rolesPanel.getConvertedInput();
                    user.setRoles(new HashSet<Role>(roleSet));

                    User tmpUser = user == null || user.getId() == null ? new User() : (User) session.load(User.class, user.getId());

                    tmpUser.setContact(user.getContact());
                    tmpUser.setUserId(user.getUserId());
                    tmpUser.setUserStatus(user.getUserStatus());
                    tmpUser.setRoles(user.getRoles());
                    tmpUser.setAllAllowedPermissions(user.getAllAllowedPermissions());
                    tmpUser.setAllDeniedPermissions(user.getAllDeniedPermissions());
                    tmpUser.setGender(user.getGender());
                    tmpUser.setLastIp(user.getLastIp());
                    tmpUser.setLastLoginDate(user.getLastLoginDate());
                    tmpUser.setLogged(user.isLogged());
                    tmpUser.setNumOfWrongTries(user.getNumOfWrongTries());
                    tmpUser.setPassword(user.getPassword());
                    tmpUser.setPasswordSalt(user.getPasswordSalt());
                    tmpUser.setPublicPrivatePair(user.getPublicPrivatePair());
                    tmpUser.setUserName(user.getUserName());
                    tmpUser.setLastModifyUser(user.getLastModifyUser());


                    UserService.Instance.saveOrUpdate(tmpUser, session);


                    tx.commit();
                    childFinished(target, null, form.get("save"));
                } catch (Exception ex) {
                    if (tx != null)
                        tx.rollback();
                    processException(target, ex);
                } finally {
                    try {
                        session.close();
                    } catch (Exception e) {
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
