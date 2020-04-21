package com.fanap.midhco.ui.component.table;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.MyCalendarUtil;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DayDate;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.ajaxDownload.AjaxDownload;
import com.fanap.midhco.ui.component.table.column.EnumColumn;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import com.fanap.midhco.ui.component.table.column.MyAbstractColumnWithDataTag;
import com.fanap.midhco.ui.component.table.column.PersianDateColumn;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.*;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import java.io.*;
import java.util.*;

public class MyDataTable extends DataTable {
    private static final Logger logger = Logger.getLogger(MyDataTable.class);

    protected MyItem selectedRow;
    protected IRowSelectEvent rowSelectEvent;
    protected SelectionMode selectionMode = SelectionMode.None;
    protected Collection<Object> selectedObjetcs = new ArrayList<Object>();

    protected List<IColumn> columns;
    protected IDataProvider dataProvider;

    public MyDataTable(String id, ColumnsList columnsList, List data) {
        this(id, columnsList.getColumns(), new ListDataProvider(data), 1, false);
        addTopToolbar(new SimpleHeader(this, columnsList));
    }

    // Main Constructor
    public MyDataTable(String id, List<IColumn> columns, IDataProvider dataProvider, int rowsPerPage, boolean showHeader) {
        super(id, columns, dataProvider, rowsPerPage);
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        setVersioned(false);
        if (showHeader)
            addTopToolbar(new SimpleHeader(this, columns));

        this.columns = columns;
        this.dataProvider = dataProvider;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }

    public void setRowSelectEvent(IRowSelectEvent rowSelectEvent) {
        this.rowSelectEvent = rowSelectEvent;
    }

    public MyItem getSelectedItem() {
        return selectedRow;
    }

    public void setSelectedItem(MyItem item) {
        this.selectedRow = item;
    }

    public boolean isSelectable() {
        return selectionMode == SelectionMode.Multiple || selectionMode == SelectionMode.Single || selectionMode == SelectionMode.MultipleOrQuery;
    }

    public Collection<Object> getSelectedObjetcs() {
        return selectedObjetcs;
    }

    protected Item newRowItem(String id, int index, final IModel model) {
        final MyItem myItem = new MyItem(id, index, model, this, rowSelectEvent != null || isSelectable());
        myItem.setSelected(isSelectable() && selectedObjetcs.contains(model.getObject()));

        if (rowSelectEvent != null || isSelectable())
            myItem.add(new AjaxEventBehavior("onclick") {
                protected void onEvent(AjaxRequestTarget target) {
                    if (selectionMode == SelectionMode.Single) {
                        if (!myItem.isSelected()) {
                            selectedObjetcs.clear();
                            if (selectedRow != null) {
                                selectedRow.setSelected(false);
                                target.add(selectedRow);
                            }
                            myItem.setSelected(true);
                            target.add(myItem);
                            selectedRow = myItem;
                            selectedObjetcs.add(model.getObject());
                        }
                    } else if (selectionMode != SelectionMode.None) {
                        if (myItem.isSelected())
                            selectedObjetcs.remove(model.getObject());
                        else
                            selectedObjetcs.add(model.getObject());
                        myItem.setSelected(!myItem.isSelected());
                        target.add(myItem);
                    }

                    if (rowSelectEvent != null) {
                        rowSelectEvent.onClick(target, model, myItem);
                    }
                }
            });
        return myItem;
    }

    public void selectAllRows(AjaxRequestTarget target) {
        if (selectionMode != SelectionMode.None && selectionMode != SelectionMode.Single) {
            selectedObjetcs.clear();
            Iterator iterator = dataProvider.iterator(0, dataProvider.size());

            while (iterator.hasNext())
                selectedObjetcs.add(iterator.next());
            target.add(this);
        }
    }

    public void selectRangedRows(AjaxRequestTarget target, int first, int count) {
        if (selectionMode != SelectionMode.None && selectionMode != SelectionMode.Single) {
            selectedObjetcs.clear();
            Iterator iterator = dataProvider.iterator(first, count);

            while (iterator.hasNext())
                selectedObjetcs.add(iterator.next());
            target.add(this);
        }
    }

    public void deselectAllRows(AjaxRequestTarget target) {
        if (selectionMode != SelectionMode.None && selectionMode != SelectionMode.Single) {
            selectedObjetcs.clear();
            target.add(this);
        }
    }

