package com.fanap.midhco.appstore.service.fileServer;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by admin123 on 7/11/2016.
 */
public class FileServerService {
    public final static FileServerService Instance = new FileServerService();
    protected static final Logger logger = Logger.getLogger(FileServerService.class);

    private FileServerService() {
    }

    final static String fileServerURL = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_URL);

    public final static String FILE_DOWNLOAD_SERVER_PATH;
    public final static String FILE_UPLOAD_SERVER_PATH;
    public final static String FILE_PERSIST_SERVER_PATH;
    public final static String FILE_DELETE_SERVER_PATH;
    public final static String FILE_EXIST_QUERY_SERVER_PATH;
    public final static String FILE_SERVER_DOWNLOAD_COUNT;
    public final static String FILE_EXIST_QUERY_SERVER_PACKAGE_FILE_SIZE;
    public final static String FILE_SERVER_GET_FILE_NAME;

    static {
        FILE_DOWNLOAD_SERVER_PATH = fileServerURL + "download?key=${key}";
        FILE_UPLOAD_SERVER_PATH = fileServerURL + "chunkUpload";
        FILE_PERSIST_SERVER_PATH = fileServerURL + "persist?key=${key}";
        FILE_DELETE_SERVER_PATH = fileServerURL + "delete?key=${key}";
        FILE_EXIST_QUERY_SERVER_PATH = fileServerURL + "isExist?key=${key}";
        FILE_SERVER_DOWNLOAD_COUNT = fileServerURL + "getNumber?key=${key}";
        FILE_EXIST_QUERY_SERVER_PACKAGE_FILE_SIZE = fileServerURL + "getSize?key=${key}";
        FILE_SERVER_GET_FILE_NAME = fileServerURL + "getName?key=${key}\n";
    }


    public static class FileDeleteException extends AppStoreRuntimeException {
        public FileDeleteException(String message, Exception ex) {
            super(message, ex);
        }

        public FileDeleteException(String message) {
            super(message);
        }

        public FileDeleteException(Exception ex) {
            super(ex);
        }
    }


    public static class FileDownloadException extends AppStoreRuntimeException {
        public FileDownloadException(String message, Exception ex) {
            super(message, ex);
        }

        public FileDownloadException(String message) {
            super(message);
        }

        public FileDownloadException(Exception ex) {
            super(ex);
        }
    }

    public static class FileUploadException extends AppStoreRuntimeException {
        public FileUploadException(String message, Exception ex) {
            super(message, ex);
        }

        public FileUploadException(String message) {
            super(message);
        }

        public FileUploadException(Exception ex) {
            super(ex);
        }
    }

    public static class UploadDescriptor {
        boolean persist;
        boolean inChunk;
        int chunkSize;

        public boolean isPersist() {
            return persist;
        }

        public void setPersist(boolean persist) {
            this.persist = persist;
        }

        public boolean isInChunk() {
            return inChunk;
        }

        public void setInChunk(boolean inChunk) {
            this.inChunk = inChunk;
        }

        public int getChunkSize() {
            return chunkSize;
        }

        public void setChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
        }
    }

    public Map<String, String> uploadFilesToServer(List<File> files, UploadDescriptor uploadDescriptor) throws
            FileUploadException {
        String charset = "UTF-8";

        String uploadURL = FILE_UPLOAD_SERVER_PATH;

        Map<String, String> retMap = new HashMap<>();

        files.stream().forEach(binaryFile -> {
            try {
                String boundary = Long.toHexString(System.currentTimeMillis());
                String CRLF = "\r\n";
                String param = binaryFile.getName();

                HttpURLConnection connection = (HttpURLConnection) new URL(uploadURL).openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

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
                    writer.append("Content-Disposition: form-data; name=\"" + param + "\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
                    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
                    writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                    writer.append(CRLF).flush();

                    if (uploadDescriptor.isInChunk()) {
                        FileInputStream fin = new FileInputStream(binaryFile);
                        byte[] chunkBytes = new byte[uploadDescriptor.chunkSize];
                        int readBit;
                        while ((readBit = fin.read()) != -1) {

                        }
                    }
                    Files.copy(binaryFile.toPath(), output);
                    output.flush();
                    writer.append(CRLF).flush();

                    writer.append("--" + boundary + "--").append(CRLF).flush();
                    writer.flush();
                }

                int responseCode = ((HttpURLConnection) connection).getResponseCode();
                if (responseCode != HttpStatus.OK.value()) {
                    throw new RuntimeException("file not uploaded successfuly!response returned from server is " + responseCode);
                }

                byte[] bytes = new byte[((HttpURLConnection) connection).getInputStream().available()];
                connection.getInputStream().read(bytes);
                String responseText = new String(bytes);

                retMap.put(binaryFile.getName(), responseText);
            } catch (Exception ex) {
                throw new FileUploadException(ex.getMessage(), ex);
            }
        });

        if (uploadDescriptor.isPersist()) {
            retMap.forEach((fileName, fileUploadCode) -> {
                try {
                    persistFileToServer(fileUploadCode);
                } catch (Exception ex) {
                    throw new FileUploadException(ex.getMessage(), ex);
                }
            });
        }

        return retMap;
    }

    public void downloadFileFromServer(String fileKey, OutputStream out) throws FileDownloadException, IOException {
        String fileDownloadURL = FILE_DOWNLOAD_SERVER_PATH;
        String concreateDownLoadURL = fileDownloadURL.replace("${key}", fileKey);
        try (InputStream input = new URL(concreateDownLoadURL).openStream()) {
            IOUtils.copy(input, out);
            out.flush();
        } catch (Exception ex) {
            throw new FileDownloadException(ex);
        } finally {
            out.close();
        }
    }

    public URL getDownloadURL(String fileKey) throws MalformedURLException {
        String downloadURL = FILE_DOWNLOAD_SERVER_PATH;
        return new URL(downloadURL.replace("${key}", fileKey));
    }

    public void deleteFileFromServer(String fileKey) throws FileDeleteException {
        try {
            String fileDeleteURL = FILE_DELETE_SERVER_PATH;
            String concreteDeleteURL = fileDeleteURL.replace("${key}", fileKey);

            URLConnection connection = new URL(concreteDeleteURL).openConnection();
            connection.setDoOutput(true);

            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            if (responseCode != HttpStatus.OK.value()) {
                throw new RuntimeException("file not deleted successfuly!response returned from server is " + responseCode);
            }

        } catch (Exception ex) {
            throw new FileDeleteException(ex.getMessage(), ex);
        }
    }

    public void persistFileToServer(String fileKey) {
        String persistURL = FILE_PERSIST_SERVER_PATH;
        try {
            String tempPersistURL = persistURL.replace("${key}", fileKey);
            URL url = new URL(tempPersistURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode != HttpStatus.OK.value()) {

                byte[] bytes = new byte[((HttpURLConnection) httpConn).getErrorStream().available()];
                httpConn.getErrorStream().read(bytes);
                String responseText = new String(bytes);

                throw new FileUploadException("error in persisting file on upload server! " + responseText);
            }
        } catch (Exception ex) {
            throw new FileUploadException(ex.getMessage(), ex);
        }
    }

    public boolean doesFileExistOnFileServer(String fileKey) {
        String fileQueryServerPath = FILE_EXIST_QUERY_SERVER_PATH;
        try {
            fileQueryServerPath = fileQueryServerPath.replace("${key}", fileKey);
            URL url = new URL(fileQueryServerPath);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode != HttpStatus.OK.value()) {
                throw new FileUploadException("error in getting fileInfo from file server!");
            }

            byte[] bytes = new byte[((HttpURLConnection) httpConn).getInputStream().available()];
            httpConn.getInputStream().read(bytes);
            String responseText = new String(bytes);

            if (responseText.equals("true"))
                return true;
            else
                return false;

        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }

    public Integer getFileDownloadCount(String fileKey) throws Exception {
        String fileQueryServerPath = FILE_SERVER_DOWNLOAD_COUNT;
        fileQueryServerPath = fileQueryServerPath.replace("${key}", fileKey);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(fileQueryServerPath);
        HttpResponse httpResponse = client.execute(httpGet);
        URL url = new URL(fileQueryServerPath);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
        if (responseCode != HttpStatus.OK.value()) {
            throw new Exception(String.valueOf(responseCode));
        }
        String json_string = EntityUtils.toString(httpResponse.getEntity());
        return (json_string != null && !json_string.trim().isEmpty()) ? Integer.parseInt(json_string) : 0;
    }

    public String copyFileFromServerToTemp(String fileKeyInFileServer) throws Exception {
        String tempLocation = System.getProperty("java.io.tmpdir");
        String tempFileName = tempLocation + "\\" + fileKeyInFileServer;
        FileOutputStream fout = new FileOutputStream(tempFileName);
        downloadFileFromServer(fileKeyInFileServer, fout);
        return tempFileName;
    }


    public Long getFileSizeByFileKey(String packageFileKey) throws IOException {
        String fileQueryServerPath = FileServerService.FILE_EXIST_QUERY_SERVER_PACKAGE_FILE_SIZE;
        fileQueryServerPath = fileQueryServerPath.replace("${key}", packageFileKey);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(fileQueryServerPath);

        HttpResponse response = client.execute(httpGet);

        String json_string = EntityUtils.toString(response.getEntity());
        return Long.valueOf(json_string);
    }

    public String getFileNameFromFilePath(String filePath) {
        if (filePath != null && !filePath.trim().equals("")) {
            try {
                String urlString = FILE_SERVER_GET_FILE_NAME;
                urlString = urlString.replace("${key}", filePath);

                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(RequestMethod.GET.name());


                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                }


            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static class DeleteTempFolderJob implements Job {
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            logger.debug("start job for delete Temp Folder ");
            checkAndDeleteTempFolder();
        }

        private void checkAndDeleteTempFolder() {
            String tempLocation = System.getProperty("java.io.tmpdir");
            Long size = null;
            if (tempLocation != null && !tempLocation.trim().equals("")) {
                if (!tempLocation.isEmpty()) {
                    File directoryFile = new File(tempLocation);
                    size = getDirectorySize(directoryFile);

                    Long maxTempSize = Long.parseLong(ConfigUtil.getProperty(ConfigUtil.MAX_TEMP_FOLDER_SIZE));
                    logger.debug("tempLocation  :  " + tempLocation);
                    logger.debug("directory size  :  " + size);
                    logger.debug("maxTempSize should be  :  " + maxTempSize);
                    if (size > maxTempSize) {
                        logger.debug("larger than expected and attempt to delete directory");
                        List<String> filePathArrayList = new ArrayList<>();
                        deleteDirectory(directoryFile, filePathArrayList);
                        size = getDirectorySize(directoryFile);
                        if (size > maxTempSize) {
                            logger.error("error on delete Directory some file(s) con not be deleted");
                            logger.error("list of directories that can not be deleted");
                            for (String str : filePathArrayList) {
                                logger.error(str);
                            }
                        } else {
                            logger.debug("delete complete");
                        }
                    }
                }
            }

        }

        private void deleteDirectory(File directoryFile, List<String> filePath) {

            Date currentDate = new Date();
            Long checkSumHour = Long.parseLong(ConfigUtil.getProperty(ConfigUtil.MAX_TEMP_FILES_CREATION_DATE_BY_HOUR));
            Long checkSumLong = currentDate.getTime() - checkSumHour * 60 * 60 * 1000;
            if (directoryFile != null) {
                for (File file : directoryFile.listFiles()) {
                    try {
                        if (file.isDirectory()) {
                            if (file.lastModified() < checkSumLong) {
                                FileUtils.deleteDirectory(file);
                            }
                        } else {
                            if (file.lastModified() < checkSumLong) {
                                FileUtils.forceDelete(file);
                            }
                        }
                    } catch (Exception e) {
                        if (file.isDirectory()) {
                            filePath.add(file.getPath());
                        }
                    }
                }
            }
        }

        private Long getDirectorySize(File directoryFile) {
            long fileSize = 0;
            try {
                for (File file : directoryFile.listFiles()) {
                    if (file.isDirectory()) {
                        fileSize += getDirectorySize(file);
                    } else {
                        fileSize += file.length();
                    }
                }
            } catch (Exception e) {
                logger.error("error on get file size", e);
                e.printStackTrace();
            }
            return fileSize;
        }
    }
}
