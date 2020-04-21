package com.fanap.midhco.ui.pages.app;

import com.fanap.midhco.appstore.applicationUtils.ComponentKey;
import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateType;
import com.fanap.midhco.appstore.restControllers.vos.AppSearchAnouncementVO;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.app.AppService;
import com.fanap.midhco.appstore.service.appcategory.AppCategoryService;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.service.user.RoleService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.access.WebAction;
import com.fanap.midhco.ui.component.ISelectable;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.dateTimePanel.DateTimeRangePanel;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.mydropdown.MyDropDownChoicePanel;
import com.fanap.midhco.ui.component.selectionpanel.SelectionPanel;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import com.fanap.midhco.ui.component.table.column.PersianDateColumn;
import com.fanap.midhco.ui.component.tagsinput.TagsInput;
import com.fanap.midhco.ui.pages.user.UserList;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by admin123 on 6/29/2016.
 */
@Authorize(view = Access.APP_LIST)
public class AppList extends BasePanel implements IParentListner, IReportableCriteria {
    Form form;
    MyAjaxDataTable table;
    AppListSortableDataProvider dp = new AppListSortableDataProvider();
    BootStrapModal modal = new BootStrapModal("modal");
    MyDropDownChoicePanel osDropDown;
    MyDropDownChoicePanel appCategoryDownChoicePanel;
    MyDropDownChoicePanel osTypeDropDown;
    TagsInput tagsInput;
    AppService.AppSearchCriteria appSearchCriteria = null;
    SwitchBox deleteSwitchBox;

    LimitedTextField packageNameTextField;
    LimitedTextField titleTextField;
    LimitedTextField versionCodeTextField;
    LimitedTextField versionNameTextField;
    AjaxLink addAjaxLink;


    public AppList() {
        this(MAIN_PANEL_ID, new AppService.AppSearchCriteria(), null, SelectionMode.None);
    }

    public AppList(String id, AppService.AppSearchCriteria criteria, List<ComponentKey> disabledComponents, final SelectionMode selectionMode) {
        super(id);

        setPageTitle(getString("label.list") + " " + getString("App"));

        add(modal);

        form = new Form("searchForm", new CompoundPropertyModel(criteria));
        form.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);

        packageNameTextField = new LimitedTextField("packageName", true, false, false, true, false, 40, getString("App.appPackageName"));
        packageNameTextField.setLabel(new ResourceModel(""));
        form.add(packageNameTextField);

        Model<String> keywordModel = Model.of("");
        if (criteria != null && criteria.keyword != null) {
            int count = 0;
            StringBuilder keywordBuilder = new StringBuilder("");
            for (String keyword : criteria.keyword) {
                keywordBuilder.append(keyword);
                if (count != criteria.keyword.size()) {
                    keywordBuilder.append(",");
                }
                count++;
            }
            keywordModel = Model.of(keywordBuilder.toString());
        }
        tagsInput = new TagsInput("tagsinput", keywordModel, null);
        tagsInput.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        tagsInput.setModel(keywordModel);
        form.add(tagsInput);


        titleTextField = new LimitedTextField("title", null, false, true, true, false, 40, getString("App.title"));
        titleTextField.setLabel(new ResourceModel("App.title"));
//        titleTextField.setRequired(true);
        form.add(titleTextField);

        versionCodeTextField = new LimitedTextField("versionCode", null, true, false, false, false, 10, getString("APPPackage.versionCode"));

        versionCodeTextField.setLabel(new ResourceModel("APPPackage.versionCode"));
        form.add(versionCodeTextField);

        versionNameTextField = new LimitedTextField("versionName", null, true, false, false, false, 11, getString("APPPackage.versionName"));

        versionNameTextField.setLabel(new ResourceModel("APPPackage.versionName"));
        form.add(versionNameTextField);

