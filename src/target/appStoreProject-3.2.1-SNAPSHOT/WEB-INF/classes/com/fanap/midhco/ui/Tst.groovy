import com.dd.plist.*
import com.fanap.midhco.appstore.iosUtil.IPAInfo
import com.fanap.midhco.appstore.iosUtil.IPAReader
import com.fanap.midhco.appstore.service.app.AppPackageService
import com.fanap.midhco.appstore.service.app.IAPPPackageService
import com.fanap.midhco.appstore.service.myException.appBundle.AppBundleNotSignedException
import com.fanap.midhco.appstore.service.myException.appBundle.AppBundleSignNotValidException
import com.fanap.midhco.appstore.service.myException.appBundle.BaseAppBundleException
import com.fanap.midhco.appstore.service.osType.IUploadFilter
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader
import jdk.internal.org.xml.sax.SAXException
import sun.security.x509.X509CertImpl

import javax.xml.parsers.ParserConfigurationException
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.text.ParseException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

def
static IPAInfo parseIosPackage(String ipaPath) throws IOException, PropertyListFormatException, ParseException, ParserConfigurationException, SAXException {
    Map<String, String> hashedDataMap = new HashMap<>();
    Certificate mainPackageCertificate = IPAReader.getCertificateAndFileKeys(hashedDataMap, ipaPath);
    if (mainPackageCertificate == null)
        throw new AppPackageService.APPPackageException(AppStorePropertyReader.getString("App.previousApp.has.no.certificate"));
    IPAInfo info = IPAReader.getIpaInfoFromIpaPath(mainPackageCertificate, hashedDataMap, ipaPath);
    if (info == null || !info.getHasCorrectHashValue())
        throw new AppPackageService.APPPackageException(AppStorePropertyReader.getString("App.certificate.not.valid"));
    info.setCertificate(mainPackageCertificate);
    return info;
}

def static IUploadFilter getUploadFilter() {
    return new IUploadFilter() {
        @Override
        String getFilterTitle() {
            return "ipa files";
        }

        @Override
        List<String> getFilterList() {
            return Arrays.asList("ipa");
        }
    }
}

def static IAPPPackageService parse(String fileName) throws IOException, Exception {

    String versionCode = "";
    String versionName = "";
    String packageName = "";
    String minimumOSVersion = "";
    String platFormVersion = "";
    List<String> usesPermissionList = new ArrayList<>();
    Certificate certificate = null;

    IPAInfo ipaInfo = null;
    try {
        ipaInfo = parseIosPackage(fileName);
        versionCode = ipaInfo.getBundleVersionString();
        minimumOSVersion = ipaInfo.getMinimumOSVersion();
        platFormVersion = ipaInfo.getPlatformVersion();
        usesPermissionList = ipaInfo.getPermissions();
        certificate = ipaInfo.getCertificate();
        versionName = ipaInfo.getBundleVersionString();
        packageName = ipaInfo.getBundleIdentifier();
    } catch (Exception ex) {
        ex.printStackTrace();
        throw ex;
    }

    return new IAPPPackageService() {
        @Override
        String getVersionCode() {
            versionCode;
        }

        @Override
        String getVersionName() {
            return versionName;
        }

        @Override
        String getPackage() {
            return packageName;
        }

        @Override
        String getMinSDK() {
            minimumOSVersion;
        }

        @Override
        String getTargetSDK() {
            platFormVersion;
        }

        @Override
        Certificate verifyPackage(Certificate previousCertificate) throws BaseAppBundleException {
            Map<String, String> hashedDataMap = new HashMap<>();
            Certificate ipaCertificate = IPAReader.getCertificateAndFileKeys(hashedDataMap, fileName);
            try {
                if (ipaCertificate == null) {
                    throw new AppBundleNotSignedException("certificate file not found!");
                } else if (certificate.getPublicKey() == null) {
                    throw new AppBundleSignNotValidException("no certificate found in app sign file!");
                } else {
                    X509CertImpl x509Cert = (X509CertImpl) ipaCertificate;
                    X509CertImpl previousX509Cert = (X509CertImpl) previousCertificate;
                    byte[] newCertSignature = x509Cert.getSignature();
                    if (previousCertificate!=null) {
                        byte[] previousCertSignature = previousX509Cert.getSignature();
                        if(previousCertSignature==null){
                            throw new AppBundleSignNotValidException("no previous certificate found in app sign file!");
                        }
                        boolean hasSameSignature = Arrays.equals(newCertSignature, previousCertSignature);
                        if (!hasSameSignature) {
                            throw new AppPackageService.APPPackageException(AppStorePropertyReader.getString("App.certificate.not.valid"));
                        }
                    }
                }

                IPAInfo newIpaInfo = IPAReader.getIpaInfoFromIpaPath(certificate , hashedDataMap , fileName);
                if(!newIpaInfo.getHasCorrectHashValue()){
                    throw new AppPackageService.APPPackageException(AppStorePropertyReader.getString("App.certificate.not.valid"));
                }


                return ipaCertificate;
            } catch (Exception ex) {
                println "Ali mirza + error on Groovy Script?"
                ex.printStackTrace();
                throw new Exception(ex);
            }
        }

        @Override
        List<String> getPermissions() {
            usesPermissionList;
        }
    }

}

