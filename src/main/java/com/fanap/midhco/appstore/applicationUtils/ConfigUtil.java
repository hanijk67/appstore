package com.fanap.midhco.appstore.applicationUtils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Properties;

/**
 * Created by admin123 on 6/5/2016.
 */
public class ConfigUtil {
    static Logger logger = Logger.getLogger(ConfigUtil.class);

    public static final Key DB_URL = new Key("db.url");
    public static final Key DB_USERNAME = new Key("db.username");
    public static final Key DB_PASSOWRD = new Key("db.password");
    public static final Key DB_SHOWSQL = new Key("db.showsql");
    public static final Key DB_HBM2DDL = new Key("db.hbm2ddl");

    public static final Key ROOT_ROLE_NAME = new Key("root.role.name","root");
    public static final Key DEVELOPER_ROLE_NAME = new Key("developer.role.name","developer");
    public static final Key DEVELOPER_ROLE_ACCESS = new Key("developer.role.access","000000be1d");
    public static final Key ADMIN_FIRST_NAME = new Key("admin.firstName");
    public static final Key ADMIN_LAST_NAME = new Key("admin.lastName");
    public static final Key ADMIN_USER_NAME = new Key("admin.userName");
    public static final Key ADMIN_USERID_IN_SSO = new Key("admin.userId.in.sso");
    public static final Key CATEGORY_ROOT_NAME = new Key("category.root.name","root");
    public static final Key DEFAULT_OS_ENVIRONMENT_NAME = new Key("default.osEnvironment.name","windows");
    public static final Key APP_DISABLE_ACCESS = new Key("app.disable.access", "false");
    public static final Key APP_IS_IN_TEST_MODE = new Key("app.is.in.test.mode", "false");
    public static final Key APP_DEFAULT_LOCALE_LANG = new Key("app.default.locale.lang", "fa");
    public static final Key APP_DEFAULT_LOCALE_COUNTRY = new Key("app.default.locale.country", "IR");
    public static final Key APP_WEB_CONTEXT = new Key("app.web.context", "/appStore/");
    public static final Key APP_MAX_WRONG_LOGIN = new Key("app.max.wrong.login", "3");
    public static final Key APP_IS_IN_DEVELOPMENTMODE = new Key("app.is.in.developmentMode", "false");
    public static final Key APP_LIST_ROWS_PER_PAGE = new Key("app.list.rows.per.page", "10");
    public static final Key TEST_ISSUE_LIST_ROWS_PER_PAGE = new Key("test.issue.list.rows.per.page", "10");
    public static final Key DEVICE_PER_PAGE_IN_DEVICE_LIST = new Key("device.per.page.in.device.list", "9");
    public static final Key ITEM_PER_PAGE = new Key("item.per.page", "10");


    public static final Key APP_DATE_TIME_THRESHHOLD = new Key("app.DateTime.threshold", "10");

    public static final Key APP_APPSTORE_THUMBIMAGE_WIDTH = new Key("app.appStore.thumImage.width", "240");
    public static final Key APP_APPSTORE_THUMBIMAGE_HEIGHT = new Key("app.appStore.thumImage.height", "180");
    public static final Key APP_RESTAPI_BASEPATH = new Key("app.resAPI.basepath", "restAPI/spring/service/");
    public static final Key APP_PACK_DOWNLOAD_PATH = new Key("app.pack.download.path", "restAPI/service/");
    public static final Key APP_CATEGORY_ICON_SIZE = new Key("app.category.icon.size", "1000");
    public static final Key APP_CATEGORY_ICON_MAX_PIXEL = new Key("app.category.icon.max.pixel", "150");
    public static final Key APP_CATEGORY_ICON_MIN_PIXEL = new Key("app.category.icon.min.pixel", "50");
    public static final Key ORG_ICON_MAX_PIXEL = new Key("org.icon.max.pixel", "700");
    public static final Key ORG_ICON_MIN_PIXEL = new Key("org.icon.min.pixel", "100");

    public static final Key FILE_SERVER_OUTGOING_DOWNLOAD_PATH = new Key("file.server.outgoing.download.path");

