package com.fanap.midhco.appstore.applicationUtils;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.login.FanapSSOToken;
import com.fanap.midhco.ui.AppStoreSession;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.springframework.web.util.HtmlUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by admin123 on 7/3/2016.
 */
public class AppUtils {
//    final static Logger logger = Logger.getLogger(AppUtils.class);

    public static String generateRandomString(int length) {
        String randomStringSeed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuffer stringBuffer = new StringBuffer();
        Random random = new Random();
        for(int i = 0; i < length; i++)
            stringBuffer.append(randomStringSeed.charAt(random.nextInt(randomStringSeed.length())));
        return stringBuffer.toString();
    }

    public static String getHostName() throws UnknownHostException {
        InetAddress addr;
        addr = InetAddress.getLocalHost();
        return addr.getHostName();
    }

    public static void deleteFilesInFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    public static String dateTagFileName(String fileName) {
        int ix = fileName.lastIndexOf(".");
        if (ix != -1) {
            return fileName.substring(0, ix) + "_" + DateTime.now().getDateTimeLong() +
                    fileName.substring(ix);
        }
        return fileName + DateTime.now().getDateTimeLong();
    }

    public static boolean isFileImage(String fileName) {
        String fileExt = getFileExtension(fileName);
        if (fileExt.toUpperCase().equals("PNG") || fileExt.toUpperCase().equals("JPG"))
            return true;
        return false;
    }

    public static byte[] getImageAsBytes(String path) {
        byte[] imageBytes = null;
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            URL url = new URL(path);
            String conetntType = url.openConnection().getContentType();
            String formatName ="";
            if(conetntType == null)
                return imageBytes;
            if(conetntType.contains("jpeg")){
                formatName= "jpg";
            } else if(conetntType.contains("png")) {
                formatName= "png";
            }
            BufferedImage originalImage = ImageIO.read(new URL(path));
            if(formatName==null || formatName.trim().equals("")){
                formatName = getFileExtension(path);
            }
            ImageIO.write(originalImage, formatName, outStream);
            outStream.close();
            return outStream.toByteArray();
        } catch (IOException e) {
//            logger.error("error occured in AppUtils.getImageAsBytes ", e);
        }
        return imageBytes;
    }

    public static void copy(InputStream source, OutputStream destination) throws IOException {

        byte[] buf = new byte[source.available()];
        int len;
        while ((len = source.read(buf)) > 0) {
            destination.write(buf, 0, len);
        }
        source.close();
        destination.close();
    }

    public static void fileCopy(String inputFileLocation, String outputFileLocation) throws IOException {
        FileInputStream fin = new FileInputStream(inputFileLocation);
        FileOutputStream fout = new FileOutputStream(outputFileLocation);
        copy(fin, fout);
    }

    public static String getFileExtension(String fileName) {
        int ix = fileName.lastIndexOf(".");
        String extension = fileName.substring(ix + 1);
        return extension.trim();
    }

    public static ImageFormat getImageFormat(String imageName) {
        if (imageName.toLowerCase().endsWith(".png"))
            return ImageFormat.PNG;
        else if (imageName.toLowerCase().endsWith(".gif"))
            return ImageFormat.GIF;
        else if (imageName.toLowerCase().endsWith(".jpg"))
            return ImageFormat.JPG;
        else if (imageName.toLowerCase().endsWith(".jpeg"))
            return ImageFormat.JPG;
        else if (imageName.toLowerCase().endsWith(".tiff"))
            return ImageFormat.TIFF;
        return ImageFormat.UNKNOWN;
    }

    public static String getImageThumbNail(String fileKey, String fileName) throws Exception {
        String tempFilePath = FileServerService.Instance.copyFileFromServerToTemp(fileKey);

        int thumb_width = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_APPSTORE_THUMBIMAGE_WIDTH));
        int thumb_height = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_APPSTORE_THUMBIMAGE_HEIGHT));

        ImageFormat imageFormat = AppUtils.getImageFormat(fileName);

        if(imageFormat.equals(ImageFormat.UNKNOWN)) {
            throw new Exception("UnKnown Image Format!");
        }

        Thumbnails.of(tempFilePath)
                .size(thumb_width, thumb_height)
                .outputFormat(imageFormat.getValue())
                .toFiles(Rename.PREFIX_DOT_THUMBNAIL);

        String tempFileLocation = System.getProperty("java.io.tmpdir");
        String thumbFileName = Rename.PREFIX_DOT_THUMBNAIL.apply(fileKey, null);
        if(!tempFileLocation.endsWith(File.separator))
            return tempFileLocation + File.separator + thumbFileName + "." + imageFormat.getValue();
        else
            return tempFileLocation + thumbFileName + "." + imageFormat.getValue();
    }

    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[0xFFFF];

            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);

            os.flush();

            return os.toByteArray();
        }
    }

    public static String getStringFromHtml(String inputHtmlString) {
        String htmlString = HtmlUtils.htmlUnescape(inputHtmlString);
        return htmlString.replaceAll("[<](/)?div[^>]*[>]", "").replaceAll("[<](/)?br[^>]*[>]", "");
    }

    public static void main(String[] args) throws Exception {
        getImageThumbNail("420DE934A1C766DC33CEA010E9A09D7E6.547063097842255E14.J", "ddd.jpg");
    }

    public static int compareStrings(String firstVersion, String secondVersion) {
        try {
            String[] firstLongArray = firstVersion.split("\\.");
            String[] secondLongArray = secondVersion.split("\\.");
            List<String> firstLong = Arrays.asList( firstLongArray);
            List<String>secondLong = Arrays.asList( secondLongArray);
            int index = 0;
            int compareInt = -2;
            if ((secondLong == null || secondLong.isEmpty()) && (firstLong != null && !firstLong.isEmpty())) {
                return Integer.valueOf(-1);
            }
            for (String secondVersionStr : secondLong) {
                Long secondVersionForCmp = Long.valueOf(secondLong.get(index));
                if (firstLong.size()>index) {
                    Long firstVersionForCmp = Long.valueOf(firstLong.get(index));

                    compareInt = secondVersionForCmp.compareTo(firstVersionForCmp);
                    if ( compareInt != Integer.valueOf("0")) {
                        return compareInt;
                    }
                } else {
                    return Integer.valueOf(1);
                }
                index++;
            }
            if (firstLong!=null && secondLong!=null &&firstLong.size()==(secondLong.size())) {
                return Integer.valueOf(0);
            }else{
                return Integer.valueOf(-1);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Invalid Version Number!", ex);
        }
    }

}
