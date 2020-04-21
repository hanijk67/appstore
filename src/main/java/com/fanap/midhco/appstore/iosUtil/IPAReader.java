package com.fanap.midhco.appstore.iosUtil;

import com.dd.plist.*;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.service.app.AppPackageService;
import com.fanap.midhco.appstore.service.app.IAPPPackageService;
import com.fanap.midhco.appstore.service.myException.appBundle.AppBundleNotSignedException;
import com.fanap.midhco.appstore.service.myException.appBundle.AppBundleSignNotValidException;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.google.common.io.Resources;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import sun.security.x509.X509CertImpl;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.ParseException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class IPAReader {
    final static Logger logger = Logger.getLogger(IPAReader.class);
    final static String codeSignatureName = ConfigUtil.getProperty(ConfigUtil.IOS_CODE_SIGNATURE_NAME);

    public static String getLastIconFileName(NSDictionary dict, String identifier) {
        NSDictionary primaryIcon = (NSDictionary) dict.get(identifier);
        NSDictionary iconFiles = (NSDictionary) primaryIcon.get("CFBundlePrimaryIcon");
        NSObject[] files = ((NSArray) iconFiles.get("CFBundleIconFiles")).getArray();

        String name = null;

        for (NSObject file : files) {
            name = file.toString();
        }

        return name;
    }

    public static IPAInfo parse(Certificate mainPackageCertificate, Map<String, String> hashedDataMap, String ipaPath) throws IOException, PropertyListFormatException, ParseException, ParserConfigurationException, SAXException, NoSuchAlgorithmException, JAXBException, AppPackageService.APPPackageException {
        String tempLocation = System.getProperty("java.io.tmpdir");
        String fileName = ipaPath.substring(ipaPath.lastIndexOf("\\") + 1, ipaPath.length());
        String tempFileName = tempLocation + fileName;
        FileOutputStream out = new FileOutputStream(tempFileName);
        InputStream input = new FileInputStream(ipaPath);
        IOUtils.copy(input, out);
        out.flush();
        out.close();
        IPAInfo ipaInfo = getIpaInfoFromIpaPath(mainPackageCertificate, hashedDataMap, ipaPath);
//        IPAInfo ipaInfo = getIpaInfoFromIpaPath(mainPackageCertificate, hashedDataMap, tempFileName);
        return ipaInfo;
    }

    public static IPAInfo getIpaInfoFromIpaPath(Certificate mainPackageCertificate, Map<String, String> hashedDataMap, String fileName) throws IOException, PropertyListFormatException, ParseException, ParserConfigurationException, SAXException, NoSuchAlgorithmException {
        IPAInfo info = null;
        Set<String> checkedFiles = new HashSet<>();
        if (mainPackageCertificate != null) {
            info = new IPAInfo();

            File f = new File(fileName);
            info.setFileSize(f.length());
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(fileName));
            ZipEntry entry = zipIn.getNextEntry();
            if (entry != null && entry.getName() != null && !entry.getName().equals("Payload/")) {
                entry = zipIn.getNextEntry();
            }
            String basePath;
            StringBuilder hashMapKey = new StringBuilder();
            StringBuilder hashMapValue = new StringBuilder();
            String ipaName = null;
            if (entry != null) {
                ZipEntry secondEntry = zipIn.getNextEntry();

                basePath = (secondEntry != null) ? secondEntry.getName() : null;
                ipaName = secondEntry.getName().substring(entry.getName().length(), secondEntry.getName().lastIndexOf("."));
                zipIn = new ZipInputStream(new FileInputStream(fileName));
                entry = zipIn.getNextEntry();
                while((!entry.getName().toString().contains("/") )|| ( entry.getName().toString().contains("/") && entry.getName().toString().lastIndexOf("/")==entry.getName().length()-1) ){
                    entry=zipIn.getNextEntry();
                }
            } else {
                basePath = null;
            }

            if (basePath != null) {

                while (entry != null) {
                    int basePathIndex = basePath.length();
                    hashMapKey.setLength(0);
                    hashMapValue.setLength(0);

                    ByteArrayOutputStream stream = null;
                    if (entry.getName().length() >= basePathIndex) {

                        hashMapKey.append(entry.getName().substring(basePathIndex, entry.getName().length()));
                        if (hashMapKey!=null && !hashMapKey.toString().trim().equals("")) {
                        if (hashedDataMap.containsKey(hashMapKey.toString())) {
                            hashMapValue.append(hashedDataMap.get(hashMapKey.toString()));
                            stream = readFileToMemory(zipIn);
                            String sigAlgName = ((X509CertImpl) mainPackageCertificate).getSigAlgName();

                            if (!hashMapValue.toString().trim().equals("null")) {
                                boolean correctHash = checkHash(mainPackageCertificate.getPublicKey(), sigAlgName, stream.toByteArray(), hashMapValue.toString());
                                if (!correctHash) {
                                    info.setHasCorrectHashValue(false);
                                    return info;
                                }
                            } else {
                                info.setHasCorrectHashValue(false);
                                return info;
                            }
                            checkedFiles.add(entry.getName().substring(basePathIndex , entry.getName().length()));
                        } else if (isEntryWithHashInCodeSignature(entry, ipaName)) {
                            info.setHasCorrectHashValue(false);
                            return info;
                        }
                    }
                    }
                    if (entry.getName().endsWith(".app/embedded.mobileprovision")) {
                        try {
                            Map<String, Object> plistXmlMap = new HashMap<>();
                            String plist = getPlistFromMobileProvisionFile(stream);

                            if (plist != null) {

                                plistXmlMap = getMapDataFromXml(plist);
                                info.setProvisioningProfileCreationDate(plistXmlMap.get("CreationDate").toString());
                                info.setProvisioningProfileExpirationDate(plistXmlMap.get("ExpirationDate").toString());

                                info.setProvisioningProfileName(plistXmlMap.get("Name").toString());
                                info.setAppIDName(plistXmlMap.get("AppIDName").toString());
                                StringBuilder teamIdentifier = new StringBuilder();
                                List<String> teamIdentifierList = (List<String>) plistXmlMap.get("TeamIdentifier");
                                for (String str : teamIdentifierList) {
                                    teamIdentifier.append(str);
                                }

                                info.setTeamIdentifier(teamIdentifier.toString());
                                info.setTeamName(plistXmlMap.get("TeamName").toString());

                                info.setVersion(Integer.valueOf(plistXmlMap.get("Version").toString()));
                                Map<String, String> permissionMap = (Map<String, String>) plistXmlMap.get("permissionsMap");
                                info.setPermissionsMap(permissionMap);
                                List<String> permissions = (List<String>) plistXmlMap.get("permissions");
                                info.setPermissions(permissions);
                                if (plistXmlMap.get("ProvisionedDevices") != null) {
                                    List<String> deviceList = (List<String>) plistXmlMap.get("ProvisionedDevices");
                                    info.setProvisioningProfileDevices(deviceList);
                                }
                            }
                        } catch (Exception e) {
                        }

                    } else if (entry.getName().endsWith(".app/Info.plist")) {
                        try {
                            info.setInfoPlistFile(stream.toByteArray());
                            NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(info.getInfoPlistFile());
                            info.setMinimumOSVersion(rootDict.get("MinimumOSVersion").toString());
                            info.setBundleName(rootDict.get("CFBundleName").toString());
                            info.setBundleVersionString(rootDict.get("CFBundleShortVersionString").toString());
                            String packageName = rootDict.get("CFBundleIdentifier").toString();
                            if(packageName.contains("watchkitapp")){
                                packageName = packageName.substring(0,packageName.lastIndexOf("."));
                            }
                            info.setBundleIdentifier(packageName);
                            info.setBuildNumber(rootDict.get("CFBundleVersion").toString());
                            info.setPlatformVersion(rootDict.get("DTPlatformVersion").toString());
                            if (rootDict.containsKey("UIRequiredDeviceCapabilities")) {
                                NSObject[] o = ((NSArray) rootDict.get("UIRequiredDeviceCapabilities")).getArray();

                                if (o.length > 0) {
                                    info.setRequiredDeviceCapabilities(o[0].toString());
                                }
                            }
                            if (rootDict.containsKey("CFBundleIcons")) {
                                info.setiPhoneSupport(true);
                                info.setBundleIconFileName(getLastIconFileName(rootDict, "CFBundleIcons"));
                            } else {
                                info.setiPhoneSupport(false);
                            }
                            if (rootDict.containsKey("CFBundleIcons~ipad")) {
                                info.setiPadSupport(true);

                                info.setBundleIconFileName(getLastIconFileName(rootDict, "CFBundleIcons~ipad"));
                            } else {
                                info.setiPadSupport(false);
                            }
                        } catch (Exception e) {
                            logger.debug("info = " + info);
                        }
                    }
                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                }
            }

            zipIn.close();

        }
        for (Map.Entry<String, String> entry : hashedDataMap.entrySet())
        {
            if(!checkedFiles.contains(entry.getKey())){
                info.setHasCorrectHashValue(false);
                return info;
            }
        }


        info.setHasCorrectHashValue(true);
        return info;
    }

    private static boolean isEntryWithHashInCodeSignature(ZipEntry entry, String ipaName) {
        String entryName = entry.getName();
        String fileName = entryName.substring(entryName.lastIndexOf("/") + 1, entryName.length());
        return entry.isDirectory() ? false : !(entryName.endsWith(codeSignatureName) || fileName.trim().equals(ipaName));
    }

    private static Map<String, Object> getMapDataFromXml(String xmlData) {
        Stack<String> plistXmlStack = new Stack<String>();
        String[] xmlLinesArray = xmlData.toString().split("\n");
        List<String> xmlLines = Arrays.asList(xmlLinesArray);
        Collections.reverse(xmlLines);
        Map<String, Object> xmlParserMap = new HashMap<>();
        Map<String, Object> permissionsMap = new HashMap<>();
        List<String> permissions = new ArrayList<>();
        for (String lineStr : xmlLines) {
            lineStr = lineStr.replaceAll("\\t\\t", "");
            lineStr = lineStr.replaceAll("\\t", "");
            plistXmlStack.push(lineStr);
        }
        String iteratorXmlStr = plistXmlStack.pop();
        StringBuilder keyText = new StringBuilder();
        Stack dictStack = new Stack();

        while (iteratorXmlStr != null && !iteratorXmlStr.trim().equals("") && plistXmlStack.size() > 0) {
            String iterator = "";
            iterator = plistXmlStack.pop();
            if (iterator != null && iterator.trim().equals("<dict>")) {
                dictStack.push("<dict>");
                iterator = plistXmlStack.pop();
                getPlistMapFromXml(xmlParserMap, plistXmlStack, keyText, iterator, dictStack, permissionsMap, permissions);
            }
            if (plistXmlStack != null && plistXmlStack.size() > 0) {
                iteratorXmlStr = plistXmlStack.pop();
            }
        }
        xmlParserMap.put("permissionsMap", permissionsMap);
        return xmlParserMap;
    }

    private static void getPlistMapFromXml(Map<String, Object> dataMap, Stack<String> plistXmlStack, StringBuilder keyText,
                                           String iterator, Stack dictStack, Map<String, Object> permissionMap, List<String> permissions) {
        while (dictStack != null && dictStack.size() != 0) {
            String keyStr = (keyText != null && !keyText.toString().equals("") ? keyText.toString() : "");
            if (iterator.startsWith("<key>")) {
                iterator = iterator.substring(5, iterator.length() - 6);
                if (iterator.startsWith("Privacy")) {
                    iterator = iterator.substring(10, iterator.length());
                    keyText.setLength(0);
                    keyText.append(iterator);
                    String permission = plistXmlStack.pop();
                    permissionMap.put(keyStr.toString(), permission);
                    permissions.add(keyText.toString());
                } else {
                    keyText.setLength(0);
                    keyText.append(iterator);
                }

            } else if (iterator.startsWith("<dict>")) {
                Map<String, Object> secondLevelMap = new HashMap<>();
                String secondLevelKeyStr = "";
                dictStack.push(iterator);
                iterator = plistXmlStack.pop();

                while (iterator != null && !iterator.equals("</dict>")) {
                    if (!iterator.trim().equals("")) {
                    if (iterator.startsWith("<key>")) {
                        iterator = iterator.substring(5, iterator.length() - 6);
                        secondLevelKeyStr = iterator;
                    } else if (iterator.startsWith("<array>")) {
                        List<String> secondLevelArrayStr = new ArrayList<>();
                        iterator = plistXmlStack.pop();
                        while (iterator != null && !iterator.equals("</array>")) {
                            int secondLevelValueTagNameSizeInArray = iterator.length() - iterator.lastIndexOf("/");
                            String strInArray = iterator.substring(secondLevelValueTagNameSizeInArray, iterator.length() - secondLevelValueTagNameSizeInArray - 1);
                            secondLevelArrayStr.add(strInArray);
                            iterator = plistXmlStack.pop();
                        }
                        secondLevelMap.put(secondLevelKeyStr, secondLevelArrayStr);
                    } else {
                        if (iterator.lastIndexOf("<") == 0) {
                            iterator = iterator.substring(1, iterator.length() - 2);
                        } else {
                            int secondLevelValueTagNameSize = iterator.length() - iterator.lastIndexOf("/");
                            iterator = iterator.substring(secondLevelValueTagNameSize, iterator.length() - secondLevelValueTagNameSize - 1);
                        }
                        secondLevelMap.put(secondLevelKeyStr, iterator);
                    }
                    }
                    iterator = plistXmlStack.pop();
                }
                dataMap.put(keyStr, secondLevelMap);
                dictStack.pop();
            } else if (iterator.startsWith("</dict>")) {
                dictStack.pop();
            } else {
                if (iterator.equals("<array>")) {
                    List<String> arrayStr = new ArrayList<>();
                    iterator = plistXmlStack.pop();
                    while (iterator != null && !iterator.equals("</array>")) {
                        int valueTagNameSizeInArray = iterator.length() - iterator.lastIndexOf("/");
                        String strInArray = iterator.substring(valueTagNameSizeInArray, iterator.length() - valueTagNameSizeInArray - 1);
                        arrayStr.add(strInArray);
                        iterator = plistXmlStack.pop();
                    }
                    dataMap.put(keyStr, arrayStr);
                } else {
                    if (iterator.lastIndexOf("<") == 0) {
                        iterator = iterator.substring(1, iterator.length() - 2);
                    } else {
                        int secondLevelValueTagNameSize = iterator.length() - iterator.lastIndexOf("/");
                        iterator = iterator.substring(secondLevelValueTagNameSize, iterator.length() - secondLevelValueTagNameSize - 1);
                    }
                    dataMap.put(keyStr, iterator);

                }
            }
            if (plistXmlStack != null && plistXmlStack.size() > 0) {
                iterator = plistXmlStack.pop();
            }
        }

    }

    public static Certificate getCertificateAndFileKeys(Map<String, String> hashedDataMap, String fileName) throws IOException, PropertyListFormatException, ParseException, ParserConfigurationException, SAXException, JAXBException, CertificateException {
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(fileName));
        ZipEntry entry = zipIn.getNextEntry();
        Certificate mainCertificate = null;

        while (entry != null) {

            if (entry.getName().endsWith(".app/_CodeSignature/CodeResources")) {
                ByteArrayOutputStream stream = readFileToMemory(zipIn);
                String xmlString = new String(stream.toByteArray());
                getHashMapFromXmlString(xmlString, hashedDataMap);

            } else if (entry.getName().endsWith(".app/embedded.mobileprovision")) {
                try {
                    Map<String, Object> plistXmlMap = new HashMap<>();
                    ByteArrayOutputStream stream = readFileToMemory(zipIn);
                    String plist = getPlistFromMobileProvisionFile(stream);
                    plistXmlMap = getMapDataFromXml(plist);
                    List<String> certificateStrList = (List<String>) plistXmlMap.get("DeveloperCertificates");
                    StringBuilder data = new StringBuilder();
                    for (String str : certificateStrList) {
                        data.append(str);
                    }

                    if (certificateStrList != null && !certificateStrList.isEmpty()) {
                        byte[] crtBytes = org.apache.xmlbeans.impl.util.Base64.decode(data.toString().getBytes());
                        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                        ByteArrayInputStream bin = new ByteArrayInputStream(crtBytes);
                        Collection<? extends Certificate> certificates = cf.generateCertificates(bin);
                        Iterator<Certificate> iterator = (Iterator<Certificate>) certificates.iterator();
                        if (iterator.hasNext()) {
                            mainCertificate = iterator.next();
                        }
                        return mainCertificate;
                    } else {
                        return null;
                    }

                } catch (Exception e) {
                    throw e;
                }
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();

        return null;
    }

    private static void getHashMapFromXmlString(String xmlString, Map<String, String> hashedDataMap) {
        String[] lines = xmlString.split("\n");
        Stack<String> xmlStack = new Stack<String>();

        ArrayList<String> xmlLines = new ArrayList<>(Arrays.asList(lines));
        Collections.reverse(xmlLines);
        for (String lineStr : xmlLines) {
            lineStr = lineStr.replaceFirst("\\t\\t", "");
            lineStr = lineStr.replaceFirst("\\t", "");
            xmlStack.push(lineStr);
        }
        String iteratorXmlStr = xmlStack.pop();
        StringBuilder keyText = new StringBuilder();
        Stack dictStack = new Stack();

        while (iteratorXmlStr != null && !iteratorXmlStr.trim().equals("") && xmlStack.size() > 0) {
            String iterator = "";
            iterator = xmlStack.pop();
            if (iterator != null && iterator.trim().equals("<dict>")) {
                dictStack.push("<dict>");
                iterator = xmlStack.pop();
                getMapFromXml(hashedDataMap, xmlStack, keyText, iterator, dictStack);
            }
            if (xmlStack != null && xmlStack.size() > 0) {
                iteratorXmlStr = xmlStack.pop();
            }
        }
    }

    private static void getMapFromXml(Map<String, String> hashedDataMap, Stack<String> xmlStack, StringBuilder keyText, String iterator, Stack dictStack) {
        while (dictStack != null && dictStack.size() != 0) {
            String keyStr = (keyText != null && !keyText.toString().equals("") ? keyText.toString() : "");
            if (iterator != null && !iterator.trim().equals("")) {
                if (iterator.startsWith("<key>")) {
                    iterator = iterator.substring(5, iterator.length() - 6);
                    if (!iterator.equals("true") && !iterator.equals("files") && !iterator.equals("rules") && !iterator.startsWith("^") && !iterator.equals("hash") && !iterator.equals("hash2")) {
                        keyStr = iterator;
                        keyText.setLength(0);
                        keyText.append(keyStr);
                    }
                } else if (iterator.startsWith("<data>")) {
                    iterator = xmlStack.pop();
                    if (hashedDataMap.get(keyStr) == null) {
                        hashedDataMap.put(keyStr, iterator);
                    }

                } else if (iterator.startsWith("<dict>")) {
                    dictStack.push(iterator);
                    iterator = xmlStack.pop();
                    getMapFromXml(hashedDataMap, xmlStack, keyText, iterator, dictStack);
                } else if (iterator.startsWith("</dict>")) {
                    dictStack.pop();
                }
                if (xmlStack != null && xmlStack.size() > 0) {
                    iterator = xmlStack.pop();
                }

            }
        }
    }

    public static String getPlistFromMobileProvisionFile(ByteArrayOutputStream stream) throws UnsupportedEncodingException {
        String s = stream.toString("UTF-8");
        int i = s.indexOf("<plist version=\"1.0\">");

        if (i >= 0) {
            s = s.substring(i);

            i = s.indexOf("</plist>");

            if (i >= 0) {
                String plist = s.substring(0, i + "</plist>".length());
                return plist;
            }
        }
        return null;
    }

    public static ByteArrayOutputStream readFileToMemory(ZipInputStream zipIn) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
        return bos;
    }

    private static byte[] readBytesFromFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }

    private static boolean checkHash(PublicKey certificatePubKey, String sigAlgName, byte[] fileArray, String hashValue) throws NoSuchAlgorithmException {

        //todo how to use certificatePubKey for signedApp ???
//        byte[] fileArray = readBytesFromFile(filePath);
        MessageDigest createHash = MessageDigest.getInstance("SHA1");
        byte[] hashedData = createHash.digest(fileArray);
        byte[] encryptedHashedData = java.util.Base64.getEncoder().encode(hashedData);
        String encryptedHashedDataSting = new String(encryptedHashedData);
        return (encryptedHashedDataSting.equals(hashValue)) ? true : false;
    }


    public static IAPPPackageService parsIos(String fileName) {

        String versionCode = "";
        String minimumOSVersion = "";
        String appIDName = "";
        boolean iPadSupport = false;
        boolean hasCorrectHashValue = false;
        boolean iPhoneSupport = false;
        List<String> usesPermissionList = new ArrayList<>();
        String platFormVersion = "";

        Certificate certificate = null;

        IPAInfo ipaInfo = null;
        try {
            ipaInfo = parseIosPackage(fileName);
            versionCode = ipaInfo.getVersion().toString();
            minimumOSVersion = ipaInfo.getMinimumOSVersion();
            appIDName = ipaInfo.getAppIDName();
            usesPermissionList = ipaInfo.getPermissions();
            certificate = ipaInfo.getCertificate();
            platFormVersion = ipaInfo.getPlatformVersion();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        String finalVersionCode = versionCode;
        String finalMinimumOSVersion = minimumOSVersion;
        String finalAppIDName = appIDName;
        Certificate finalCertificate = certificate;
        List<String> finalUsesPermissionList = usesPermissionList;

        String finalPlatFormVersion = platFormVersion;
        return new IAPPPackageService() {
            @Override
            public String getVersionCode() {
                return finalVersionCode;
            }

            @Override
            public String getVersionName() {
                return null;
            }

            @Override
            public String getPackage() {
                return null;
            }

            @Override
            public String getMinSDK() {
                return finalMinimumOSVersion;
            }

            @Override
            public String getTargetSDK() {
                return finalPlatFormVersion;
            }

            @Override
            public Certificate verifyPackage(Certificate previousCertificate) throws Exception {
                Map<String, String> hashedDataMap = new HashMap<>();
                Certificate ipaCertificate = IPAReader.getCertificateAndFileKeys(hashedDataMap, fileName);
                try {
                    if (ipaCertificate == null) {
                        throw new AppBundleNotSignedException("certificate file not found!");
                    } else if (finalCertificate.getPublicKey() == null) {
                        throw new AppBundleSignNotValidException("no certificate found in app sign file!");
                    } else {
                        X509CertImpl x509Cert = (X509CertImpl) ipaCertificate;
                        X509CertImpl previousX509Cert = (X509CertImpl) previousCertificate;
                        byte[] newCertSignature = x509Cert.getSignature();
                        if (previousCertificate != null) {
                            byte[] previousCertSignature = previousX509Cert.getSignature();
                            if (previousCertSignature == null) {
                                throw new AppBundleSignNotValidException("no previous certificate found in app sign file!");
                            }
                            boolean hasSameSignature = Arrays.equals(newCertSignature, previousCertSignature);
                            if (!hasSameSignature) {
                                throw new AppPackageService.APPPackageException(AppStorePropertyReader.getString("App.certificate.not.valid"));
                            }
                        }
                    }

                    IPAInfo newIpaInfo = IPAReader.getIpaInfoFromIpaPath(finalCertificate, hashedDataMap, fileName);
                    if (!newIpaInfo.getHasCorrectHashValue()) {
                        throw new AppPackageService.APPPackageException(AppStorePropertyReader.getString("App.certificate.not.valid"));
                    }
                    return ipaCertificate;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new Exception(ex);
                }
            }

            @Override
            public List<String> getPermissions() {
                return finalUsesPermissionList;
            }
        };
    }

    private static IPAInfo parseIosPackage(String ipaPath) throws AppPackageService.APPPackageException, SAXException, ParseException, IOException, JAXBException, PropertyListFormatException, ParserConfigurationException, NoSuchAlgorithmException, CertificateException {
        Map<String, String> hashedDataMap = new HashMap<>();

        Certificate mainPackageCertificate = getCertificateAndFileKeys(hashedDataMap, ipaPath);
        if (mainPackageCertificate == null)
            throw new AppPackageService.APPPackageException(AppStorePropertyReader.getString("App.previousApp.has.no.certificate"));

        IPAInfo info = getIpaInfoFromIpaPath(mainPackageCertificate, hashedDataMap, ipaPath);

        if (info == null || !info.getHasCorrectHashValue())
            throw new AppPackageService.APPPackageException(AppStorePropertyReader.getString("App.certificate.not.valid"));
        info.setCertificate(mainPackageCertificate);
        return info;
    }

    public static void main(String[] args) {
        //groovy scripts

        /*
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
            String minimumOSVersion = "";
            String platFormVersion = "";
            List<String> usesPermissionList = new ArrayList<>();
            Certificate certificate = null;

            IPAInfo ipaInfo = null;
            try {
                ipaInfo = parseIosPackage(fileName);
                versionCode = ipaInfo.getVersion();
                minimumOSVersion = ipaInfo.getMinimumOSVersion();
                platFormVersion = ipaInfo.getPlatformVersion();
                usesPermissionList = ipaInfo.getPermissions();
                certificate = ipaInfo.getCertificate();
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
                    return null;
                }

                @Override
                String getPackage() {
                    return null;
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

*/

    }
}
