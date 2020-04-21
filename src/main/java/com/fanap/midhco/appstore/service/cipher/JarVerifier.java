package com.fanap.midhco.appstore.service.cipher;

import com.fanap.midhco.appstore.applicationUtils.JarUtil;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by admin123 on 7/23/2016.
 */
public class JarVerifier {
    static Logger logger = Logger.getLogger(JarVerifier.class);

    public static class UnSignedEntryFoundInJarFile extends SecurityException {
        public UnSignedEntryFoundInJarFile(String message) {
            super(message);
        }
    }

    public static class NotSignedByTrustedSignerException extends SecurityException {
        public NotSignedByTrustedSignerException(String message) {
            super(message);
        }
    }

    public static class UnsignedPackageException extends SecurityException {
        public UnsignedPackageException(String message) {
            super(message);
        }
    }


    private URL jarURL = null;
    private JarFile jarFile = null;

    JarVerifier(URL jarURL) {
        this.jarURL = jarURL;
    }

    /**
     * First, retrieve the jar file from the URL passed in constructor.
     * Then, compare it to the expected X509Certificate.
     * If everything went well and the certificates are the same, no
     * exception is thrown.
     */
    public void verify(X509Certificate targetCert) throws IOException {

        try {
            if (jarFile == null) {
                jarFile = JarUtil.retrieveJarFileFromURL(jarURL);
            }
        } catch (Exception ex) {
            logger.error("error verifying jar file ", ex);
            SecurityException se = new SecurityException();
            se.initCause(ex);
            throw se;
        }

        Vector<JarEntry> entriesVec = new Vector<JarEntry>();

        // Ensure the jar file is signed.
        Manifest man = jarFile.getManifest();
        if (man == null) {
            throw new UnsignedPackageException("The provider is not signed");
        }

        // Ensure all the entries' signatures verify correctly
        byte[] buffer = new byte[8192];
        Enumeration entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry je = (JarEntry) entries.nextElement();

            // Skip directories.
            if (je.isDirectory()) continue;
            entriesVec.addElement(je);
            InputStream is = jarFile.getInputStream(je);

            // Read in each jar entry. A security exception will
            // be thrown if a signature/digest check fails.
            int n;
            while ((n = is.read(buffer, 0, buffer.length)) != -1) {
                // Don't care
            }
            is.close();
        }

        // Get the list of signer certificates
        Enumeration e = entriesVec.elements();

        while (e.hasMoreElements()) {
            JarEntry je = (JarEntry) e.nextElement();

            // Every file must be signed except files in META-INF.
            Certificate[] certs = je.getCertificates();
            if ((certs == null) || (certs.length == 0)) {
                if (!je.getName().startsWith("META-INF"))
                    throw new UnSignedEntryFoundInJarFile("The provider has unsigned class files.");
            } else if(targetCert != null) {
                // Check whether the file is signed by the expected
                // signer. The jar may be signed by multiple signers.
                // See if one of the signers is 'targetCert'.
                int startIndex = 0;
                X509Certificate[] certChain;
                boolean signedAsExpected = false;

                while ((certChain = getAChain(certs, startIndex)) != null) {
                    if (certChain[0].equals(targetCert)) {
                        // Stop since one trusted signer is found.
                        signedAsExpected = true;
                        break;
                    }
                    // Proceed to the next chain.
                    startIndex += certChain.length;
                }

                if (!signedAsExpected) {
                    throw new NotSignedByTrustedSignerException("The provider is not signed by a trusted signer");
                }
            }
        }
    }

    /**
     * Extracts ONE certificate chain from the specified certificate array
     * which may contain multiple certificate chains, starting from index
     * 'startIndex'.
     */
    private static X509Certificate[] getAChain(Certificate[] certs,
                                               int startIndex) {
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

    // Close the jar file once this object is no longer needed.
    protected void finalize() throws Throwable {
        jarFile.close();
    }

    public static void main(String[] args) throws Exception {
        try(JarFile jarFile1 = JarUtil.retrieveJarFileFromURL(new URL("file:\\D:\\android-release-v1.apk"));
            JarFile jarFile2 = JarUtil.retrieveJarFileFromURL(new URL("file:\\D:\\android-release-v2.apk"))) {

            Manifest firstManifest = jarFile1.getManifest();
            Map<String, Attributes> firstManifestEntries = firstManifest.getEntries();

            Manifest secondManifest = jarFile2.getManifest();
            Map<String, Attributes> secondManifestEntries = secondManifest.getEntries();

            List<String> differKeys = secondManifestEntries.keySet().stream().filter(key -> {
                Attributes attributesInFirstJar = firstManifestEntries.get(key);
                if(attributesInFirstJar == null) {
                    return true;
                } else {
                    String hashValueInFirst = attributesInFirstJar.getValue("SHA1-Digest");
                    String hashValueInSecond = secondManifestEntries.get(key).getValue("SHA1-Digest");

                    if(!hashValueInFirst.equals(hashValueInSecond)) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toCollection(ArrayList<String>::new));


            differKeys.add("META-INF/CERT.RSA");
            differKeys.add("META-INF/CERT.SF");
            differKeys.add("META-INF/MANIFEST.MF");

            ZipOutputStream zipOutputStream = new ZipOutputStream(
                    new FileOutputStream("D:\\out.apk"));
            for(String differKey : differKeys) {
                ZipEntry zipEntry = jarFile2.getEntry(differKey);
                InputStream inStream = jarFile2.getInputStream(zipEntry);

                zipOutputStream.putNextEntry(new ZipEntry(differKey));
                int count;
                byte[] bt = new byte[100];
                while((count = inStream.read(bt, 0, 100)) != -1) {
                    zipOutputStream.write(bt, 0, count);
                }
            }
            zipOutputStream.close();

        }


//        String apkFileName = "D:/hamid/Bazaar.apk";
//        InputStream is;
//
//        ZipFile zip = new ZipFile(apkFileName);
//
//        try {
//            ZipEntry certRSAFile = zip.getEntry("META-INF/CERT.RSA");
//            if (certRSAFile == null) {
//                throw new AppBundleNotSignedException("META-INF/CERT.RSA file not found!");
//            }
//
//            is = zip.getInputStream(certRSAFile);
//
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            Collection c = cf.generateCertificates(is);
//
//            if (c.size() > 1)
//                throw new AppBundleSignNotValidException("more than one certificate found in app sign file");
//
//            Iterator i = c.iterator();
//            if (i.hasNext()) {
//                X509CertImpl cert = (X509CertImpl) i.next();
//                JarVerifier jarVerifier = new JarVerifier(new URL("file:\\" + apkFileName));
//                jarVerifier.verify(cert);
//            } else {
//                throw new AppBundleSignNotValidException("no certificate found in app sign file!");
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            if (zip != null)
//                zip.close();
//        }


//        FileInputStream fis = new FileInputStream("D:/temp.jks");
//        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//        Collection c = cf.generateCertificates(fis);
//        Iterator i = c.iterator();
//
//        X509CertImpl cert = null;
//        while (i.hasNext()) {
//            cert = (X509CertImpl)i.next();
//        }

//        if(cert != null) {
//
//            System.out.println("Verified!");
//        }

    }
}