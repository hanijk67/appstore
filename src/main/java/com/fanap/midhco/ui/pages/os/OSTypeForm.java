package com.fanap.midhco.ui.pages.os;

import com.fanap.midhco.appstore.entities.OSType;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.IAPPPackageService;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.component.ace.AceConfig;
import com.fanap.midhco.ui.component.ace.AceEditorField;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import groovy.lang.GroovyClassLoader;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.codehaus.groovy.control.CompilationFailedException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Comparator;
import java.util.zip.ZipInputStream;

/**
 * Created by admin123 on 6/28/2016.
 */
@Authorize(views = {Access.OSTYPE_ADD, Access.OSTYPE_EDIT})
public class OSTypeForm extends BasePanel {
    Form form;
    FeedbackPanel feedbackPanel;
    SwitchBox disabledSwitchBox;
    LimitedTextField nameTextField;
    protected OSTypeForm(String id, OSType osType) {
        super(id);

        feedbackPanel = new FeedbackPanel("feedbackPabel");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        form = new Form("form", new CompoundPropertyModel(osType));
        form.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        Boolean hasDefaultState = null;
        if (osType != null && osType.getDisabled() != null) {
            hasDefaultState = osType.getDisabled();
        }
        nameTextField = new LimitedTextField("name",true ,false,false,false,false,40,getString("OSType.name"));
        nameTextField.setRequired(true);
        nameTextField.setLabel(new ResourceModel("OSType.name"));
        form.add(nameTextField);

        disabledSwitchBox = new SwitchBox( hasDefaultState, "disable", getString("label.yes"), getString("label.no"));
        disabledSwitchBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        disabledSwitchBox.setLabel(new ResourceModel("label.disable.verb"));
        disabledSwitchBox.setModel(new Model<>());
        form.add(disabledSwitchBox);

        AceConfig options = new AceConfig();
        options.setMode("groovy");
        final AceEditorField editorField = new AceEditorField<String>("osCompareScript", options);
        editorField.setLabel(new ResourceModel("OS.osCompareScript"));
        editorField.setRequired(true);

        form.add(editorField);

        form.add(new AbstractFormValidator() {
            @Override
            public FormComponent<?>[] getDependentFormComponents() {
                return new FormComponent<?>[0];
            }

            @Override
            public void validate(Form<?> form) {
                String editorFieldScript = (String) editorField.getConvertedInput();
                if (editorFieldScript != null && !editorFieldScript.trim().isEmpty()) {
                    try {
                        Class scriptClass = new GroovyClassLoader().parseClass(editorFieldScript);
                        Method parseMethod = scriptClass.getMethod("parse", String.class);
                        if (!Modifier.isStatic(parseMethod.getModifiers())) {
                            error(editorField, "OSType.editorField.parseMethod.isNotStatic");
                        }

                        if (!(parseMethod.getReturnType().isAssignableFrom(IAPPPackageService.class))) {
                            error(editorField, "OSType.editorField.parseMethod.returnType");
                        }

                        Method getUploadFilterMethod = scriptClass.getMethod("getUploadFilter");
                        if (!Modifier.isStatic(getUploadFilterMethod.getModifiers())) {
                            error(editorField, "OSType.editorField.getUploadFilterMethod.isNotStatic");
                        }

                        if (!(getUploadFilterMethod.getReturnType().isAssignableFrom(IUploadFilter.class))) {
                            error(editorField, "OSType.editorField.getUploadFilterMethod.returnType");
                        }

                        Method getVersionComaparatorMethod = scriptClass.getMethod("getVersionComparator");
                        if (!Modifier.isStatic(getVersionComaparatorMethod.getModifiers())) {
                            error(editorField, "OSType.editorField.getVersionComaparator.isNotStatic");
                        }

                        if (!(getVersionComaparatorMethod.getReturnType().isAssignableFrom(Comparator.class))) {
                            error(editorField, "OSType.editorField.getVersionComaparator.returnType");
                        }

                        Method IsDeltaUpdatableMethod = scriptClass.getMethod("IsDeltaUpdatable");
                        if (!Modifier.isStatic(IsDeltaUpdatableMethod.getModifiers())) {
                            error(editorField, "OSType.editorField.getIsDeltaUpdatable.isNotStatic");
                        }

                        if (!(IsDeltaUpdatableMethod.getReturnType().isAssignableFrom(boolean.class))) {
                            error(editorField, "OSType.editorField.getIsDeltaUpdatable.returnType");
                        }

                        Method getDeltaPackageMethod = scriptClass.getMethod("getDeltaPackage", URL.class, URL.class);
                        if (!Modifier.isStatic(getDeltaPackageMethod.getModifiers())) {
                            error(editorField, "OSType.editorField.getDeltaPackage.isNotStatic");
                        }

                        Method checkFileExistenceInPackage = scriptClass.getMethod("checkFileExistenceInPackage", ZipInputStream.class, String.class);
                        if (!Modifier.isStatic(checkFileExistenceInPackage.getModifiers())) {

                            //todo send error
                            //  error(editorField, "OSType.editorField.checkFileExistenceInPackage.isNotStatic");
                        }


                        if (!(getDeltaPackageMethod.getReturnType().isAssignableFrom(String.class))) {
                            error(editorField, "OSType.editorField.getDeltaPackage.returnType");
                        }

                        return;
                    } catch (CompilationFailedException ex) {
                        error(editorField, "OSType.editorField.compilationFailed");
                        return;
                    } catch (Exception ex) {
                        if (ex instanceof NoSuchMethodException) {
                            if(ex.getMessage().contains("parse")) {
                                error(editorField, "OSType.editorField.parseMethod.not.present");
                            } else if(ex.getMessage().contains("getUploadFilterMethod")) {
                                error(editorField, "OSType.editorField.getUploadFilter.not.present");
                            }  else if (ex.getMessage().contains("getUploadFilter")) {
                                error(editorField, "OSType.editorField.getUploadFilter.not.present");
                            } else if(ex.getMessage().contains("getVersionComparator")) {
                                error(editorField, "OSType.editorField.getVersionComparator.not.present");
                            } else if(ex.getMessage().contains("verifyPackage")) {
                                error(editorField, "OSType.editorField.verifyPackage.not.present");
                            } else if(ex.getMessage().contains("IsDeltaUpdatable")) {
                                error(editorField, "OSType.editorField.IsDeltaUpdatable.not.present");
                            } else if(ex.getMessage().contains("getDeltaPackage")) {
                                error(editorField, "OSType.editorField.getDeltaPackage.not.present");
                            }
                            return;
                        }
                    }
                    error(editorField, "IConverter");
                }
            }
        });


        form.add(new AjaxFormButton("save", form, feedbackPanel) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                String validationString = "";
                if (nameTextField != null && nameTextField.getValidatorString() != null && !nameTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : nameTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }


                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }


                editorField.getConvertedInput();
                boolean disabled = disabledSwitchBox == null ? false : (boolean) disabledSwitchBox.getConvertedInput();
                OSType osTypeToSave = (OSType) form.getModelObject();
                osTypeToSave.setDisabled(disabled);

                Session session = HibernateUtil.getCurrentSession();
                Transaction tx = null;

                try {
                    tx = session.beginTransaction();
                    OSTypeService.Instance.saveOrUpdate(osTypeToSave, session);
                    tx.commit();
                } catch (Exception ex) {
                    if (tx != null && tx.isActive())
                        tx.rollback();
                    processException(target, ex);
                } finally {
                    if(session.isOpen())
                        session.close();
                }

                childFinished(target, null, this);
            }
        });

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, this);
            }
        });

        add(form);
    }
}
