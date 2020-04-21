package com.fanap.midhco.ui.pages.appcategory;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.entities.AppCategory;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.appcategory.AppCategoryService;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.ISelectable;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.ajaxLinkPanel.AjaxLinkPanel;
import com.fanap.midhco.ui.component.limitedTextField.LimitedTextField;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by admin123 on 2/12/2017.
 */
@Authorize(view = Access.APPCATEGORY_LIST)
public class AppCategoryList  extends BasePanel implements IParentListner, ISelectable {
    Form searchForm;
    AppCategorySorableDataProvider dp = new AppCategorySorableDataProvider();
    MyAjaxDataTable table;
    BootStrapModal modalWindow = new BootStrapModal("modal");
    LimitedTextField idField;
    LimitedTextField categoryNameTextField;

    public AppCategoryList() {
        this(MAIN_PANEL_ID, new AppCategoryService.AppCategoryCriteria(), SelectionMode.None);
    }

    protected AppCategoryList(String id, AppCategoryService.AppCategoryCriteria criteria, SelectionMode selectionMode) {
        super(id);

        setPageTitle(getString("AppCategory"));

        add(modalWindow);

        boolean allFieldsNull = checkAllFieldsNull(criteria);

        searchForm = new Form("searchForm", new CompoundPropertyModel(criteria));
        searchForm.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(searchForm);

        idField = new LimitedTextField("id", true, true, false,false,false, 12, getString("AppCategory.id"));
        searchForm.add(idField);

        categoryNameTextField = new LimitedTextField("categoryName", null, false, true,true,false, 30, getString("AppCategory.categoryName"));
        searchForm.add(categoryNameTextField);

        int rowsPerPage =
                Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_LIST_ROWS_PER_PAGE));
        table = new MyAjaxDataTable("table", getColumns(), dp, rowsPerPage);
        table.setSelectionMode(selectionMode);
        table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(table);

        if(allFieldsNull)
            table.setVisible(false);


        add(new AjaxLink("select") {
            public void onClick(AjaxRequestTarget target) {
                childFinished(target, null, null);
            }
        }.setOutputMarkupPlaceholderTag(true).setVisible(false));
        if (selectionMode.isSelectable() && table.getRowCount() > 0)
            get("select").setVisible(true);


        AjaxLink createCategory =new AjaxLink("createCategory") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                AppCategory appCategory = new AppCategory();
                AppCategoryForm appCategoryForm = new AppCategoryForm(modalWindow.getContentId(), appCategory);
                appCategoryForm.setParentListner(AppCategoryList.this);
                modalWindow.setContent(appCategoryForm);
                modalWindow.setTitle(getString("AppCategory.createForm"));
                modalWindow.show(target);
            }
        };
        createCategory.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        Boolean showButton = PrincipalUtil.hasPermission(Access.APPCATEGORY_ADD);
        createCategory.setVisible(showButton);
        add(createCategory);

        searchForm.add(new AjaxFormButton("search", searchForm) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {

                String validationString = "";
                if (idField != null && idField.getValidatorString() != null && !idField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : idField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (categoryNameTextField != null && categoryNameTextField.getValidatorString() != null && !categoryNameTextField.getValidatorString().isEmpty()) {
                    for (String validationStringInList : categoryNameTextField.getValidatorString()) {
                        validationString += " - " +
                                validationStringInList + "<br/>";
                    }
                }

                if (!validationString.isEmpty()) {
                    target.appendJavaScript("showMessage('" + validationString + "');");
                    return;
                }


                AppCategoryService.AppCategoryCriteria criteria = (AppCategoryService.AppCategoryCriteria) form.getModelObject();
                if(criteria != null) {
                    dp.setCriteria(criteria);
                    table.setVisible(true);
                    target.add(table);
                    if (selectionMode.isSelectable())
                        target.add(AppCategoryList.this.get("select").setVisible(true));
                }
            }
        });

        searchForm.add(new AjaxLink("reset") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                AppCategoryService.AppCategoryCriteria criteria = new AppCategoryService.AppCategoryCriteria();
                searchForm.setModelObject(criteria);
                target.add(searchForm);
                table.setVisible(false);
                target.add(table);

                if (selectionMode.isSelectable())
                    target.add(AppCategoryList.this.get("select").setVisible(true));
                else
                    AppCategoryList.this.get("select").setVisible(false);

                target.add(AppCategoryList.this.get("select"));
            }
        });
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

        columnList.add(new PropertyColumn(new ResourceModel("AppCategory.id"), "ent.id", "id"));
        columnList.add(new PropertyColumn(new ResourceModel("AppCategory.categoryName"), "ent.categoryName", "categoryName"));

        columnList.add(new AbstractColumn(new ResourceModel("AppCategory.isAssignable", "Assignable")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                AppCategory appCategory = (AppCategory) rowModel.getObject();
                if (appCategory.getAssignable() != null && appCategory.getAssignable()) {
                    cellItem.add(new Label(componentId, new ResourceModel("label.yes", "YES")));
                } else {
                    cellItem.add(new Label(componentId, new ResourceModel("label.no", "NO")));
                }
            }
        });

        columnList.add(new AbstractColumn(new ResourceModel("AppCategory.isEnabled", "isEnabled")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                AppCategory appCategory = (AppCategory) rowModel.getObject();
                if (appCategory.getEnabled() != null && appCategory.getEnabled()) {
                    cellItem.add(new Label(componentId, new ResourceModel("label.yes", "YES")));
                } else {
                    cellItem.add(new Label(componentId, new ResourceModel("label.no", "NO")));
                }
            }
        });

        if(PrincipalUtil.hasPermission(Access.APPCATEGORY_EDIT))
            columnList.add(new AbstractColumn(new ResourceModel("label.edit", "EDIT")) {
                @Override
                public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
                    Session session = HibernateUtil.getNewSession();
                    AppCategory appCategory = (AppCategory) rowModel.getObject();
                    AppCategory loadedAppCategory = (AppCategory) session.get(AppCategory.class, appCategory.getId());
                    session.close();
                    if (!loadedAppCategory.getCategoryName().equals("root")) {
                    cellItem.add(new AjaxLinkPanel(componentId, AjaxLinkPanel.Image.Edit) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                                AppCategoryForm appCategoryForm = new AppCategoryForm(modalWindow.getContentId(), loadedAppCategory);
                            appCategoryForm.setParentListner(AppCategoryList.this);
                            modalWindow.setContent(appCategoryForm);
                            modalWindow.setTitle(getString("AppCategory.editForm"));
                            modalWindow.show(target);
                        }
                    });
                    } else {
                        cellItem.add(new Label(componentId, new Model<>()));
                    }
                }
            });

        return columnList;
    }


    @Override
    public void onChildFinished(AjaxRequestTarget target, IModel childModel, Component eventThrownCmp) {
        if(eventThrownCmp != null && eventThrownCmp.getId().equals("save")) {
            target.add(table);
        }
        modalWindow.close(target);
    }

    @Override
    public Collection<Object> getSelection() {
        return table.getSelectedObjetcs();
    }

    public static class AppCategorySorableDataProvider extends SortableDataProvider {
        AppCategoryService.AppCategoryCriteria criteria;

        public AppCategorySorableDataProvider() {
            setSort("ent.id", SortOrder.ASCENDING);
        }

        public void setCriteria(AppCategoryService.AppCategoryCriteria criteria) {
            this.criteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            Session session = HibernateUtil.getCurrentSession();
            SortParam sortParam = getSort();
            return AppCategoryService.Instance.list(criteria, (int)first, (int)count, (String)sortParam.getProperty(), sortParam.isAscending(), session).iterator();
        }

        @Override
        public long size() {
            Session session = HibernateUtil.getCurrentSession();
            return AppCategoryService.Instance.count(criteria, session);
        }

        @Override
        public IModel model(Object o) {
            return new Model((Serializable) o);
        }
    }
}
