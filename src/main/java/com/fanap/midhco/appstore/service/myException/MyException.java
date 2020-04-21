package com.fanap.midhco.appstore.service.myException;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Properties;

/**
 * Created by admin123 on 6/18/2016.
 */
public class MyException extends Exception {
    static Logger logger = Logger.getLogger(MyException.class);
    private static final Properties MYEXCEPTION_PROPERTIES = new Properties();
    private static final Properties SWITCH_PROPERTIES = new Properties();
    private Map<String, String> params;
    String message;


    public MyException(String message) {
        super(message);
        this.message = message;
    }

    public MyException(String message, Map<String, String> params) {
        super(message);

        for (String s : params.keySet()) {
            message = message.replace("${" + s + "}", params.get(s));
        }

        this.message = message;

        this.params = params;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setExceptionParams(Map<String, String> params) {
        this.params = params;
    }


    static {
        try {
            SWITCH_PROPERTIES.load(MyException.class.getResourceAsStream("/com/fanap/midhco/appstore/wicketApp/AppStoreApplication_fa.properties"));



        } catch (Exception ex) {
            logger.debug("Error Loading myExceptions property Files!", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyException))
            return false;
        return ((MyException) o).getMessage().equals(this.getMessage());
    }

    public static MyException generateRequiredException(String key) {
        String keyString = SWITCH_PROPERTIES.getProperty(key);
        String required = SWITCH_PROPERTIES.getProperty("Required");
        required = required.replace("${label}", keyString);
        return new MyException(required);
    }

    public static MyException generateConvertedException(String key) {
        String keyString = SWITCH_PROPERTIES.getProperty(key);
        String converter = SWITCH_PROPERTIES.getProperty("IConverter");
        converter = converter.replace("${label}", keyString);
        return new MyException(converter);
    }
}
