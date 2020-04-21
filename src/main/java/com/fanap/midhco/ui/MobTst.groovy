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
import org.json.JSONObject

import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

def static IUploadFilter getUploadFilter() {
    return new IUploadFilter() {
        @Override
        String getFilterTitle() {
            return "MOB files";
        }

        @Override
        List<String> getFilterList() {
            return Arrays.asList("mob");
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
    String certificateAsString = "";

    InputStream fin = null;
    try {
        fin = new FileInputStream(fileName);
        byte[] buffer = new byte[(int) fin.available()];

        if (fin.read(buffer) == -1) {
            throw new RuntimeException("error occred reading file input stream!");
        }

        JSONObject jsonContent = new JSONObject(new String(buffer));

        versionCode = jsonContent.getString("versionCode");
        versionName = jsonContent.getString("versionName");
        packageName = jsonContent.getString("packageName");
        minSDK = jsonContent.getString("minSDK");
        targetSDK = jsonContent.getString("targetSDK");
        certificateAsString = jsonContent.getString("certificate")

    } catch (Exception ex) {
        throw ex;
    } finally {
        if(fin != null) {
            fin.close();
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
            try {
                certificateAsString = certificateAsString
                    .replace("-----BEGIN CERTIFICATE-----\n", "")
                    .replace("-----END CERTIFICATE-----", "");
                byte[] certificateData = Base64.getDecoder().decode(certificateAsString.trim());
                CertificateFactory cf = CertificateFactory.getInstance("X509");
                X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));

                if(previousCertficate == null)
                    return certificate;

                if(certificate.equals(previousCertficate))
                    return certificate;

                throw new AppStoreNoSimilarCertificateFoundException("certificates are different!");
            } catch (Exception ex) {
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
                throw ex;
            } finally {
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

def static boolean checkFileExistenceInPackage( ZipFile zipFile, String fileName) throws Exception {
    try {

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File file = new File(entry.getName());
            if (file.getName().equals(fileName)) {
                return  true;
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

public static void main(String[] args) {
    IAPPPackageService packageService =
            parse("D:\\Program Files\\Apache Software Foundation\\Tomcat 8.0\\temp/79EFBF7491957143180D09AA263EEFD24.638181835388469E15");

    X509Certificate certificate = null;
    CertificateFactory cf = null;
    String certificateString = "";
    try {
        certificateString = "-----BEGIN CERTIFICATE-----\nMIICMTCCAZqgAwIBAgIEcPXcmDANBgkqhkiG9w0BAQUFADBdMQswCQYDVQQGEwJERTENMAsGA1UECBMEdGVzdDENMAsGA1UEBxMEdGVzdDENMAsGA1UEChMEdGVzdDELMAkGA1UECxMCSVQxFDASBgNVBAMTC3d3dy50ZXN0LmRlMB4XDTE5MDcyMTEyMzgyNloXDTIyMDcyMTEyMzgyNlowXTELMAkGA1UEBhMCREUxDTALBgNVBAgTBHRlc3QxDTALBgNVBAcTBHRlc3QxDTALBgNVBAoTBHRlc3QxCzAJBgNVBAsTAklUMRQwEgYDVQQDEwt3d3cudGVzdC5kZTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEApm6Tjk1916EStXG+h5FI2WcXXk4M5x6XNurA/9rmJm7bDvwBVfaT2vMRBMjxQ/O/sujwdndSat8MqyW0wTcbYJzP0xEfUMmggOnpNBMk5nfFwGV+Dv+JBwb02fXoO50nWwFXgeeEYmYOaKOI0qrb/u+evANdNaiTCWiXwpNSmNECAwEAATANBgkqhkiG9w0BAQUFAAOBgQCBbv9vRF5EEX88GGyPr3OyLAOFVAWDKRzsfGocDHqUUhTxH6qy1Zx6awRqHbkfYDO3SVI8t1EE/xv5BX4moo8nWlsH6HxSFfZLRetsEukfSeTs3NFWvKgSrCaCIcdyaScGlL8aoLNcJtoAIDHuUnUT6Xtw4pwXptPxQcjv/Lf32Q==\n-----END CERTIFICATE-----"
                .replace("-----BEGIN CERTIFICATE-----\n", "")
                .replace("-----END CERTIFICATE-----", ""); // NEED FOR PEM FORMAT CERT STRING
        byte[] certificateData = Base64.getDecoder().decode(certificateString.trim());
        cf = CertificateFactory.getInstance("X509");
        certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));
        packageService.verifyPackage(certificate);
    } catch (CertificateException e) {
        throw new CertificateException(e);
    }

}

