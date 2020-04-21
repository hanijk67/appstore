package com.fanap.midhco.ui.pages.app;

import com.fanap.midhco.appstore.applicationUtils.DataUtil;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.service.comment.CommentService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.osType.IUploadFilter;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.ajaxButton.AjaxFormButton;
import com.fanap.midhco.ui.component.ajaxDownload.AjaxDownload;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.MultiAjaxFileUploadPanel2;
import com.fanap.midhco.ui.component.multiAjaxFileUpload2.UploadedFileInfo;
import io.searchbox.client.JestResult;
import javassist.NotFoundException;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by A.Moshiri on 9/28/2017.
 */
public class AppGroupCommentImportForm extends BasePanel {
    Form form;
    FeedbackPanel feedbackPanel;
    MultiAjaxFileUploadPanel2 chooseExcelFilePanel;
    String excelFilePath = null;
    BootStrapModal modal = new BootStrapModal("modal");
    AjaxDownload fileAjaxDownload;
    String excelFileName = null;


    protected AppGroupCommentImportForm(String id) {
        super(id);


        add(modal);
        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(feedbackPanel);

        form = new Form("form");


        List<IUploadFilter> excelFilters = new ArrayList<>();
        excelFilters.add(IUploadFilter.getExcelUploadFilter());

        chooseExcelFilePanel = new MultiAjaxFileUploadPanel2("ChooseExcelFile", excelFilters, 1, false, getString("app.comment.excel.file"));
        chooseExcelFilePanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        chooseExcelFilePanel.setLabel(new ResourceModel("app.comment.excel.file"));
        chooseExcelFilePanel.setModel(new Model());
        form.add(chooseExcelFilePanel);
        String secondFilePath = System.getProperty("java.io.tmpdir") + "/" + "reportApproveComment.xlsx";


        form.add(new AjaxFormButton("save", form) {
            @Override
            protected void onSubmit(Form form, AjaxRequestTarget target) {
                FileServerService.UploadDescriptor uploadDescriptor = new FileServerService.UploadDescriptor();
                String validationString = "";
                String charset = "UTF-8";

                try {
                    Collection<UploadedFileInfo> excelFileList = (Collection<UploadedFileInfo>) chooseExcelFilePanel.getConvertedInput();
                    Iterator<UploadedFileInfo> uploadedExcelFileIterator;


                    if (excelFileList != null && excelFileList.size() > 0) {
                        uploadedExcelFileIterator = excelFileList.iterator();
                    } else {
                        uploadedExcelFileIterator = null;
                    }
                    List<UploadedFileInfo> uploadedExcelFileList = new ArrayList();
                    if (uploadedExcelFileIterator != null) {
                        while (uploadedExcelFileIterator.hasNext()) {
                            UploadedFileInfo tmpUploadedFileInfo = uploadedExcelFileIterator.next();
                            uploadedExcelFileList.add(tmpUploadedFileInfo);
                        }
                    }

                    if (uploadedExcelFileList != null && uploadedExcelFileList.size() > 0) {
                        UploadedFileInfo excelFile = uploadedExcelFileList.get(0);

                        String tempFileLocation = tempFileLocation = FileServerService.Instance.copyFileFromServerToTemp(excelFile.getFileId());
                        File file = new File(tempFileLocation);

                        String uploadURL = FileServerService.FILE_UPLOAD_SERVER_PATH;
                        HttpURLConnection connection = (HttpURLConnection) new URL(uploadURL).openConnection();
                        connection.setDoOutput(true);
                        String boundary = Long.toHexString(System.currentTimeMillis());
                        String CRLF = "\r\n";
                        String param = file.getName();
                        excelFileName = param;
                        try (
                                OutputStream output = connection.getOutputStream();
                                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
                        ) {
                            writer.append("--" + boundary).append(CRLF);
                            writer.append("Content-Disposition: form-data; name=\"name\"").append(CRLF);
                            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                            writer.append(CRLF).append(param).append(CRLF).flush();

                            // Send binary file.
                            writer.append("--" + boundary).append(CRLF);
                            writer.append("Content-Disposition: form-data; name=\"" + param + "\"; filename=\"" + file.getName() + "\"").append(CRLF);
                            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName())).append(CRLF);
                            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                            writer.append(CRLF).flush();

                            if (uploadDescriptor.isInChunk()) {
                                FileInputStream fin = new FileInputStream(file);
                                byte[] chunkBytes = new byte[uploadDescriptor.getChunkSize()];
                                int readBit;
                                while ((readBit = fin.read()) != -1) {

                                }
                            }
                            Files.copy(file.toPath(), output);

                            output.flush();
                            writer.append(CRLF).flush();

                            writer.append("--" + boundary + "--").append(CRLF).flush();
                            writer.flush();
                            excelFilePath = file.toString();
                        }

                    } else {
                        validationString += " - " +
                                getString("Required").replace("${label}", getString("app.comment.excel")) + "<br/>";
                    }

                    if (!validationString.isEmpty()) {
                        target.appendJavaScript("showMessage('" + validationString + "');");
                        return;
                    }

                    if (excelFilePath != null) {

                        int rows = 0;
                        Sheet sheet = null;
                        Row row = null;

                        XSSFWorkbook excelFile = new XSSFWorkbook(excelFilePath);
                        sheet = excelFile.getSheetAt(0);
                        rows = sheet.getPhysicalNumberOfRows();
                        User currentUser = PrincipalUtil.getCurrentUser();
                        XSSFCellStyle style = excelFile.createCellStyle();
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setFillForegroundColor(IndexedColors.RED.getIndex());

                        for (int r = 0; r < rows; r++) {
                            try {
                                if (r == 0)
                                    continue;
                                row = sheet.getRow(r);

                                if (row != null && row.getCell(0) != null) {
                                    String commentId =
                                            DataUtil.getCellData(row.getCell(1), String.class);

                                    String description =
                                            URLDecoder.decode(DataUtil.getCellData(row.getCell(4), String.class), "UTF-8");

                                    String status =
                                            DataUtil.getCellData(row.getCell(6), String.class);
                                    Double statusDouble = Double.valueOf(status);
                                    if (!(statusDouble.equals(Double.valueOf(1)) || statusDouble.equals(Double.valueOf(0)))) {
                                        throw new NumberFormatException("");
                                    }

                                    CommentService.ElasticCommentVO elasticCommentVO = new CommentService.ElasticCommentVO();
                                    elasticCommentVO.setId(commentId);
                                    elasticCommentVO.setApproved(statusDouble.equals(Double.valueOf(1)) ? true : false);
                                    elasticCommentVO.setUserName(currentUser.getUserName());

                                    CommentService.ElasticApproveVO elasticApproveVO = CommentService.Instance.buildApproveVoByElasticCommentVo(elasticCommentVO);
                                    elasticApproveVO.setDescription(description);
                                    CommentService.CommentVOCriteria commentVOCriteria = new CommentService.CommentVOCriteria();
                                    commentVOCriteria.id = elasticCommentVO.getId();
                                    List<CommentService.ElasticCommentVO> elasticCommentVOS = CommentService.Instance.getCommentsForApp(commentVOCriteria, 0, -1, null, false);

                                    if (elasticApproveVO != null && !elasticCommentVOS.isEmpty()) {
                                        JestResult jestResult = CommentService.Instance.insertCommentForAppMainPackage(elasticCommentVO);
                                        JestResult jestResultApprove = CommentService.Instance.approveComment(elasticCommentVO.getId(), elasticApproveVO);
                                    } else {
                                        throw new NotFoundException(elasticCommentVO.getId());
                                    }
                                    row.createCell(7).setCellValue(getString("app.comment.change"));
                                }
                            } catch (NumberFormatException ex) {
                                row.createCell(7).setCellValue(getString("app.comment.invalid.approve.status"));
                                row.getCell(7).setCellStyle(style);
                                row.setRowStyle(style);
                            } catch (NotFoundException ex) {
                                row.createCell(7).setCellValue(getString("error.parent.comment.not.found"));
                                row.getCell(7).setCellStyle(style);
                            } catch (Exception ex) {
                                row.createCell(7).setCellValue(ex.getMessage());
                                row.getCell(7).setCellStyle(style);
                            }
                        }
                        File secondFile = new File(secondFilePath);
                        if (!secondFile.exists()) {
                            secondFile.createNewFile();
                        }
                        FileOutputStream fileOut = new FileOutputStream(new File(secondFilePath));
                        excelFile.write(fileOut);
                        fileOut.close();
                    }
                    fileAjaxDownload.initiate(target);
                    modal.close(target);
                } catch (Exception e) {
                    processException(target, e);
                }
            }
        });

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                modal.close(target);
            }
        });

        add(form);

        fileAjaxDownload = new AjaxDownload() {

            @Override
            protected String getFileName() {
                return "result.xlsx";
            }

            @Override
            protected IResourceStream getResourceStream() {
                try {
                    File resourceFile = new File(secondFilePath);
                    return new FileResourceStream(resourceFile);
                } catch (Exception ex) {

                }
                return null;
            }
        };
        add(fileAjaxDownload);

    }

}
