import com.fanap.midhco.appstore.service.app.IAPPPackageService
import com.fanap.midhco.appstore.service.myException.appBundle.BaseAppBundleException
import com.fanap.midhco.appstore.service.osType.IUploadFilter
import org.apache.xerces.impl.dv.util.Base64
import org.json.JSONObject

import java.security.KeyStore
import java.security.cert.Certificate
import java.util.zip.ZipInputStream

def static IUploadFilter getUploadFilter() {
    return new IUploadFilter() {
        @Override
        String getFilterTitle() {
            return "plugin js files";
        }

        @Override
        List<String> getFilterList() {
            return Arrays.asList("js");
        }
    }
}

def static IAPPPackageService parse(String fileName) throws IOException, Exception {
    //def downloadedFileName = downloadFile(fileName);

    String fileContents = new File(fileName).text;
    JSONObject jsonObject = new JSONObject(fileContents);
    Integer versionCode = jsonObject.getInt("version");
    String packageName = jsonObject.getString("package");
    String certificateString = jsonObject.getString("certificate");

    return new IAPPPackageService() {
        @Override
        String getVersionCode() {
            versionCode;
        }

        @Override
        String getVersionName() {
            return versionCode;
        }

        @Override
        String getPackage() {
            return packageName;
        }

        @Override
        String getMinSDK() {
            "";
        }

        @Override
        String getTargetSDK() {
            "";
        }

        @Override
        Certificate verifyPackage(Certificate previousCertificate) throws BaseAppBundleException {
            KeyStore keyStoreLoadedFrom = KeyStore.getInstance("JKS");
            byte[] decodedCertificate = Base64.decode(certificateString)

            ByteArrayInputStream bin = new ByteArrayInputStream(decodedCertificate);

            keyStoreLoadedFrom.load(bin, null);
            return keyStoreLoadedFrom.getCertificate("pluginCertificate");
        }

        @Override
        List<String> getPermissions() {
            Arrays.asList();
        }
    }
}

def static boolean IsDeltaUpdatable() {
    return true;
}

def static boolean checkFileExistenceInPackage(ZipInputStream zipIn, String fileName, Boolean wholePath) throws Exception {
    return false;
}

def static String getDeltaPackage(URL prevURL, URL currentURL) {
    return "";
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

def static String downloadFile(String sourceUrl) {
    def stagingDir = System.getProperty("java.io.tmpdir");
    new File(stagingDir).mkdirs()
    def net_fileName = new URL(sourceUrl).openConnection().getHeaderFields().get("filename")[0];
    def fileName = "$stagingDir" + net_fileName;
    def file = new FileOutputStream(fileName)
    def out = new BufferedOutputStream(file)
    out << new URL(sourceUrl).openStream()
    out.close()
    return fileName
}