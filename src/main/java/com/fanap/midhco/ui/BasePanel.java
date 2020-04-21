package com.fanap.midhco.ui;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DayDate;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.myException.MyException;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.access.WebAction;
import com.fanap.midhco.ui.pages.BasePage;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.hibernate.exception.ConstraintViolationException;

import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.security.cert.CertificateException;
import java.util.Collection;

public abstract class BasePanel extends Panel {
    protected static final Logger logger = Logger.getLogger(BasePanel.class);
    public static final String MAIN_PANEL_ID = "mainPanel";

    private String pageTitle;
    private IParentListner parentListner;

    public BasePanel setParentListner(IParentListner parentListner) {
        this.parentListner = parentListner;
        return this;
    }

    public IParentListner getParentListner() {
        return parentListner;
    }

    protected BasePanel(String id) {
        super(id);
    }

    protected BasePanel(String id, IModel model) {
        this(id, model, null);
    }

    public BasePanel(String id, String title) {
        this(id, null, title);
    }

    // Main Constructor
    public BasePanel(String id, IModel model, String title) {
        super(id, model);
        this.pageTitle = title;
    }

    protected String getMsg(String key) {
        return getLocalizer().getString(key, this, key);
    }

    protected String getCRUDMsg(String key, String entKey) {
        return String.format("%s %s", getMsg(key), getMsg(entKey));
    }

    protected final void childFinished(AjaxRequestTarget target, IModel model, Component childComponent) {
        if (parentListner != null)
            parentListner.onChildFinished(target, model, childComponent);
        else
            throw new RuntimeException("Calling childFinish() on null parent listner!");
    }

    protected <T extends Component> T authorize(T component, WebAction webAction, Access... accesses) {
        boolean result = true;
        for (Access access : accesses) {
            result = PrincipalUtil.hasPermission(access);
            if (result)
                break;
        }
        if (!result) {
            if (webAction == WebAction.ENABLED)
                component.setEnabled(false);
            else
                component.setVisible(false);
        }
        return component;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        WebPage webPage = getWebPage();
        if (webPage instanceof BasePage) {
            BasePage basePage = (BasePage) webPage;
            basePage.setPageTitle(pageTitle);
        }
    }

    protected void showMessage(AjaxRequestTarget target, String msg) {
        target.appendJavaScript(String.format("showMessage('%s');", msg));
    }

    protected void showMessageByKey(AjaxRequestTarget target, String resourceKey) {
        showMessage(target, getLocalizer().getString(resourceKey, this));
    }

    protected void showMessageByKey(AjaxRequestTarget target, String resourceKey, Model model) {
        StringResourceModel srm = new StringResourceModel(resourceKey, this, model);
        showMessage(target, srm.getString());
    }

    protected void showAlert(AjaxRequestTarget target, String msg) {
        target.appendJavaScript(String.format("showMessage('%s');", msg));
    }

    protected void showAlertByKey(AjaxRequestTarget target, String resourceKey) {
        target.appendJavaScript(String.format("showMessage('%s');", getMsg(resourceKey)));
    }

    protected void processException(AjaxRequestTarget target, Exception e) {
        String key = "error.generalErr";
        boolean loadMessageFromKey = true;
        if(e instanceof MyException) {
            loadMessageFromKey = false;
            key = e.getMessage();
        }
        if (e instanceof java.security.InvalidKeyException)
            key = "error.invalidPublicKey";
        else if (e instanceof CertificateException)
            key = "error.invalidCertificate";
        else if(e instanceof ConstraintViolationException)
            key = "error.constraint";
        else if(e instanceof ConstraintViolationException)
            key = "error.constraint";
        else if (e instanceof MyException) {
            loadMessageFromKey = false;
            key = e.getMessage();
            logger.warn("BasePanel", e);
        } else if(e instanceof ScriptException) {
            key = "script.run.exception";
        } else
            logger.error("BasePanel", e);
        HibernateUtil.rollback();
        if (loadMessageFromKey)
            target.appendJavaScript(String.format("showMessage('%s');", getMsg(key)));
        else
            target.appendJavaScript(String.format("showMessage('%s');", key));
    }

    protected boolean checkAllFieldsNull(Object obj) {
        try {
            for (Field field : obj.getClass().getFields()) {
                Object value = field.get(obj);
                if (value == null)
                    continue;

                if (value instanceof DateTime[]) {
                    DateTime[] dts = (DateTime[]) value;
                    if (dts.length>0 )
                    if (!DateTime.isNullOrUnknown(dts[0]) || !DateTime.isNullOrUnknown(dts[1]))
                        return false;
                } else if (value instanceof DayDate[]) {
                    DayDate[] dts = (DayDate[]) value;
                    if (!DayDate.isNullOrUnknown(dts[0]) || !DayDate.isNullOrUnknown(dts[1]))
                        return false;
                } else if (value instanceof Long[]) {
                    Long[] lng = (Long[]) value;
                    if (lng[0] != null || lng[1] != null)
                        return false;
                } else if (value instanceof Collection) {
                    Collection col = (Collection) value;
                    if (col.size() > 0)
                        return false;
                } else
                    return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    protected String getFileTypeImageSrc(String fileName) {
        String fileExtension = AppUtils.getFileExtension(fileName);
        String basePath = "http://" + getRequest().getOriginalUrl().getHost() + ":" +
                getRequest().getOriginalUrl().getPort() + getRequest().getContextPath();
        if (fileExtension != null) {
            if (fileExtension.equals("apk")) {
                return basePath + "/images/fileTypes/apk.png";
            }
        }

        return basePath + "/images/fileTypes/unknown.png";
    }

    Form parentForm;
    public void setParentForm(Form parentForm) {
        this.parentForm = parentForm;
    }
    public Form getParentForm() {
        return parentForm;
    }
}
