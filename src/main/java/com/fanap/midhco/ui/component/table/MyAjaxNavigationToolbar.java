package com.fanap.midhco.ui.component.table;

import com.fanap.midhco.ui.component.SelectionMode;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.string.StringValue;

import java.io.Serializable;

public class MyAjaxNavigationToolbar extends AbstractToolbar {
    private static final long serialVersionUID = 1L;
    private AjaxLink selectAll;
    private AjaxLink deselectAll;
    private MyDataTable table;

    public MyAjaxNavigationToolbar(final MyDataTable table) {
        this(table, false, 1000);
    }

    // Main Constructor
    public MyAjaxNavigationToolbar(final MyDataTable table, boolean countLess, final int maxRows) {
        super(table);

        this.table = table;

        WebMarkupContainer span = new WebMarkupContainer("span");
        add(span);
        span.add(new AttributeModifier("colspan", new Model(String.valueOf(table.getColumns().size()))));

        if (countLess)
            span.add(new MyCLPagingNavigator("navigator", table));
        else
            span.add(new MyPagingNavigator("navigator", table));

        if (countLess) {
            span.add(new AjaxLink("navigatorLabel") {
                @Override
                protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                    super.updateAjaxAttributes(attributes);

                    AjaxCallListener ajaxCallListener = new AjaxCallListener();
                    ajaxCallListener.onPrecondition("return confirm('" + getString("label.confirmCount") + "');");
                    attributes.getAjaxCallListeners().add(ajaxCallListener);
                }

                @Override
                public void onClick(AjaxRequestTarget target) {
                    CLSortableDataProvider provider = (CLSortableDataProvider) table.getDataProvider();
                    long l = provider.count();
                    target.appendJavaScript(String.format("alert('%s: %,d');", getLocalizer().getString("monitoring.trx.count", this), l));
                }

                @Override
                public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                    replaceComponentTagBody(markupStream, openTag, getLocalizer().getString("monitoring.trx.count", this));
                }
            }.add(new AttributeModifier("style", new Model("cursor:pointer"))));


        } else
            span.add(new Label("navigatorLabel", new StringResourceModel("NavigatorLabel", this, new Model(new NavigatorLabelModel(table)))));

        WebComponent changeRows = new ContextImage("changeRows", new Model("images/nav/table_refresh.png"));
        changeRows.setOutputMarkupId(true);

        AjaxLink exportToXLSX = new AjaxLink("exportToXLSX") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                table.exportToCSV(ajaxRequestTarget);
            }
        };
        exportToXLSX.setOutputMarkupId(true);
        span.add(exportToXLSX);

        AjaxLink exportAllToXLSX = new AjaxLink("exportAllToXLSX") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
