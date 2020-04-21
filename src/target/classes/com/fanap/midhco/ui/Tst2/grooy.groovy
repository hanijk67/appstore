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
import sun.security.x509.X509CertImpl

import java.security.cert.Certificate
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Created by admin123 on 8/7/2016.
 */

def static IAPPPackageService parse(String fileName) throws IOException {
    InputStream is = null;
    ZipFile zip = null;

    zip = new ZipFile(fileName);
    ZipEntry mft = zip.getEntry("Manifest.xml");
    is = zip.getInputStream(mft);

    byte[] buf = new byte[is.available()];
    int bytesRead = is.read(buf);

    is.close();
    if (zip != null) {
        zip.close();
    }

    String xml = new String(buf);

    def manifest = new XmlSlurper().parseText(xml);
    String versionCode = manifest.versionCode;
    String versionName = manifest.versionName;
    String packageName = manifest.package;
    String minSDK = manifest.minSdkVersion;
    String targetSDK = manifest.targetSdkVersion;

    return new IAPPPackageService() {
        @Override
        String getVersionCode() {
            versionCode;
        }

        @Override
        String getVersionName() {
            versionName
        }

        @Override
        String getPackage() {
            packageName
        }

        @Override
        String getMinSDK() {
            minSDK
        }

        @Override
        String getTargetSDK() {
            targetSDK
        }

        @Override
        List<String> getPermissions() {
            return null;
        }

        @Override
        Certificate verifyPackage(Certificate previousCertficate) throws BaseAppBundleException {
            try {
                zip = new ZipFile(fileName);
                ZipEntry certRSAFile = zip.getEntry("META-INF/BLOCKCHA.RSA");
                if (certRSAFile == null) {
                    throw new AppBundleNotSignedException("META-INF/BLOCKCHA.RSA file not found!");
                }

                java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
                is = zip.getInputStream(certRSAFile);
                Collection c = cf.generateCertificates(is);

                if (c.size() > 1)
                    throw new AppBundleSignNotValidException("more than one certificate found in app sign file");

                Iterator i = c.iterator();
                if (i.hasNext()) {
                    X509CertImpl cert = (X509CertImpl) i.next();
                    JarVerifier jarVerifier = new JarVerifier(new URL("file:" + fileName));
                    jarVerifier.verify(previousCertficate);
                    return cert;
                } else {
                    throw new AppBundleSignNotValidException("no certificate found in app sign file!");
                }
            } catch (Exception ex) {
                if(ex instanceof JarVerifier.UnSignedEntryFoundInJarFile) {
                    throw new AppBundleSignNotValidException(ex);
                } else if(ex instanceof JarVerifier.NotSignedByTrustedSignerException) {
                    throw new AppStoreNoSimilarCertificateFoundException(ex);
                } else if(ex instanceof JarVerifier.UnsignedPackageException) {
                    throw new AppBundleNotSignedException(ex);
                } else if(ex instanceof BaseAppBundleException) {
                    throw ex;
                } else if(ex instanceof SecurityException) {
                    throw new AppBundleSignNotValidException(ex);
                }
            } finally {
                if(zip != null)
                    zip.close();
            }
        }
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

def static IUploadFilter getUploadFilter() {
    return new IUploadFilter() {
        @Override
        String getFilterTitle() {
            return "BLOCKCHA files";
        }

        @Override
        List<String> getFilterList() {
            return Arrays.asList("BLOCKCHA");
        }
    }
}

def static boolean IsDeltaUpdatable() {
    return true;
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

        differKeys.add("META-INF/BLOCKCHA.RSA");
        differKeys.add("META-INF/BLOCKCHA.SF");
        differKeys.add("META-INF/BLOCKCHA.MF");

        String temp = System.getProperty("java.io.tmpdir");
        if (!temp.endsWith(File.separator))
            temp = temp + File.separator;
        println "--------------------------------->>>>" + temp;
        String tempOutZip = temp + DateTime.now().dateTimeLong +
                "_" + AppUtils.generateRandomString(5) + ".sag";

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
        println "--------------fileName is "+ file.getName() + "------------------------" + mp.get(file.getName()) + "------file size is " + file.size();
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

public static void main(String[] args) {
    parse("D:\\Program Files\\Apache Software Foundation\\Tomcat 8.0\\temp/C567813CB1D7AD82FE1D473A7846ADDC1.5722169469918906E14.BLOCKCHAIN");
}