def static boolean IsDeltaUpdatable() {
    return true;
}

def static boolean checkFileExistenceInPackage(ZipInputStream zipIn, String fileName, Boolean wholePath) throws Exception {
    try {
        ZipEntry zipEntry;
        while ((zipEntry = zipIn.getNextEntry()) != null) {
            String fileNameInZip = zipEntry.getName();
            int lastIndexOfSlash = fileNameInZip.lastIndexOf("/");
            String finalFileName = fileNameInZip.substring(lastIndexOfSlash > -1 ? lastIndexOfSlash + 1 : 0);
            if (finalFileName.equals(fileName)) {
                return true;
            }
        }
        return false;
    } catch (Exception ex) {
        throw new RuntimeException(ex.getMessage(), ex);
    }
}

def static String getDeltaPackage(URL prevURL, URL currentURL) {
    /*
    JarFile jarFile1 = null;
    JarFile jarFile2 = null;
    try {
        jarFile1 = JarUtil.retrieveJarFileFromURL(prevURL);
        jarFile2 = JarUtil.retrieveJarFileFromURL(currentURL);

        Manifest firstManifest = jarFile1.getManifest();
        Map<String, Attributes> firstManifestEntries = firstManifest.getEntries();

        Manifest secondManifest = jarFile2.getManifest();
        Map<String, Attributes> secondManifestEntries = secondManifest.getEntries();

        List<String> differKeys = new ArrayList<>();
        secondManifestEntries.keySet().each { key ->
            Attributes attributesInFirstJar = firstManifestEntries.get(key);
            if (attributesInFirstJar == null) {
                differKeys.add(key);
            } else {
                String hashValueInFirst = attributesInFirstJar.getValue("SHA1-Digest");
                String hashValueInSecond = secondManifestEntries.get(key).getValue("SHA1-Digest");

                if (!hashValueInFirst.equals(hashValueInSecond)) {
                    differKeys.add(key);
                }
            }
        }

        differKeys.add("META-INF/CERT.RSA");
        differKeys.add("META-INF/CERT.SF");
        differKeys.add("META-INF/MANIFEST.MF");

        String temp = System.getProperty("java.io.tmpdir");
        if (!temp.endsWith(File.separator))
            temp = temp + File.separator;
        println "--------------------------------->>>>" + temp;
        String tempOutZip = temp + DateTime.now().dateTimeLong +
                "_" + AppUtils.generateRandomString(5) + "." + getUploadFilter().filterList.get(0);

        ZipOutputStream zipOutputStream = new ZipOutputStream(
                new FileOutputStream(tempOutZip));
        for (String differKey : differKeys) {
            ZipEntry zipEntry = jarFile2.getEntry(differKey);
            InputStream inStream = jarFile2.getInputStream(zipEntry);

            zipOutputStream.putNextEntry(new ZipEntry(differKey));
            int count;
            byte[] bt = new byte[100];
            while ((count = inStream.read(bt, 0, 100)) != -1) {
                zipOutputStream.write(bt, 0, count);
            }
        }
        zipOutputStream.flush();
        zipOutputStream.close();

        File file = new File(tempOutZip);
        Map<String, String> mp = FileServerService.Instance.uploadFilesToServer(Arrays.asList(file), new FileServerService.UploadDescriptor());
        println "--------------fileName is " + file.getName() + "------------------------" + mp.get(file.getName()) + "------file size is " + file.size();
        return mp.get(file.getName());
    } catch (Exception ex) {
        throw ex;
    } finally {
        if (jarFile1 != null)
            jarFile1.close();
        if (jarFile2 != null)
            jarFile2.close();
    }
    */
    //todo get DeltaPackage
    return "toDo get DeltaPackage";
}

