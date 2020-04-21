import com.fanap.midhco.appstore.applicationUtils.AppUtils
import com.fanap.midhco.appstore.applicationUtils.JarUtil
import com.fanap.midhco.appstore.entities.helperClasses.DateTime
import com.fanap.midhco.appstore.service.app.IAPPPackageService
import com.fanap.midhco.appstore.service.cipher.JarVerifier
import com.fanap.midhco.appstore.service.fileServer.FileServerService
import com.fanap.midhco.appstore.service.myException.appBundle.AppBundleNotSignedException
import com.fanap.midhco.appstore.service.myException.appBundle.AppBundleSignNotValidException
import com.fanap.midhco.appstore.service.myException.appBundle.AppStoreNoSimilarCertificateFoundException
import com.fanap.midhco.appstore.service.myException.appBundle.BaseAppBundleException
import com.fanap.midhco.appstore.service.osType.IUploadFilter
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import sun.security.x509.X509CertImpl

import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class AndroidXMLDecompress {
    // decompressXML -- Parse the 'compressed' binary form of Android XML docs
    // such as for AndroidManifest.xml in .apk files
    public static int endDocTag = 0x00100101;
    public static int startTag = 0x00100102;
    public static int endTag = 0x00100103;

    static void prt(String str) {
    }

}

def static IUploadFilter getUploadFilter() {
    return new IUploadFilter() {
        @Override
        String getFilterTitle() {
            return "apk files";
        }

        @Override
        List<String> getFilterList() {
            return Arrays.asList("apk");
        }
    }
}

def static IAPPPackageService parse(String fileName) throws IOException {

    String versionCode = "";
    String versionName = "";
    String packageName = "";
    String minSDK = "";
    String targetSDK = "";
    List<String> usesPermissionList = new ArrayList<>();

    ApkFile apkFile = null;
    try {
        apkFile = new ApkFile(fileName);
        ApkMeta apkMeta = apkFile.getApkMeta();

        versionCode = apkMeta.getVersionCode();
        versionName = apkMeta.getVersionName();
        packageName = apkMeta.getPackageName();
        minSDK = apkMeta.getMinSdkVersion();
        targetSDK = apkMeta.getTargetSdkVersion();
        usesPermissionList = apkMeta.getUsesPermissions();

    } catch (Exception ex) {
        ex.printStackTrace();
    } finally {
        if (apkFile != null) {
            apkFile.close();
        }
    }


    return new IAPPPackageService() {
        @Override
        String getVersionCode() {
            versionCode;
        }

        @Override
        String getVersionName() {
            versionName;
        }

        @Override
        String getPackage() {
            packageName;
        }

        @Override
        String getMinSDK() {
            return minSDK;
        }

        @Override
        String getTargetSDK() {
            return targetSDK;
        }

        @Override
        Certificate verifyPackage(Certificate previousCertficate) throws Exception {
            ZipFile zip = null;
            println "fileName is " + fileName;
            try {
                zip = new ZipFile(fileName);
                ZipEntry certRSAFile = zip.getEntry("META-INF/CERT.RSA");
                if (certRSAFile == null) {
                    throw new AppBundleNotSignedException("META-INF/CERT.RSA file not found!");
                }

                java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
                InputStream is = zip.getInputStream(certRSAFile);
                Collection c = cf.generateCertificates(is);

                if (c.size() > 1)
                    throw new AppBundleSignNotValidException("more than one certificate found in app sign file");

                Iterator i = c.iterator();
                if (i.hasNext()) {
                    X509CertImpl cert = (X509CertImpl) i.next();
                    if (cert.getIssuerDN().getName().equals("C=US, O=Android, CN=Android Debug")) {
                        throw new AppBundleNotSignedException("app signed with default key!");
                    }
                    JarVerifier jarVerifier = new JarVerifier(new URL("file:" + fileName));
                    jarVerifier.verify(previousCertficate);
                    return cert;
                } else {
                    throw new AppBundleSignNotValidException("no certificate found in app sign file!");
                }
            } catch (Exception ex) {
                println "SAG SAG SAG"
                ex.printStackTrace();
                if (ex instanceof JarVerifier.UnSignedEntryFoundInJarFile) {
                    throw new AppBundleSignNotValidException(ex);
                } else if (ex instanceof JarVerifier.NotSignedByTrustedSignerException) {
                    throw new AppStoreNoSimilarCertificateFoundException(ex);
                } else if (ex instanceof JarVerifier.UnsignedPackageException) {
                    throw new AppBundleNotSignedException(ex);
                } else if (ex instanceof BaseAppBundleException) {
                    throw ex;
                } else if (ex instanceof SecurityException) {
                    throw new AppBundleSignNotValidException(ex);
                }
            } finally {
                if (zip != null)
                    zip.close();
            }
        }

        @Override
        List<String> getPermissions() {
            return usesPermissionList;
        }
    }
}

def static boolean IsDeltaUpdatable() {
    return true;
}


def static boolean checkFileExistenceInPackage(ZipInputStream zipIn,ZipFile zipFile, String fileName, Boolean wholePackage) throws Exception {
    try {
        if (wholePackage) {
            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                String fileNameInZip = zipEntry.getName();
                int lastIndexOfSlash = fileNameInZip.lastIndexOf("/");
                String finalFileName = fileNameInZip.substring(lastIndexOfSlash > -1 ? lastIndexOfSlash + 1 : 0);
                if (finalFileName.equals(fileName)) {
                    return true;
                }
            }
        } else {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    File file = new File(entry.getName());
                    if (file.getParent() == null) {
                        if (file.getName().equals(fileName)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    } catch (Exception ex) {
        throw new RuntimeException(ex.getMessage(), ex);
    }
}

def static String getDeltaPackage(URL prevURL, URL currentURL) {
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
//    ApkFile apkFile = null;
//    try {
//        apkFile = new ApkFile("C:\\Users\\admin123\\Desktop\\navin\\c_2.apk");
//        ApkMeta apkMeta = apkFile.getApkMeta();
//
//        versionCode = apkMeta.getVersionCode();
//        versionName = apkMeta.getVersionName();
//        packageName = apkMeta.getPackageName();
//        minSDK = apkMeta.getMinSdkVersion();
//        targetSDK = apkMeta.getTargetSdkVersion();
//
//    } catch (Exception ex) {
//        ex.printStackTrace();
//    } finally {
//        if(apkFile != null) {
//            apkFile.close();
//        }
//    }


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