    public static final Key REQUEST_SERVLET_OSTYPE_HEADER = new Key("request.servlet.osType.Header", "OSTYPE");
    public static final Key REQUEST_SERVLET_JWT_TOKEN_HEADER = new Key("request.servlet.jwtToken.Header", "JWTTOKEN");
    public static final Key REQUEST_SERVLET_SORT_FIELD_HEADER = new Key("request.servlet.sort.field.Header", "sortBy");
    public static final Key REQUEST_SERVLET_SORT_TYPE_ASC_HEADER = new Key("request.servlet.sort.type.asc.Header", "asc");
    public static final Key REQUEST_SERVLET_FROM_INDEX_HEADER = new Key("request.servlet.jwtToken.Header", "fromIndex");
    public static final Key REQUEST_SERVLET_COUNT_INDEX_HEADER = new Key("request.servlet.jwtToken.Header", "countIndex");
    public static final Key REQUEST_SERVLET_GET_RESULT_COUNT_HEADER = new Key("request.servlet.jwtToken.Header", "resultCount");

    public static final Key FILE_SERVER_URL = new Key("file.server.url");

    public static final Key SSO_OAUTH_SERVER_URL = new Key("sso.oAuth.server.url");
    public static final Key POD_SERVER_URL = new Key("pod.server.url");
    public static final Key POD_API_TOKEN = new Key("pod.api.token");
    public static final Key COMPANY_GET_PARSERS = new Key("company.get.parsers");


    public static final Key OAUTH_CLIENT_ID = new Key("oAuth.client.id");
    public static final Key OAUTH_CLIENT_SECRET = new Key("oAuth.client.secret");
    public static final Key OAUTH_CLIENT_API_TOKEN = new Key("oAuth.client.api.token");

    public static final Key OAUTH_PORTAL_LOGIN_REDIRECT_URL = new Key("oAuth.portal.login.redirect.url");

    public static final Key JWT_ISSUER = new Key("JWT_ISSUER", "https://www.fanapium.com/");
    public static final Key APPLICATION_PATH = new Key("application_path");

    public static final Key KEYBOD_DER_FILE_LOCATION = new Key("keybod.der.file.location");

    public static final Key ELASTIC_SEARCH_SERVER_ADDRESS = new Key("elastic.search.server.address");
    public static final Key ELASTIC_SEARCH_SERVER_MAX_CONNECTIONS_PER_ROUTE = new Key("elastic.search.server.max.connections.per.route");
    public static final Key ELASTIC_SEARCH_SERVER_MAX_TOTAL_CONNECTIONS = new Key("elastic.search.server.max.total.connections");

    public static final Key ELASTIC_SEARCH_SEARCH_GUARD_SSL_TRANSPORT_KEYSTORE_PASSWORD = new Key("elastic.search.guard.ssl.transport.keystore_password");
    public static final Key ELASTIC_SEARCH_SEARCH_GUARD_SSL_TRANSPORT_TRUSTSTORE_PASSWORD = new Key("elastic.search.guard.ssl.transport.truststore_password");
    public static final Key ELASTIC_SEARCH_SEARCH_GUARD_SSL_TRANSPORT_KEYSTORE_FILE_PATH = new Key("elastic.search.guard.ssl.transport.keystore_file_path");
    public static final Key ELASTIC_SEARCH_SEARCH_GUARD_SSL_TRANSPORT_TRUSTSTORE_FILE_PATH = new Key("elastic.search.guard.ssl.transport.truststore_file_path");

    public static final Key ELASTIC_SEARCH_SERVER_ADDRESS_HOST = new Key("elastic.search.server.address.host");
    public static final Key ELASTIC_SEARCH_SERVER_ADDRESS_PORT = new Key("elastic.search.server.address.port");
    public static final Key ELASTIC_SEARCH_SERVER_USER_NAME = new Key("elastic.search.server.username");
    public static final Key ELASTIC_SEARCH_SERVER_PASSWORD = new Key("elastic.search.server.password");
    public static final Key ELASTIC_SEARCH_USE_HTTPS = new Key("elastic.search.use.https");
    public static final Key ELASTIC_SEARCH_INDEX_NAME = new Key("elastic.search.index.name");

    public static final Key DELETE_TEMP_FOLDER_CRON = new Key("delete.temp.folder.cron", "0 0 0 * * ? *");
    public static final Key MAX_TEMP_FOLDER_SIZE = new Key("max.temp.folder.size", "500000000"); //  ~ means 500MB
    public static final Key MAX_TEMP_FILES_CREATION_DATE_BY_HOUR = new Key("max.temp.files.creation.date.by.hour", "5");