        List<OSType> allOSTypes = OSTypeService.Instance.getEnabledOSTypes();
        osTypeDropDown =
                new MyDropDownChoicePanel("osType", allOSTypes, true, false, getString("OS.osType"), 3, true, new ChoiceRenderer()) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        List<OSType> selectedItems = (List<OSType>) getSelectedItem();
                        if (selectedItems == null || selectedItems.isEmpty())
                            osDropDown.setChoices(new ArrayList(), target);
                        else {
                            OSService.OSCriteria osCriteria = new OSService.OSCriteria();
                            osCriteria.osType = new ArrayList<OSType>();
                            osCriteria.osType.addAll(selectedItems);
                            Session session = HibernateUtil.getCurrentSession();
                            try {
                                List<OS> osList = OSService.Instance.list(osCriteria, 0, -1, null, true, session);
                                osDropDown.setChoices(osList, target);
                            } finally {
                                session.close();
                            }
                        }
                    }
                };
        osTypeDropDown.setLabel(new ResourceModel("OS.osType"));
        osTypeDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(osTypeDropDown);

        List<OS> osList = new ArrayList<OS>();
        if (criteria != null && criteria.getOsType() != null) {

            Session session = HibernateUtil.getCurrentSession();
            try {
                OSService.OSCriteria osCriteria = new OSService.OSCriteria();
                osCriteria.osType = new ArrayList<OSType>();
                osCriteria.osType.addAll(criteria.getOsType());
                osList = OSService.Instance.list(osCriteria, 0, -1, null, true, session);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                session.close();
            }

        }
        osDropDown =
                new MyDropDownChoicePanel("os", osList, true, false, getString("OS"), 3, false, new ChoiceRenderer());
        osDropDown.setLabel(new ResourceModel("OS"));
        osDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(osDropDown);


        List<AppCategory> allAssignableAppCategory = AppCategoryService.Instance.listAllAssignable();
        appCategoryDownChoicePanel =
                new MyDropDownChoicePanel("appCategory", allAssignableAppCategory, true, true, getString("AppCategory"), 4, false, new ChoiceRenderer<>());
        appCategoryDownChoicePanel.setLabel(new ResourceModel("AppCategory"));
        appCategoryDownChoicePanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(appCategoryDownChoicePanel);

        form.add(new SelectionPanel("developers", SelectionMode.Multiple) {
            @Override
            public ISelectable getSelectable(String panelId) {
                Role developerRole = RoleService.Instance.getDeveloperRole();
                List<ComponentKey> disabledList = new ArrayList<ComponentKey>();
                disabledList.add(new ComponentKey("roles"));
                UserService.UserCriteria userCriteria = new UserService.UserCriteria();
                userCriteria.roles = new ArrayList<Role>();
                userCriteria.roles.add(developerRole);
                return new UserList(panelId, userCriteria, disabledList, SelectionMode.Multiple);
            }
        }.setLabel(new ResourceModel("App.developer")));

        DateTimeRangePanel creationDateTimeDateTimePanel = new DateTimeRangePanel("creationDateTime", DateType.DateTime);
        creationDateTimeDateTimePanel.setLabel(new ResourceModel("label.createdDateTime"));
        form.add(creationDateTimeDateTimePanel);

        List<PublishState> publishStates = PublishState.listAll();
        MyDropDownChoicePanel pubLishStateDropDown =
                new MyDropDownChoicePanel("publishStates", publishStates, true, false, getString("APPPackage.publishState"), 2, false, new ChoiceRenderer());
        pubLishStateDropDown.setLabel(new ResourceModel("APPPackage.publishState"));
        pubLishStateDropDown.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(pubLishStateDropDown);

        form.add(new SelectionPanel("creatorUsers", SelectionMode.Multiple) {
            @Override
            public ISelectable getSelectable(String panelId) {
                return new UserList(panelId, new UserService.UserCriteria(), SelectionMode.Multiple);
            }
        }.setLabel(new ResourceModel("label.creatorUser")));


        AjaxFormButton searchAjaxFormButton = new AjaxFormButton("search", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                AppService.AppSearchCriteria criteria = (AppService.AppSearchCriteria) form.getModelObject();


                String validationString = "";
                if (titleTextField != null && titleTextField.getValidatorString() != null && !titleTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : titleTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (packageNameTextField != null && packageNameTextField.getValidatorString() != null && !packageNameTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : packageNameTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (versionCodeTextField != null && versionCodeTextField.getValidatorString() != null && !versionCodeTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : versionCodeTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (versionNameTextField != null && versionNameTextField.getValidatorString() != null && !versionNameTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : versionNameTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }


                User requesterUser = PrincipalUtil.getCurrentUser();
                if (requesterUser != null) {
                    if (!UserService.Instance.isUserRoot(requesterUser)) {
                        List<User> developerUsers = new ArrayList<>();
                        developerUsers.add(requesterUser);
                        criteria.setDevelopers(developerUsers);
                    }
                }
                if (tagsInput != null && tagsInput.getConvertedInput() != null) {

                    String[] keywordsString = tagsInput.getConvertedInput().toString().split(",");
                    List<String> keywords = new ArrayList<String>();
                    for (String string : keywordsString) {
                        keywords.add(string.trim());
                    }
                    criteria.keyword = keywords;
                } else {
                    criteria.keyword = null;
                }
                criteria.setDeleted(false);

                if (criteria != null) {
                    appSearchCriteria = criteria;
                    dp.setCriteria(criteria);
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable())
                        target.add(AppList.this.get("select").setVisible(true));
                }
            }
        };
        searchAjaxFormButton.setVisible(!selectionMode.equals(SelectionMode.WithoutAdd));
        form.add(searchAjaxFormButton);


        AjaxFormButton preShowAjaxFormButton = new AjaxFormButton("preShow", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                AppService.AppSearchCriteria criteria = (AppService.AppSearchCriteria) form.getModelObject();
                if (tagsInput != null && tagsInput.getConvertedInput() != null) {
                    String[] keywordsString = tagsInput.getConvertedInput().toString().split(",");
                    List<String> keywords = new ArrayList<String>();
                    for (String string : keywordsString) {
                        keywords.add(string.trim());
                    }
                    criteria.keyword = keywords;
                } else {
                    criteria.keyword = null;
                }

                if (criteria.getAppCategory() == null || criteria.getAppCategory().isEmpty()) {
                    criteria.setAppCategoryId(null);
                } else {
                    List<Long> appCategoryIdList = new ArrayList<>();
                    criteria.getAppCategory().forEach(appCategory -> appCategoryIdList.add(appCategory.getId()));
                    criteria.setAppCategoryId(appCategoryIdList);
                }
                criteria.setDeleted(false);

                if (criteria != null) {
                    appSearchCriteria = criteria;
                    dp.setCriteria(criteria);
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable())
                        target.add(AppList.this.get("select").setVisible(true));
                }
            }
        };
        preShowAjaxFormButton.setVisible(selectionMode.equals(SelectionMode.WithoutAdd));
        form.add(preShowAjaxFormButton);

        AjaxFormButton createAnouncement = new AjaxFormButton("createAnouncement", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                try {
                    AppService.AppSearchCriteria criteria = (AppService.AppSearchCriteria) form.getModelObject();
                    if (tagsInput != null && tagsInput.getConvertedInput() != null) {
                        String[] keywordsString = tagsInput.getConvertedInput().toString().split(",");
                        List<String> keywords = new ArrayList<String>();
                        for (String string : keywordsString) {
                            keywords.add(string.trim());
                        }
                        criteria.keyword = keywords;
                    } else {
                        criteria.keyword = null;
                    }

                    criteria.setDeleted(false);

                    if (criteria != null) {
                        appSearchCriteria = criteria;
                    }
                    childFinished(target, new Model<>(), this);
                    modal.close(target);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        createAnouncement.setVisible(selectionMode.equals(SelectionMode.WithoutAdd));
        createAnouncement.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(createAnouncement);

        AjaxFormButton resetAjaxFormButton = new AjaxFormButton("reset", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
                criteria.setDeleted(false);
                form.setModelObject(criteria);
                Model<String> keyWordModel = Model.of("");
                if (tagsInput != null) {
                    tagsInput.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                    tagsInput.setModel(keyWordModel);
                    form.add(tagsInput);
                }
                target.add(form);

                if (selectionMode.isSelectable())
                    target.add(AppList.this.get("select").setVisible(true));
                else
                    AppList.this.get("select").setVisible(false);

                target.add(AppList.this.get("select"));
                table.setVisible(false);
                target.add(table);
            }
        };
//        resetAjaxFormButton.setVisible(!selectionMode.equals(SelectionMode.WithoutAdd));
//        resetAjaxFormButton.setVisible(true);
//        resetAjaxFormButton.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(resetAjaxFormButton);


        addAjaxLink = new AjaxLink("add") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (!selectionMode.equals(SelectionMode.WithoutAdd)) {

                    AppForm appForm = new AppForm(modal.getContentId(), new App());
                    appForm.setParentListner(AppList.this);
                    modal.setContent(appForm);
                    target.appendJavaScript("$('.modal-body').css('overflow-x', 'hidden')");
                    modal.setTitle(getString("App.createForm"));
                    modal.show(target);
                }
            }
        };

        if (PrincipalUtil.hasPermission(Access.APP_ADD) && !selectionMode.equals(SelectionMode.WithoutAdd)) {
            addAjaxLink.setEnabled(true);
            addAjaxLink.setVisible(true);
        } else {
            addAjaxLink.setEnabled(false);
            addAjaxLink.setVisible(false);
        }
        add(addAjaxLink);

        table = new MyAjaxDataTable("table", getColumns(), dp, 10);
        table.setSelectionMode(selectionMode);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(table);

        boolean allFieldsNull = checkAllFieldsNull(criteria);

        if (allFieldsNull || selectionMode.equals(SelectionMode.WithoutAdd))
            table.setVisible(false);

        add(new AjaxLink("select") {
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, null);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.isSelectable() && table.getRowCount() > 0)
            get("select").setVisible(true);

        add(form);

        if (disabledComponents != null && !disabledComponents.isEmpty()) {
            for (ComponentKey componentKey : disabledComponents) {
                FormComponent formComponent = (FormComponent) form.get(componentKey.getId());
//                System.out.println("-------------->" + formComponent);
                if (formComponent != null) {
                    if (formComponent instanceof SelectionPanel) {
                        ((SelectionPanel) formComponent).disable();
                    } else {
                        formComponent.setEnabled(false);
                    }
                }
            }
            form.get("reset").setVisible(false);
        }
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("App.id"), "app.id", "appId"));
        columnList.add(new PropertyColumn(new ResourceModel("App.appPackageName"), "App.appPackageNamee", "packageName"));
        columnList.add(new PropertyColumn(new ResourceModel("App.title"), "app.title", "title"));
        columnList.add(new PropertyColumn(new ResourceModel("OSType"), "app.osType", "osType"));
        columnList.add(new PropertyColumn(new ResourceModel("App.os"), "os.osName", "osName"));
        columnList.add(new PropertyColumn(new ResourceModel("APPPackage.versionCode"), "mainPackage.versionCode", "app_mainPack_versionCode"));
        columnList.add(new PropertyColumn(new ResourceModel("APPPackage.versionName"), "mainPackage.versionName", "app_mainPack_versionName"));
        columnList.add(new PropertyColumn(new ResourceModel("AppCategory"), "app.appCategory", "appCategory"));
        columnList.add(new PropertyColumn(new ResourceModel("APPPackage.publishState"), "mainPackage.publishState", "publishState"));
        columnList.add(new PersianDateColumn(new ResourceModel("label.createdDateTime"), "app.creationDate", "creationDateTime"));
        columnList.add(new PropertyColumn(new ResourceModel("label.creatorUser"), "app.creatorUser", "creatorUser"));


        //todo create yesNoPanel Form to get confirm for delete
        //http://tomaszdziurko.com/2010/02/wicket-ajax-modal-are-you-sure-window/

        columnList.add(new AbstractColumn(new Model("")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                if (PrincipalUtil.hasPermission(Access.APP_REMOVE)) {
                    final AppService.AppSearchResultModel searchResultModel = (AppService.AppSearchResultModel) rowModel.getObject();
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Delete) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {


                            Long appId = Long.valueOf(searchResultModel.getAppId());
                            Session session = HibernateUtil.getCurrentSession();

                            App app = (App) session.load(App.class, appId);

                            Transaction tx = null;
                            try {
                                tx = session.beginTransaction();
                                AppService.Instance.deleteApp(app, session);
                                tx.commit();
                            } catch (Exception ex) {
                                if(tx != null)
                                    tx.rollback();
                            }

                            target.add(table);
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
                if (PrincipalUtil.hasPermission(Access.APP_EDIT)) {
                    final AppService.AppSearchResultModel searchResultModel = (AppService.AppSearchResultModel) rowModel.getObject();
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            Long appId = Long.valueOf(searchResultModel.getAppId());
                            Session session = HibernateUtil.getCurrentSession();

                            App app = (App) session.load(App.class, appId);
                            AppForm appForm = new AppForm(modal.getContentId(), app);
                            appForm.setParentListner(AppList.this);
                            modal.setTitle(getString("App.editForm"));
                            modal.setContent(appForm);
                            modal.show(target);

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
                final AppService.AppSearchResultModel searchResultModel = (AppService.AppSearchResultModel) rowModel.getObject();
                cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.History) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        Long appId = Long.valueOf(searchResultModel.getAppId());
                        Session session = HibernateUtil.getCurrentSession();

                        App app = (App) session.load(App.class, appId);
                        AppHistoryForm appHistoryForm = new AppHistoryForm(modal.getContentId(), app);
                        appHistoryForm.setParentListner(AppList.this);
                        modal.setTitle(new ResourceModel("label.history").getObject());
                        modal.setContent(appHistoryForm);
                        modal.show(target);

                    }
                });
            }
        });

        columnList.add(new AbstractColumn(new Model<String>("")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                AppService.AppSearchResultModel appSearchResultModel = (AppService.AppSearchResultModel) rowModel.getObject();

                cellItem.add(new AjaxLinkPanel(componentId, "images/table/application-view-detail-icon.png", new ResourceModel("app.dashboard")) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        Session session = HibernateUtil.getCurrentSession();
                        App app = (App) session.load(App.class, appSearchResultModel.getAppId());
                        AppCommentDashboardForm appCommentDashboardForm = new AppCommentDashboardForm(modal.getContentId(), app);
                        modal.setContent(appCommentDashboardForm);
                        appCommentDashboardForm.setParentListner(AppList.this);
                        modal.setTitle(getString("app.dashboard"));
                        modal.show(target);
                    }
                });
            }
        });

        return columnList;
    }

    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if (eventThrownCmp != null && (eventThrownCmp.getId().equals("save")) || (eventThrownCmp.getId().equals("cancel")))
            target.add(table);
        modal.close(target);
    }

    @Override
    public String reportCriteria() {
        if (appSearchCriteria == null)
            return null;
        else {
            AppSearchAnouncementVO appSearchAnouncementVO = AppSearchAnouncementVO.createAppSearchAnouncementVoFromCriteria(appSearchCriteria);
            String jsonStr = JsonUtil.getJson(appSearchAnouncementVO);

            return jsonStr;
        }
    }

    public static class AppListSortableDataProvider extends SortableDataProvider {
        public AppService.AppSearchCriteria criteria;

        public AppListSortableDataProvider() {
            setSort("app.id", SortOrder.ASCENDING);
        }

        public void setCriteria(AppService.AppSearchCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            try {
                return AppService.Instance.list(criteria, (int) first, (int) count, (String) sortParam.getProperty(), sortParam.isAscending(), session).iterator();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            try {
                return AppService.Instance.count(criteria, session);
            } catch (Exception e) {
                return 0;
            }
        }

        @Override
        public IModel model(Object object) {
            return new Model((Serializable) object);
        }
    }
}