    @Override
    protected void onPageChanged() {
        selectedRow = null;
    }

    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    //TODO : add AsyncJobRunner
//    public void exportAllToXLSX() {
//        try {
//            final User currentUser = PrincipalUtil.getCurrentUser();
//
//            final IDataProvider provider = this.dataProvider;
//            final long providerSize = provider.size();
//
//            final XSSFWorkbook wb = new XSSFWorkbook();
//            XSSFSheet temp_createdSheet = wb.getSheet("Sheet 1");
//            if (temp_createdSheet == null) {
//                temp_createdSheet = wb.createSheet("Sheet 1");
//            }
//            final XSSFSheet createdSheet = temp_createdSheet;
//
//            XSSFRow header = createdSheet.createRow(0);
//            header.setHeightInPoints(20);
//
//            XSSFCellStyle headerStyle = wb.createCellStyle();
//            headerStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
//            headerStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
//
//            XSSFFont headerFont = wb.createFont();
//            headerFont.setBold(true);
//            headerFont.setFontHeightInPoints((short) 9);
//            headerStyle.setFont(headerFont);
//
//            final XSSFCellStyle cellStyle = wb.createCellStyle();
//            cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
//            cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
//
//            XSSFFont cellFont = wb.createFont();
//            cellFont.setFontHeightInPoints((short) 9);
//            cellStyle.setFont(cellFont);
//
//            XSSFCell xssfCell;
//            int headerNum = 0;
//            for (IColumn col : columns) {
//                xssfCell = header.createCell(headerNum++);
//                AbstractColumn acolumn = (AbstractColumn) col;
//                if (acolumn.getDisplayModel() != null && acolumn.getDisplayModel().getObject() != null)
//                    xssfCell.setCellValue(acolumn.getDisplayModel().getObject().toString());
//                xssfCell.setCellStyle(headerStyle);
//            }
//
//            FutureTask futureTask = new FutureTask(new Callable() {
//                @Override
//                public Object call() throws Exception {
//                    OutputStream outputStream = null;
//                    try {
//                        PrincipalUtil.setCurrentUser(currentUser);
//
//                        int chunkSize = 500;
//                        int first = 0;
//
//                        while (true) {
//                            if(first >= providerSize)
//                                break;
//
//                            Iterator iterator = provider.iterator(first, chunkSize);
//
//                            int rowNum = first + 1;
//                            int colNum;
//                            XSSFCell xssfCell;
//
//                            first = first + chunkSize;
//
//                            while (iterator.hasNext()) {
//                                colNum = 0;
//                                XSSFRow row = createdSheet.createRow(rowNum);
//                                row.setHeightInPoints(20);
//                                xssfCell = row.createCell(colNum++);
//                                xssfCell.setCellValue(rowNum++);
//                                xssfCell.setCellStyle(cellStyle);
//
//                                Object o = iterator.next();
//                                for (IColumn column : columns) {
//                                    if (column instanceof EnumColumn) {
//                                        String prop = ((EnumColumn) column).getPropertyExpression();
//                                        Object value = PropertyResolver.getValue(prop, o);
//
//                                        if (value != null) {
//                                            xssfCell = row.createCell(colNum);
//                                            xssfCell.setCellValue(((EnumColumn) column).getEnumCaption(value));
//                                            xssfCell.setCellStyle(cellStyle);
//                                        }
//                                        colNum++;
//                                    } else if (column instanceof PersianDateColumn) {
//                                        String prop = ((PropertyColumn) column).getPropertyExpression();
//                                        Object value = PropertyResolver.getValue(prop, o);
//
//                                        if (value != null) {
//                                            xssfCell = row.createCell(colNum);
//                                            if (value instanceof DateTime) {
//                                                xssfCell.setCellValue((MyCalendarUtil.toPersian((DateTime) value)).toString());
//                                            } else if (value instanceof DayDate) {
//                                                xssfCell.setCellValue((MyCalendarUtil.toPersian((DayDate) value)).toString());
//                                            } else {
//                                                xssfCell.setCellValue(value.toString());
//                                            }
//                                            xssfCell.setCellStyle(cellStyle);
//                                        }
//                                        colNum++;
//                                    } else if (column instanceof PropertyColumn) {
//                                        String prop = ((PropertyColumn) column).getPropertyExpression();
//                                        Object value = PropertyResolver.getValue(prop, o);
//
//                                        if (value != null) {
//                                            xssfCell = row.createCell(colNum);
//                                            xssfCell.setCellValue(value.toString());
//                                            xssfCell.setCellStyle(cellStyle);
//                                        }
//                                        colNum++;
//                                    } else if (column instanceof MyAbstractColumnWithDataTag) {
//                                        Object value = ((MyAbstractColumnWithDataTag) column).getDataTag(o);
////                        System.out.println(value);
//                                        if (value != null) {
//                                            xssfCell = row.createCell(colNum);
//                                            xssfCell.setCellValue(value.toString());
//                                            xssfCell.setCellStyle(cellStyle);
//                                        }
//                                        colNum++;
//                                    } else if (column instanceof AbstractColumn && !(column instanceof IndexColumn)) {
//                                        xssfCell = row.createCell(colNum);
//                                        xssfCell.setCellValue("");
//                                        xssfCell.setCellStyle(cellStyle);
//                                        colNum++;
//                                    }
//                                }
//                            }
//                        }
//
//
//                        String randomFileName = GlobalObjects.generateRandomFileName("Records.xlsx");
//                        String physicalFileName =
//                                ir.refah.application.util.ConfigUtil
//                                        .getProperty(ir.refah.application.util.ConfigUtil.PORTAL_TMP_FILES_LOCATION) +
//                                        randomFileName;
//                        outputStream = new FileOutputStream(physicalFileName);
//                        wb.write(outputStream);
//                        outputStream.flush();
//
//                        return new String[]{randomFileName, physicalFileName};
//                    } catch (Exception ex) {
//                        logger.debug(ex.getMessage(), ex);
//                        throw ex;
//                    } finally {
//                        if (outputStream != null) {
//                            outputStream.close();
//                        }
//                    }
//                }
//            });
//
//            String userName = PrincipalUtil.getCurrentUser().getUsername();
//            String jobTitle = SwitchApplicationProperties.getMessage("label.exportAllToXLSX");
//            AsyncJobRunner.addJob(futureTask, userName, JobType.FileDownload, jobTitle);
//        } catch (Exception ex) {
//            logger.error("error in export all to xlsx ", ex);
//        }
//    }
    OutputStream responseOut = null;
    public void exportToCSV(AjaxRequestTarget target) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        OutputStream responseOut = null;
        try {
            IDataProvider provider = this.dataProvider;
            long providerSize = provider.size();

            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet createdSheet = wb.getSheet("Sheet 1");
            if (createdSheet == null) {
                createdSheet = wb.createSheet("Sheet 1");
            }

            XSSFRow header = createdSheet.createRow(0);
            header.setHeightInPoints(20);

            XSSFCellStyle headerStyle = wb.createCellStyle();
            headerStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
            headerStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);

            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 9);
            headerStyle.setFont(headerFont);

            XSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
            cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);

            XSSFFont cellFont = wb.createFont();
            cellFont.setFontHeightInPoints((short) 9);
            cellStyle.setFont(cellFont);

            XSSFCell xssfCell;
            Map<String, String> abstractColVals = new HashMap<String, String>();
            int headerNum = 0;
            for (IColumn col : columns) {
                xssfCell = header.createCell(headerNum++);
                AbstractColumn acolumn = (AbstractColumn) col;
                if (acolumn.getDisplayModel() != null && acolumn.getDisplayModel().getObject() != null) {
                    if (showColumnName(acolumn.getDisplayModel().getObject().toString())) {
                    xssfCell.setCellValue(acolumn.getDisplayModel().getObject().toString());
                    }
                }
                xssfCell.setCellStyle(headerStyle);
            }

            Iterator iterator;
            int rowNum = 1;
            int colNum;
            long first = (this.getCurrentPage()) * this.getItemsPerPage();

            iterator = provider.iterator(first, (first + this.getItemsPerPage() > providerSize) ? providerSize % this.getItemsPerPage() : this.getItemsPerPage());

            while (iterator.hasNext()) {
                colNum = 0;
                XSSFRow row = createdSheet.createRow(rowNum);
                row.setHeightInPoints(20);
                xssfCell = row.createCell(colNum++);
                xssfCell.setCellValue(rowNum++);
                xssfCell.setCellStyle(cellStyle);

                Object o = iterator.next();
                for (IColumn column : columns) {
                    if (column instanceof EnumColumn) {
                        String prop = ((EnumColumn) column).getPropertyExpression();
                        Object value = PropertyResolver.getValue(prop, o);

                        if (value != null) {
                            xssfCell = row.createCell(colNum);
                            xssfCell.setCellValue(((EnumColumn) column).getEnumCaption(value));
                            xssfCell.setCellStyle(cellStyle);
                        }
                        colNum++;
                    } else if (column instanceof PersianDateColumn) {
                        String prop = ((PropertyColumn) column).getPropertyExpression();
                        Object value = PropertyResolver.getValue(prop, o);

                        if (value != null) {
                            xssfCell = row.createCell(colNum);
                            if (value instanceof DateTime) {
                                xssfCell.setCellValue((MyCalendarUtil.toPersian((DateTime) value)).toString());
                            } else if (value instanceof DayDate) {
                                xssfCell.setCellValue((MyCalendarUtil.toPersian((DayDate) value)).toString());
                            } else {
                                xssfCell.setCellValue(value.toString());
                            }
                            xssfCell.setCellStyle(cellStyle);
                        }
                        colNum++;
                    } else if (column instanceof PropertyColumn) {
                        String prop = ((PropertyColumn) column).getPropertyExpression();
                        Object value = PropertyResolver.getValue(prop, o);

                        if (value != null) {
                            xssfCell = row.createCell(colNum);
                            xssfCell.setCellValue(value.toString());
                            xssfCell.setCellStyle(cellStyle);
                        }
                        colNum++;
                    } else if (column instanceof MyAbstractColumnWithDataTag) {
                        Object value = ((MyAbstractColumnWithDataTag) column).getDataTag(o);
//                        System.out.println(value);
                        if (value != null) {
                            xssfCell = row.createCell(colNum);
                            xssfCell.setCellValue(value.toString());
                            xssfCell.setCellStyle(cellStyle);
                        }
                        colNum++;
                    } else if (column instanceof AbstractColumn && !(column instanceof IndexColumn)) {
                        xssfCell = row.createCell(colNum);
                        xssfCell.setCellValue("");
                        xssfCell.setCellStyle(cellStyle);
                        colNum++;
                    }
                }
            }
            wb.write(outputStream);
            outputStream.flush();

            String tempLocation = System.getProperty("java.io.tmpdir");
            String fileName = AppUtils.generateRandomString(5) + ".xlsx";
            final String tempFileName = tempLocation + "/" + fileName;
            FileOutputStream fout = new FileOutputStream(tempFileName);

            fout.write(outputStream.toByteArray());

            fout.close();