    public static final Key APP_RELEVENCYRATE_RATIO = new Key("app.relevencyRate.ratio", "10");
    public static final Key APP_LAST_CHECK_RELEVENCY_SPAN = new Key("app.last.check.relevency.span", "-10");
    public static final Key APP_RELEVENCYRATE_CRON = new Key("app.relevenctRate.cron", "0 0/60 * * * ? *");

    public static final Key APP_INSTALLATION_CRON = new Key("app.installation.cron", "0 0/30 * * * ? *");

    public static final Key PACKAGE_PUBLISHING_CRON = new Key("package.publishing.cron", "0 0/20 * * * ? *");

    public static final Key APP_INSTALL_REPORT_MAX_SIZE = new Key("app.install.report.max.size", "200");
    public static final Key LAST_DATA_TO_SHOW = new Key("last.data.to.show", "9");

    public static final Key QUEUE_SERVER_URL = new Key("queue.server.url");
    public static final Key QUEUE_SERVER_USERNAME = new Key("queue.server.username");
    public static final Key QUEUE_SERVER_PASSWORD = new Key("queue.server.password");
    public static final Key QUEUE_SERVER_PORT = new Key("queue.server.port");

//    public static final Key QUEUE_OUTPUT_QUEUE = new Key("queue.output.queue");
//    public static final Key QUEUE_INPUT_QUEUE = new Key("queue.input.queue");
//    public static final Key NOTIFICATION_ENDPOINT_PEERID = new Key("notification.endPoint.peerId");
//    public static final Key NOTIFICATION_SERVICE_NAME = new Key("notification.service.name");
    public static final Key IOS_CODE_SIGNATURE_NAME = new Key("ios.codeSignature.name", "_CodeSignature/CodeResources");

    public static final Key ORGANIZATION_ASYNC_WEBSOCKET_URL = new Key("organization.async.websocket.url");
    public static final Key ORGANIZATION_ENGINE_ENDPOINT_NAME = new Key("organization.engine.endpoint.name");
    public static final Key ORGANIZATION_APPID = new Key("organization.appId", "Appstore");
    public static final Key ORGANIZATION_DEVICEID = new Key("organization.deviceId", "appStoreServer_server");
    public static final Key ORGANIZATION_QUEUENAME = new Key("organization.queue.name");
    public static final Key ORGANIZATION_SERVICE_IDTOKEN = new Key("organization.service.IDToken");

    /////////////////////////////////////////////////////////////////////////

    private static final Properties PROPERTIES = new Properties();

    public static void load(InputStream stream) {
        PROPERTIES.clear();

        try {
            PROPERTIES.load(stream);
            stream.close();
            if (PROPERTIES.size() == 0) {
                throw new RuntimeException("Empty [config.properties]!");
            }
        } catch (Exception var2) {
            throw new RuntimeException("Can\'t load [config.properties]!", var2);
        }
    }

    static {
        load(ConfigUtil.class.getResourceAsStream("/config.properties"));

        String fileAsString =
                ConfigUtil.class.getResource("/config.properties").getFile();
        File file = new File(fileAsString);
        String pathAsString = file.getParent();
        if (pathAsString.startsWith("/") || pathAsString.startsWith("\\")) {
            pathAsString = pathAsString.substring(1);
        }

        final Path path = Paths.get(pathAsString);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    while (true) {
                        final WatchKey wk = watchService.take();
                        for (WatchEvent<?> event : wk.pollEvents()) {
                            final Path changed = (Path) event.context();
                            if (changed.endsWith("config.properties")) {
                                logger.info("config.properties file has been reloaded!");
                                load(ConfigUtil.class.getResourceAsStream("/config.properties"));
                            }
                        }

                        boolean valid = wk.reset();
                        if (!valid) {
                            logger.info("watch key has been unregistered!");
                        }
                    }
                } catch (Exception ex) {
                    logger.error("error occured while watching fileService!");
                }
            }
        }).start();
    }

    public static String getProperty(Key key) {
        String value = PROPERTIES.getProperty(key.getKey());
        if (value == null) {
            if(key.hasDefaultValue())
                return key.getDefaultValue();
            else
                throw new RuntimeException("Key not found: " + key);
        }

        if (value.trim().length() == 0) {
            throw new RuntimeException("Key without value: " + key);
        }

        return value;
    }

    private static class Key {
        String key;
        String defaultValue;

        public Key(String key) {
            this.key = key;
        }

        public Key(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public boolean hasDefaultValue() {
            return defaultValue != null;
        }

        public String getKey() {
            return key;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }
}
