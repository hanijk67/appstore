package com.fanap.midhco.appstore.wicketApp;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;

import java.io.InputStream;
import java.util.Properties;

public class AppStorePropertyReader {
    static final Properties PROPERTIES = new Properties();

    private AppStorePropertyReader() {
    }

    public static void load(InputStream stream) {
        PROPERTIES.clear();

        try {
            PROPERTIES.load(stream);
            stream.close();
            if(PROPERTIES.size() == 0) {
                throw new RuntimeException("Empty properties file!");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Can\'t load properties file!", ex);
        }
    }

    static {
        String lang = ConfigUtil.getProperty(ConfigUtil.APP_DEFAULT_LOCALE_LANG);
        load(AppStoreApplication.class.getClassLoader().getResourceAsStream(
                "/com/fanap/midhco/appstore/wicketApp/AppStoreApplication_" + lang + ".properties"));
    }

    public static String getString(String propertyKey) {
        return PROPERTIES.getProperty(propertyKey);
    }

}
