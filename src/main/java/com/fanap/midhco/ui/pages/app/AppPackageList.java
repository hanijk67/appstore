package com.fanap.midhco.ui.pages.app;

import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppHistoryService;
import com.fanap.midhco.appstore.service.app.AppPackageHistoryService;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.packagePublish.PackagePublishService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import com.fanap.midhco.ui.pages.PublishPackage.PackagePublishForm;
import com.fanap.midhco.ui.pages.test.TestIssueList;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.proxy.HibernateProxyHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Hamid on 7/8/2016.
 */
public class AppPackageList extends BasePanel implements IParentListner {
    MyAjaxDataTable table;
    AppPackageDataProvider dp = new AppPackageDataProvider();
    BootStrapModal modal = new BootStrapModal("modal");
    WebMarkupContainer addTestIssuePanel;
    TestIssueList testIssueForm;
    AppPackage inputAppPackage;
    WebMarkupContainer masterContainer;
    String selectedCell ="";


    private Panel replacedPanel;

    protected AppPackageList(String id, SelectionMode selectionMode, String packageName, OSType osType) {
        super(id);
        masterContainer = new WebMarkupContainer("masterContainer");
        masterContainer.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        TestIssueList testIssueList = new TestIssueList("content", new AppPackage(), SelectionMode.Single);
        masterContainer.add(testIssueList).setVisible(false);
        add(masterContainer);

        add(modal);

        AppPackageService.Criteria criteria = new AppPackageService.Criteria();
        criteria.appPackageName = packageName;
        criteria.osType = osType;
        criteria.isDeleted = false;
        dp.setCriteria(criteria);

        table = new MyAjaxDataTable("table", getColumns(), dp, 10);
        table.setSelectionMode(selectionMode);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        addTestIssuePanel = new WebMarkupContainer("addTestIssuePanel");
        addTestIssuePanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        addTestIssuePanel.setVisible(false);

        add(table);
    }