//                table.exportAllToXLSX();
            }
        };
        exportAllToXLSX.setOutputMarkupId(true);
        span.add(exportAllToXLSX);
        exportAllToXLSX.setVisible(false);

        selectAll = new AjaxLink("selectAll") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                table.selectAllRows(ajaxRequestTarget);
            }
        };
        selectAll.setOutputMarkupId(true);
        span.add(selectAll);

        deselectAll = new AjaxLink("deselectAll") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                table.deselectAllRows(ajaxRequestTarget);
            }
        };
        deselectAll.setOutputMarkupId(true);
        span.add(deselectAll);


        final WebComponent noOfRows = new WebComponent("noOfRows");
        noOfRows.setOutputMarkupId(true);
        noOfRows.add(new AttributeAppender("value", new Model(table.getItemsPerPage()), " "));
        noOfRows.add(new AttributeAppender("onkeydown", new Model(String.format("return tableButtonClick(event, '%s')", changeRows.getMarkupId())), " "));
        noOfRows.add(new AttributeModifier("maxlength", new Model(String.valueOf(maxRows).length())));
        span.add(noOfRows);

        span.add(new Label("rowsPerPage", (new ResourceModel("label.rowsPerPage")).getObject()
                + "(" + new ResourceModel("label.max").getObject() + ":" + (maxRows - 1) + ")"));

        changeRows.add(new AbstractDefaultAjaxBehavior() {
            protected void respond(AjaxRequestTarget target) {
                IRequestParameters requestParameters = getRequest().getRequestParameters();
                StringValue _noOfRowsAsStringValue = requestParameters.getParameterValue("noOfRows");
                String _noOfRows = _noOfRowsAsStringValue.toString();
                try {
                    int nor = Integer.parseInt(_noOfRows);
                    if (nor > 0 && nor < maxRows) {
                        table.setItemsPerPage(nor);
                        noOfRows.add(new AttributeModifier("value", new Model(table.getItemsPerPage())));
                        target.add(table);
                    } else
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    String errorMsg = getLocalizer().getString("error.paging.noOfRowsRange", MyAjaxNavigationToolbar.this);
                    errorMsg = errorMsg.replace("${maxRows}", String.valueOf(maxRows));
                    target.appendJavaScript(String.format("alert('%s');", errorMsg));
                }
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                Component cmp = getComponent();
                String onclick = String.format("tableChangeRows('%s', '%s')", getCallbackUrl(), noOfRows.getMarkupId());
                if (cmp.isEnabled() && cmp.isEnableAllowed())
                    tag.put("onclick", onclick);
            }
        });
        span.add(changeRows);

        span.add(new Label("gotoPageLabel", new ResourceModel("label.goToPage")).setVisible(!countLess));

        WebComponent gotoPage = new ContextImage("gotoPage", new Model("images/nav/go.png"));
        gotoPage.setOutputMarkupId(true);
        gotoPage.setVisible(!countLess);

        final WebComponent pageNo = new WebComponent("pageNo");
        pageNo.setOutputMarkupId(true);
        pageNo.setVisible(!countLess);

        if (!countLess)
            pageNo.add(new AttributeAppender("onkeydown", new Model(String.format("return tableButtonClick(event, '%s')", gotoPage.getMarkupId())), " "));

        span.add(pageNo);

        if (!countLess)
            gotoPage.add(new AbstractDefaultAjaxBehavior() {
                protected void respond(AjaxRequestTarget target) {
                    IRequestParameters requestParameters = getRequest().getRequestParameters();
                    String _pageNo = requestParameters.getParameterValue("pageNo").toString();
                    try {
                        long no = Long.parseLong(_pageNo);
                        if (no > table.getPageCount())
                            no = table.getPageCount();
                        table.setCurrentPage(no - 1);
                        target.add(table);
                    } catch (NumberFormatException e) {
                        target.appendJavaScript("alert('Number');");
                    }
                }

                @Override
                protected void onComponentTag(ComponentTag tag) {
                    super.onComponentTag(tag);
                    Component cmp = getComponent();
                    String onclick = String.format("tableGotoPage('%s', '%s')", getCallbackUrl(), pageNo.getMarkupId());
                    if (cmp.isEnabled() && cmp.isEnableAllowed())
                        tag.put("onclick", onclick);
                }
            });
        span.add(gotoPage);
    }

    public boolean isVisible() {
        return getTable().getPageCount() > 0;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        selectAll.setVisible(table.getSelectionMode() == SelectionMode.Multiple || table.getSelectionMode() == SelectionMode.MultipleOrQuery);
        deselectAll.setVisible(table.getSelectionMode() == SelectionMode.Multiple || table.getSelectionMode() == SelectionMode.MultipleOrQuery);
    }

    private class NavigatorLabelModel implements Serializable {
        private final DataTable table;

        private NavigatorLabelModel(DataTable table) {
            this.table = table;
        }

        public long getOf() {
            return table.getRowCount();
        }

        public long getFrom() {
            return (table.getCurrentPage() * table.getItemsPerPage()) + 1;
        }

        public long getTo() {
            return getFrom() + table.getItemsPerPage() - 1;
        }
    }
}
