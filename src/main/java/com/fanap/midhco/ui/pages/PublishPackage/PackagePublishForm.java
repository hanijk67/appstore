package com.fanap.midhco.ui.pages.PublishPackage;

import com.fanap.midhco.appstore.applicationUtils.DateUtil;
import com.fanap.midhco.appstore.applicationUtils.MyCalendarUtil;
import com.fanap.midhco.appstore.entities.App;
import com.fanap.midhco.appstore.entities.AppPackage;
import com.fanap.midhco.appstore.entities.PackagePublish;
import com.fanap.midhco.appstore.entities.helperClasses.*;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.packagePublish.PackagePublishService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.dateTimePanel.DateTimePanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * Created by A.Moshiri on 10/28/2017.
 */
public class PackagePublishForm extends BasePanel {
    Form form;
    FeedbackPanel feedbackPanel;
    DateTimePanel publishDateTimePanel;
    PackagePublish insertedPackagePublish;


    public PackagePublishForm(String id, App app, AppPackage appPackage) {
        super(id);
        feedbackPanel = new FeedbackPanel("feedbackPanel");
        add(feedbackPanel);
        form = new Form("form");
        PackagePublish packagePublish = new PackagePublish();
        if (app != null && app.getId() != null && appPackage != null && appPackage.getId() != null) {
            PackagePublishService.PackagePublishCriteria packJobCriteria = new PackagePublishService.PackagePublishCriteria();
            packJobCriteria.appId = app.getId();
            packJobCriteria.packId = appPackage.getId();
            packJobCriteria.isApplied = false;
            List<PackagePublish> packagePublishList = null;
            try {
                packagePublishList = PackagePublishService.Instance.list(packJobCriteria, 0, -1);
                if (packagePublishList != null && !packagePublishList.isEmpty()) {
                    packagePublish = packagePublishList.get(0);
                }
                insertedPackagePublish = packagePublish;
            } catch (Exception e) {
                logger.error(e.getMessage());
                return;
            }
        }

        publishDateTimePanel = new DateTimePanel("publishDateTime", DateType.DateTime, HourMeridianType._24HOUR);
        publishDateTimePanel.setLabel(new ResourceModel("AppPackage.publishDate"));
        publishDateTimePanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        if (packagePublish.getPublishDateTime() != null) {
            DateTime persianDateTime = MyCalendarUtil.toPersian(packagePublish.getPublishDateTime());
            publishDateTimePanel.setModel(new Model(persianDateTime));
        } else {
            publishDateTimePanel.setModel(new Model());
        }
        form.add(publishDateTimePanel);

        form.add(new AjaxFormButton("savePublishDate", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                Session session = HibernateUtil.getCurrentSession();
                DateTime publishDateTime = null;
                try {
                    String validationString = "";
                    if (publishDateTimePanel == null || publishDateTimePanel.getConvertedInput() == null) {
                        validationString += " - " +
                                getString("Required").replace("${label}", publishDateTimePanel.getLabel().getObject()) + "<br/>";
                    } else {
                        publishDateTime = (DateTime) publishDateTimePanel.getConvertedInput();

                        DateTime minDateTime = new DateTime(DayDate.MIN_DAY_DATE, DayTime.MIN_DAY_TIME);


                        DateTime currentDateTime = DateTime.now();

                        if (  publishDateTime.compareTo(minDateTime) == 0) {
                            validationString += " - " +
                                    getString("Required").replace("${label}", publishDateTimePanel.getLabel().getObject()) + "<br/>";
                        } else if (publishDateTime.compareTo(currentDateTime) < 0) {
                            validationString += " - " +
                                    getString("error.dayAndTime.validation.less").replace("${first}", publishDateTimePanel.getLabel().getObject()).replace("${second}", getString("label.current.date")) + "<br/>";
                        }
                    }
                    if (!validationString.isEmpty()) {
                        target.appendJavaScript("showMessage('" + validationString + "');");
                        return;
                    }

                    Transaction tx = null;
                    try {
                        tx = session.beginTransaction();

                        insertedPackagePublish.setApplied(false);
                        insertedPackagePublish.setAppId(app.getId());
                        insertedPackagePublish.setPackId(appPackage.getId());

                        insertedPackagePublish.setPublishDateTime(new DateTime(DateUtil.localToGMT(publishDateTime)));
                        logger.error("ghost publish data is "+ publishDateTime);
                        logger.error("ghost gmtpublish data is "+ insertedPackagePublish.getPublishDateTime());

                        PackagePublishService.Instance.saveOrUpdate(insertedPackagePublish, session);
                        App loadedApp = (App) session.load(App.class, app.getId());
                        loadedApp.setHasScheduler(true);
                        AppService.Instance.saveOrUpdate(loadedApp, session);
                        tx.commit();

                        childFinished(target, new Model<>(loadedApp), this);
                    } catch (Exception ex) {
                        if (tx != null)
                            tx.rollback();
                        processException(target, ex);
                    } finally {
                        if (session.isOpen())
                            session.close();
                        return;
                    }

                } catch (Exception e) {
                    processException(target, e);
                }
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