//            WebResponse response = (WebResponse) getResponse();
//            responseOut = response.getOutputStream();

            AjaxDownload ajaxDownload = new AjaxDownload() {
                @Override
                protected String getFileName() {
                    return fileName;
                }

                @Override
                protected IResourceStream getResourceStream() {
                    return new FileResourceStream(new File(tempFileName));
                }
            };
            add(ajaxDownload);

//            response.setAttachmentHeader(providerSize + "Records.xlsx");
//            ((ByteArrayOutputStream) outputStream).writeTo(responseOut);
            ajaxDownload.initiate(target);

        } catch (IOException e) {
            logger.error("exportToCSV: ", e);
        } finally {
            try {
                outputStream.close();
            } catch (Exception ex) {
                logger.debug("exportToCSV: closing outputStream Error ", ex);
            }
            try {
                if (responseOut != null)
                    responseOut.close();
            } catch (Exception ex) {
                logger.debug("exportToCSV: closing responseOutputStream Error ", ex);
            }
        }
    }

    private boolean showColumnName(String inputString) {

        List<String> unShowColumnName = new ArrayList<>();
        unShowColumnName.add(AppStorePropertyReader.getString("label.active"));
        unShowColumnName.add(AppStorePropertyReader.getString("AppCategory.isAssignable"));
        unShowColumnName.add(AppStorePropertyReader.getString("label.edit"));
        unShowColumnName.add(AppStorePropertyReader.getString("label.disable.verb"));
        unShowColumnName.add(AppStorePropertyReader.getString("label.disabled.verb"));
        unShowColumnName.add(AppStorePropertyReader.getString("label.activation.verb"));
        unShowColumnName.add(AppStorePropertyReader.getString("organization.default"));
        unShowColumnName.add(AppStorePropertyReader.getString("HandlerApp.isDefault"));
        unShowColumnName.add(AppStorePropertyReader.getString("HandlerApp.isDefault"));
        unShowColumnName.add(AppStorePropertyReader.getString("Anouncement.actionCategory"));
        unShowColumnName.add(AppStorePropertyReader.getString("Anouncement.isExpired"));
        for(String str : unShowColumnName){
            if(inputString.equals(str) ){
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onBeforeRender() {
        IDataProvider dp = getDataProvider();
        if (dp instanceof ListDataProvider) {
            long size = dp.size();
            setItemsPerPage(size > 0 ? size : 1);
        }
        super.onBeforeRender();
    }
}
