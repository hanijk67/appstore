package com.fanap.midhco;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.ui.AppStoreSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.json.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;


/**
 * Created by admin123 on 6/27/2016.
 */
public class UploadRecieveServlet extends HttpServlet {
    static Logger logger = Logger.getLogger(UploadRecieveServlet.class);

    private static String generateRandomUUID(String combineName) {
        return combineName + UUID.randomUUID().toString();
    }

    public final void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
//        logger.debug("file upload request recieved from ip " + servletRequest.getRemoteHost());
//
//        AppStoreSession appStoreSession = (AppStoreSession) AppStoreSession.get();
//
//        if (appStoreSession != null) {
//
//            if (appStoreSession.isAuthenticated()) {
//                try {
//
//                    String tempFilesLocation = ConfigUtil.getProperty(ConfigUtil.APP_TEMP_FILES_LOCATION);
//
//                    String file_id = servletRequest.getParameter("file_id");
//                    String file_name = servletRequest.getParameter("fileName");
//
//                    List<FileItem> items = null;
//
//                    try {
//                        DiskFileItemFactory factory = new DiskFileItemFactory();
//                        ServletFileUpload upload = new ServletFileUpload(factory);
//                        items = upload.parseRequest(servletRequest);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//
//                    FileItem fileItem;
//
//
//                    if (items != null && !items.isEmpty()) {
//
//                        fileItem = items.get(items.size() - 1);
//
//                        FileOutputStream fo = new FileOutputStream(tempFilesLocation + file_id, true);
//
//                        try {
//                            fo.write(fileItem.get());
//                            fo.close();
//                        } catch (Exception ex) {
//                            logger.error("error writing upload file on disk : ", ex);
//                            servletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                            servletResponse.getWriter().write("Error occured writing file on server!");
//                            fo.close();
//                        }
//
//                        servletResponse.setStatus(HttpServletResponse.SC_OK);
//                        JSONObject jsonObject = new JSONObject();
//                        jsonObject.put("message", "successfully upload!");
//                        jsonObject.put("fileId", file_id);
//                        jsonObject.put("fileName", file_name);
//                        servletResponse.getWriter().write(jsonObject.toString());
//
////                UploadedFileInfo uploadedFileInfo = new UploadedFileInfo();
////                uploadedFileInfo.fileContent = fileItem.get();
////                uploadedFileInfo.fileName = fileName;
////
////                byte[] prevBytes = uploadedFileInfo.getFileContent();
////                byte[] newChunkBytes = fileItem.get();
////                byte[] c = new byte[prevBytes.length + newChunkBytes.length];
////                System.arraycopy(prevBytes, 0, c, 0, prevBytes.length);
////                System.arraycopy(newChunkBytes, 0, c, prevBytes.length, newChunkBytes.length);
////                uploadedFileInfo.setFileContent(c);
////
////                uploadedFilesInfoMap.put(file_id, uploadedFileInfo);
//
//                    }
//
////                    target.appendJavaScript("alert('" + fileUploadCompleteBehaviour.getCallbackUrl() + "');");
//                } catch (Exception e) {
//                    servletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    servletResponse.getWriter().write("Error occured writing file on server!");
//                    logger.debug("FileUpload Error :", e);
//                }
//            } else {
//                servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                servletResponse.getWriter().write("unAuthorized request to upload file on this server!");
//            }
//        }
    }
}
