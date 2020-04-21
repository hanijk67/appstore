package com.fanap.midhco.appstore.applicationUtils;

import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.jar.JarFile;

/**
 * Created by admin123 on 7/23/2016.
 */
public class JarUtil {
    public static JarFile retrieveJarFileFromURL(URL url)
            throws PrivilegedActionException, MalformedURLException {
        JarFile jf = null;

        // Prep the url with the appropriate protocol.
        URL jarURL =
                url.getProtocol().equalsIgnoreCase("jar") ?
                        url :
                        new URL("jar:" + (url.toString().startsWith("\\") ? url.toString().substring(1) : url.toString()) + "!/");
        // Retrieve the jar file using JarURLConnection
        jf = AccessController.doPrivileged(
                new PrivilegedExceptionAction<JarFile>() {
                    public JarFile run() throws Exception {
                        JarURLConnection conn =
                                (JarURLConnection) jarURL.openConnection();
                        // Always get a fresh copy, so we don't have to
                        // worry about the stale file handle when the
                        // cached jar is closed by some other application.
                        conn.setUseCaches(false);
                        return conn.getJarFile();
                    }
                });
        return jf;
    }
}