def static Comparator getVersionComparator() {
    return new Comparator<String>() {
        @Override
        int compare(String oldVersion, String newVersion) {
            try {
                long oldVersionAsLong = Long.parseLong(oldVersion);
                long newVersionAsLong = Long.parseLong(newVersion);

                return newVersionAsLong.compareTo(oldVersionAsLong);
            } catch (Exception ex) {
                throw new RuntimeException("Invalid Version Number!", ex);
            }
        }
    }
}

private static X509Certificate[] getAChain(Certificate[] certs, int startIndex) {
    if (startIndex > certs.length - 1)
        return null;

    int i;
    // Keep going until the next certificate is not the
    // issuer of this certificate.
    for (i = startIndex; i < certs.length - 1; i++) {
        if (!((X509Certificate) certs[i + 1]).getSubjectDN().
                equals(((X509Certificate) certs[i]).getIssuerDN())) {
            break;
        }
    }
    // Construct and return the found certificate chain.
    int certChainSize = (i - startIndex) + 1;
    X509Certificate[] ret = new X509Certificate[certChainSize];
    for (int j = 0; j < certChainSize; j++) {
        ret[j] = (X509Certificate) certs[startIndex + j];
    }
    return ret;
}

public static void main(String[] args) {
    String file = "E:\\projects\\Mobile\\app\\extracted\\MBank.app\\embedded.mobileprovision";

    String ipaPath = "E:\\projects\\Mobile\\app\\mBank";
//    String ipaPath = "E:\\projects\\Mobile\\app\\mBank (2).ipa";

//    IPAInfo ipaInfo = parseIosPackage(ipaPath);
    IAPPPackageService iappPackageService = parse(ipaPath);
//    println "iipaPackageService = $iipaPackageService.minimumOSVersion";
//    println "iipaPackageService.minimumOSVersion = $iipaPackageService.minimumOSVersion"
//    println "iipaPackageService.appIDName = $iipaPackageService.appIDName"

    try {
        URL prevURL = new URL("file:\\D:\\diff_problem\\MIDRP com.fanap.fanrp.midrp.herour-14.0.warc");
        URL currentURL = new URL("file:\\D:\\diff_problem\\MIDRP com.fanap.fanrp.midrp.herour-15.0.warc");

        String fileKey = getDeltaPackage(prevURL, currentURL);


    } catch (Exception ex) {
        ex.printStackTrace();
    }

    File apkfile = new File("C:\\Users\\admin123\\Desktop\\navin\\c_2.apk");
    if (!apkfile.exists()) {

        System.err.println("Error: File Not Found: " + apkfile);
        System.exit(-1);
    }


}