    public List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("APPPackage.id"), null, "appPackage.id"));
        columnList.add(new PropertyColumn(new ResourceModel("APPPackage.versionCode"), null, "appPackage.versionCode"));
        columnList.add(new PropertyColumn(new ResourceModel("APPPackage.versionName"), null, "appPackage.versionName"));
        columnList.add(new PropertyColumn(new ResourceModel("APPPackage.publishState"), null, "appPackage.publishState"));

        columnList.add(new AbstractColumn(new ResourceModel("App.mainPackage")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                AppPackageService.AppPackageSearchResult searchResult =
                        (AppPackageService.AppPackageSearchResult)rowModel.getObject();
                App app = searchResult.getApp();
                AppPackage appPackage = searchResult.getAppPackage();

                Label label = new Label(componentId, new Model(""));
                if (searchResult.getIsMain())
                    label.add(new AttributeModifier("class", new Model("entypo-check")));
                else
                    label.add(new AttributeModifier("class", new Model("entypo-minus")));
                cellItem.add(label);
            }
        });

        if (PrincipalUtil.hasPermission(Access.APP_PUBLISH_UNPUBLISH)) {

            columnList.add(new AbstractColumn(new Model<String>("")) {
                @Override
                public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                    final AppPackageService.AppPackageSearchResult searchResult =
                            (AppPackageService.AppPackageSearchResult)rowModel.getObject();
                    String imagePath = "images/table/like.png";
                    String action = getString("AppPackage.publishState.action");
                    final PublishState publishState = searchResult.getAppPackage().getPublishState();
                    if(publishState.equals(PublishState.PUBLISHED)) {
                        imagePath = "images/table/unlike.png";
                        action = getString("AppPackage.unPublishState.action");
                    }

                    cellItem.add(new AjaxLinkPanel(componentId, imagePath, new Model<>(action)) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            App app = searchResult.getApp();
                            AppPackage appPackage = searchResult.getAppPackage();

                            Session session = HibernateUtil.getCurrentSession();
                            session.clear();
                            Transaction tx = null;

                            try {
                                tx = session.beginTransaction();
                                AppPackage loadedAppPackage = (AppPackage) session.load(AppPackage.class , appPackage.getId());
//                                if (publishState.equals(PublishState.PUBLISHED)) {
//                                    loadedAppPackage.setPublishState(PublishState.UNPUBLISHED);
//                                } else {
//                                    loadedAppPackage.setPublishState(PublishState.PUBLISHED);
//                                }

//                                appPackage = AppPackageHistoryService.Instance.setAppPackageHistoryForAppPackage( session, loadedAppPackage);
                                app = (App)session.load(App.class, app.getId());

                                if(publishState.equals(PublishState.PUBLISHED)) {
                                    appPackage = (AppPackage)session.load(AppPackage.class, appPackage.getId());
                                    appPackage.setPublishState(PublishState.UNPUBLISHED);
                                    AppPackageService.Instance.saveOrUpdate(appPackage, session);
                                } else {
                                    PackagePublishService.PackagePublishCriteria packagePublishCriteria = new PackagePublishService.PackagePublishCriteria();
                                    packagePublishCriteria.appId = app.getId();
                                    packagePublishCriteria.packId = appPackage.getId();
                                    packagePublishCriteria.isApplied = false;
                                    List<PackagePublish> packagePublishList = PackagePublishService.Instance.list(packagePublishCriteria, 0, -1, session);
                                    if(packagePublishList!=null && !packagePublishList.isEmpty() ){
                                        PackagePublish packagePublish = packagePublishList.get(0);
                                        PackagePublish loadedPackagePublish = (PackagePublish) session.load(PackagePublish.class , packagePublish.getId());
                                        loadedPackagePublish.setApplied(true);
                                        app.setHasScheduler(false);
                                        PackagePublishService.Instance.saveOrUpdate(loadedPackagePublish , session);
                                    }

                                    appPackage.setPublishState(PublishState.PUBLISHED);

                                    AppPackageService.Instance.publishAppPackage(
                                            app, appPackage, session);
                                }
                                AppService.Instance.saveOrUpdate(app, session);


                                tx.commit();

                                target.add(table);
                            } catch (Exception ex) {
                                if(tx != null)
                                    tx.rollback();

                                if(ex instanceof AppPackageService.APPPackageException)
                                    target.appendJavaScript("showMessage('" + ex.getMessage() + "');");
                                else
                                    processException(target, ex);
                            }
                        }
                    }.setConfirmationMessage("AppPackage.publishState.confirm"));
                }
            });

            columnList.add(new AbstractColumn(new Model<String>("")) {
                @Override
                public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                    final Session[] session = {HibernateUtil.getCurrentSession()};

                    final AppPackageService.AppPackageSearchResult searchResult =
                            (AppPackageService.AppPackageSearchResult) rowModel.getObject();

                    App app = searchResult.getApp();
                    App loadedApp = (App) session[0].load(App.class, app.getId());
                    AppPackage appPackage = searchResult.getAppPackage();


                    Boolean existedPackageInPackagePublish = null;
                    Boolean emptyPackagePublishForAppId = true;
                    try {
                        PackagePublishService.PackagePublishCriteria packagePublishCriteria = new PackagePublishService.PackagePublishCriteria();
                        packagePublishCriteria.appId = app.getId();
                        packagePublishCriteria.packId = appPackage.getId();
                        packagePublishCriteria.isApplied = false;
                        existedPackageInPackagePublish = PackagePublishService.Instance.isExisted(packagePublishCriteria, session[0]);
                        PackagePublishService.PackagePublishCriteria packagePublishCriteriaForCheckEmpty = new PackagePublishService.PackagePublishCriteria();
                        packagePublishCriteriaForCheckEmpty.appId = packagePublishCriteria.appId;

                        List<PackagePublish> appIdList = PackagePublishService.Instance.list(packagePublishCriteriaForCheckEmpty, 0, -1, session[0]);
                        if (appIdList != null && !appIdList.isEmpty()) {
                            emptyPackagePublishForAppId = false;
                        }


                        if (app != null && !appPackage.getPublishState().equals(PublishState.PUBLISHED)) {
                            if (app.getHasScheduler() == null || (app.getHasScheduler() != null && !app.getHasScheduler())) {
                                cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.SetScheduler) {
                                    @Override
                                    public void onClick(AjaxRequestTarget target) {
                                        App app = searchResult.getApp();
                                        app.setHasScheduler(true);
                                        try {
                                            PackagePublishForm publishPackageForm = new PackagePublishForm(modal.getContentId(), app, appPackage);
                                            publishPackageForm.setParentListner(AppPackageList.this);
                                            modal.setTitle(getString("AppPackage.publishDate"));
                                            modal.setContent(publishPackageForm);
                                            modal.show(target);
                                        } catch (Exception ex) {
                                            if (ex instanceof AppPackageService.APPPackageException)
                                                target.appendJavaScript("showMessage('" + ex.getMessage() + "');");
                                            else
                                                processException(target, ex);
                                        }
                                    }
                                });
                            } else if (emptyPackagePublishForAppId || existedPackageInPackagePublish) {
                                cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.UnSetScheduler) {
                                    @Override
                                    public void onClick(AjaxRequestTarget target) {
                                        if(!session[0].isOpen()){
                                            session[0] = HibernateUtil.getCurrentSession();
                                        }
                                        App loadedAppForScheduling = (App) session[0].get(App.class,loadedApp.getId());

                                        if (loadedAppForScheduling!=null) {
                                            loadedAppForScheduling.setHasScheduler(false);
//                                            loadedApp.setHasScheduler(false);
                                            Transaction tx = null;
                                            Session session = HibernateUtil.getCurrentSession();
                                            tx = session.beginTransaction();
                                            try {
                                                List<PackagePublish> packPublishJobList = null;
                                                packPublishJobList = PackagePublishService.Instance.list(packagePublishCriteria, 0, -1);
                                                if (packPublishJobList != null && !packPublishJobList.isEmpty()) {
                                                    PackagePublish loadedPackagePublish = packPublishJobList.get(0);
                                                    loadedPackagePublish.setApplied(true);
                                                    PackagePublishService.Instance.saveOrUpdate(loadedPackagePublish, session);
                                                }
                                                AppService.Instance.saveOrUpdate(loadedAppForScheduling, session);
                                                tx.commit();
                                                target.add(table);

                                            } catch (Exception e) {
                                                tx.rollback();
                                                logger.error(AppStorePropertyReader.getString("error.generalErr"));
                                                return;
                                            }
                                        }
                                    }
                                });
                            } else {
                                cellItem.add(new Label(componentId, new Model("")));
                            }
                        } else {
                            cellItem.add(new Label(componentId, new Model("")));
                        }

                    } catch (Exception e) {
                        logger.error(AppStorePropertyReader.getString("error.generalErr"));
                        return;
                    }
                }
            });
        }

        columnList.add(new AbstractColumn(new Model<String>("")) {
            @Override
            public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                if(PrincipalUtil.hasPermission(Access.APP_EDIT)) {
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            AppPackageService.AppPackageSearchResult appPackageSearchResult =
                                    (AppPackageService.AppPackageSearchResult)rowModel.getObject();

                            Session session = HibernateUtil.getCurrentSession();
                            AppPackage appPackage = (AppPackage)session.load(AppPackage.class, appPackageSearchResult.getAppPackage().getId());
                            App app = (App)session.load(HibernateProxyHelper.getClassWithoutInitializingProxy(appPackageSearchResult.getApp()), appPackageSearchResult.getApp().getId());


                            PackageEditForm packageEditForm =
                                    new PackageEditForm(modal.getContentId(), appPackage, app);
                            packageEditForm.setParentListner(AppPackageList.this);
                            modal.setContent(packageEditForm);
                            modal.setTitle(getString("appPackage.editForm"));
                            modal.show(target);
                        }
                    });
                } else {
                    cellItem.add(new Label(componentId, new Model("")));
                }
            }
        });

        columnList.add(new AbstractColumn(new Model<String>("")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                if (PrincipalUtil.hasPermission(Access.TEST_LIST)) {
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Test) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            AppPackageService.AppPackageSearchResult appPackageSearchResult =
                                    (AppPackageService.AppPackageSearchResult) rowModel.getObject();

                            Session session = HibernateUtil.getCurrentSession();
                            AppPackage appPackage = (AppPackage) session.load(AppPackage.class, appPackageSearchResult.getAppPackage().getId());
                            inputAppPackage = appPackage;
                            TestIssueList testIssueList = new TestIssueList(modal.getContentId(), appPackage, SelectionMode.Single);
                            masterContainer.setVisible(true);
                            masterContainer.get("content").setVisible(true);
                            if (selectedCell.equals(cellItem.getId())) {
                                masterContainer.get("content").replaceWith(new WebMarkupContainer("content"));
                                selectedCell = "";
                            } else {
                                masterContainer.get("content").replaceWith(testIssueList);
                                selectedCell = cellItem.getId();
                            }
                            target.add(masterContainer);
                        }
                    });
                } else {
                    cellItem.add(new Label(componentId, new Model<>("")));
                }
            }
        });


        columnList.add(new AbstractColumn(new Model("")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                final AppPackageService.AppPackageSearchResult searchResultModel = (AppPackageService.AppPackageSearchResult) rowModel.getObject();
                cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.History) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        if (searchResultModel.getAppPackage()!=null && searchResultModel.getAppPackage().getId()!=null) {
                            Session session = HibernateUtil.getCurrentSession();
                            AppPackage loadedAppPackage= (AppPackage) session.load(AppPackage.class , searchResultModel.getAppPackage().getId());
                            AppPackageHistoryForm appPackageHistoryFrom = new AppPackageHistoryForm(modal.getContentId(), loadedAppPackage, false);
                            appPackageHistoryFrom.setParentListner(AppPackageList.this);
                            modal.setContent(appPackageHistoryFrom);
                            modal.setTitle(new ResourceModel("label.history"));
                            modal.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                            modal.show(target);
                        }
                    }
                });
            }
        });

        return columnList;
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {

        if (eventThrownCmp != null && eventThrownCmp.getId().equals("add")) {
            target.add(testIssueForm);
        }
        if (eventThrownCmp != null && eventThrownCmp.getId().equals("savePublishDate")) {
            if (childModel != null) {
                target.add(table);
            }
        }
        modal.close(target);

    }

    public static class AppPackageDataProvider extends SortableDataProvider {
        AppPackageService.Criteria criteria;

        public void setCriteria(AppPackageService.Criteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            return AppPackageService.Instance.list(criteria, (int)first, (int)count, session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            return AppPackageService.Instance.count(criteria, session);
        }

        @Override
        public IModel model(Object object) {
            return new Model((Serializable) object);
        }
    }
}
