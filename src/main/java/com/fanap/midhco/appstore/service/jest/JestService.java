package com.fanap.midhco.appstore.service.jest;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import io.searchbox.annotations.JestId;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by admin123 on 4/4/2017.
 */
public class JestService {
    public static JestService Instance = new JestService();

    private JestService() {
    }

    private static class InnerJestClientFactory {
        private static JestClientFactory jestClientFactory;

        static {
            try {
                String elastic_server_address =
                        "http://" + ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SERVER_ADDRESS_HOST) + ":" + ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SERVER_ADDRESS_PORT);
                Integer maxConnectionsPerRoute = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SERVER_MAX_CONNECTIONS_PER_ROUTE));
                Integer maxTotalConnections = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SERVER_MAX_TOTAL_CONNECTIONS));

                jestClientFactory = new JestClientFactory();
                HttpClientConfig httpClientConfig = new HttpClientConfig
                        .Builder(elastic_server_address)
                        .multiThreaded(true)
                        .defaultMaxTotalConnectionPerRoute(maxConnectionsPerRoute)
                        .maxTotalConnection(maxTotalConnections)
                        .build();
                jestClientFactory.setHttpClientConfig(httpClientConfig);
            } catch (Exception ex) {
            }
        }

        public static JestClientFactory getJestClientFactory() {
            return jestClientFactory;
        }

        static JestClientFactory httpsJestClientFactory = null;

        public static JestClientFactory getHttpsJestClientFactory() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
            if (httpsJestClientFactory == null) {
                KeyStore ks1 = KeyStore.getInstance("jks");
                String trustStoreFilePath = ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SEARCH_GUARD_SSL_TRANSPORT_TRUSTSTORE_FILE_PATH);
                ks1.load(new FileInputStream(trustStoreFilePath), ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SEARCH_GUARD_SSL_TRANSPORT_TRUSTSTORE_PASSWORD).toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks1);
                TrustManager[] tm = tmf.getTrustManagers();

                KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                KeyStore ks2 = KeyStore.getInstance("jks");
                String keyStoreFilePath = ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SEARCH_GUARD_SSL_TRANSPORT_KEYSTORE_FILE_PATH);
                ks2.load(new FileInputStream(keyStoreFilePath), ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SEARCH_GUARD_SSL_TRANSPORT_KEYSTORE_PASSWORD).toCharArray());
                kmFactory.init(ks2, ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SEARCH_GUARD_SSL_TRANSPORT_KEYSTORE_PASSWORD).toCharArray());
                KeyManager[] km = kmFactory.getKeyManagers();
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(ks1, new TrustStrategy() {
                    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                        return true;
                    }
                }).build();

                sslContext.init(km, tm, null);

                HostnameVerifier hostNameVerifier = NoopHostnameVerifier.INSTANCE;
                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostNameVerifier);
                SchemeIOSessionStrategy httpsIOSessionStrategy = new SSLIOSessionStrategy(sslContext, hostNameVerifier);

                httpsJestClientFactory = new JestClientFactory();

                BasicCredentialsProvider customCredentialsProvider = new BasicCredentialsProvider();
                customCredentialsProvider.setCredentials(
                        new AuthScope(ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SERVER_ADDRESS_HOST), Integer.valueOf(ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SERVER_ADDRESS_PORT))),
                        new UsernamePasswordCredentials(ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SERVER_USER_NAME), ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SERVER_PASSWORD))
                );
                StringBuilder serverStringBuilder = new StringBuilder();
                serverStringBuilder.append("https://").append(ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SERVER_ADDRESS_HOST)).append(":")
                        .append(ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_SERVER_ADDRESS_PORT));
                httpsJestClientFactory.setHttpClientConfig(
                        new HttpClientConfig.Builder(serverStringBuilder.toString())
                                .defaultSchemeForDiscoveredNodes("https") // required, otherwise uses http
                                .sslSocketFactory(sslSocketFactory) // this only affects sync calls
                                .httpsIOSessionStrategy(httpsIOSessionStrategy) // this only affects async calls
                                .credentialsProvider(customCredentialsProvider)
                                .build()
                );
            }
            return httpsJestClientFactory;
        }


    }

    public JestClient getJestClient() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        String useHttpsStr = ConfigUtil.getProperty(ConfigUtil.ELASTIC_SEARCH_USE_HTTPS);
        boolean useHttps = useHttpsStr.equals("true") ? true : false;
        if (useHttps)
            return InnerJestClientFactory.getHttpsJestClientFactory().getObject();
        else
            return InnerJestClientFactory.getJestClientFactory().getObject();
    }

    static class Article {
        @JestId
        Long documentId;

        String documentName;

        public Long getDocumentId() {
            return documentId;
        }

        public void setDocumentId(Long documentId) {
            this.documentId = documentId;
        }

        public String getDocumentName() {
            return documentName;
        }

        public void setDocumentName(String documentName) {
            this.documentName = documentName;
        }
    }

    public static void main(String[] args) throws Exception {
//        Set<String> servers = new LinkedHashSet<String>();
//        servers.add("http://localhost:9200");

//        HttpClientConfig httpClientConfig = new HttpClientConfig
//                .Builder("http://localhost:9200")
//                .multiThreaded(true)
//                .defaultMaxTotalConnectionPerRoute(12)
//                .maxTotalConnection(400)
//                .build();
//        JestClientFactory factory = new JestClientFactory();
//        factory.setHttpClientConfig(httpClientConfig);
//        JestClient jestClient = factory.getObject();
//
//        Session session = HibernateUtil.getCurrentSession();
//        App app = AppService.Instance.load(1019l, session);
//
//        User me = UserService.Instance.findUser("admin", session);
//
//        CommentService.DeviceMetaData deviceMetaData = new CommentService.DeviceMetaData();
//        deviceMetaData.setCpuModel("AA");
//        deviceMetaData.setDeviceClass("1Q");
//        deviceMetaData.setManufacturer("SAMSUNG");
//        deviceMetaData.setProductName("SAMSUNG GALAXY");
//        deviceMetaData.setRAM("128GB");
//        deviceMetaData.setScreenDensityDpi("2000");
//        deviceMetaData.setScreenHeight("120");
//        deviceMetaData.setScreenWidth("50");
//
//        CommentService.ElasticCommentVO commentVO = CommentService.Instance.buildCommentVOForElastic(app, deviceMetaData, "SALAM SAG",
//                "ENG", new Date().getTime(), me.getUserName(), me.getUserId(), RatingIndex.Excellent);
//
//        JestResult jestResult = CommentService.Instance.insertCommentForAppMainPackage(commentVO);

//        ElasticLogService.RequestLog requestLog = new ElasticLogService.RequestLog();
//        requestLog.setInnerTrackerId("232343L");
//        ElasticLogService.RequestVO requestVO = new ElasticLogService.RequestVO();
//        requestVO.setRequestByteSize(2132l);
//        requestVO.setRequestMessageType("LOGIN_REQUEST");
//        requestLog.setRequest(requestVO);

//        CommentService.ElasticCommentLikeVO elasticCommentLikeVO =
//                CommentService.Instance.buildLikeVOForElastic(true, "admin", 22l, "AV2i0j8KkwJ2p56NZtgX");
//        JestResult jestResult = CommentService.Instance.insertLikeForComment(elasticCommentLikeVO);

//        CommentService.ElasticCommentReportVO elasticCommentReportVO = new CommentService.ElasticCommentReportVO();
//        elasticCommentReportVO.setParentCommentId("AV2i0j8KkwJ2p56NZtgX");
//        elasticCommentReportVO.setReportText("خفه شو");
//        elasticCommentReportVO.setUserId(22l);
//        elasticCommentReportVO.setUserName("admin");
//
//        JestResult jestResult = CommentService.Instance.insertReportForComment(elasticCommentReportVO);
//        if (jestResult.isSucceeded()) {
//            System.out.println("salam!");
//        } else {
//            System.out.println("sag!");
//        }

//        jestClient.execute(new CreateIndex.Builder("articles").build());
//        Settings.Builder settingsBuilder = Settings.builder();
//        settingsBuilder.put("number_of_shards",5);
//        settingsBuilder.put("number_of_replicas",1);
//
//        jestClient.execute(new CreateIndex.Builder("articles").settings(settingsBuilder.build().getAsMap()).build());
//
//        PutMapping putMapping = new PutMapping.Builder(
//                "my_index",
//                "my_type",
//                "{ \"my_type\" : { \"properties\" : { \"message\" : {\"type\" : \"string\", \"store\" : \"yes\"} } } }"
//        ).build();
//        jestClient.execute(putMapping);


//        factory.setHttpClientConfig(clientConfig.get);
//        factory.setClientConfig(clientConfig);
//        JestClient client = factory.getObject();


//        JestClient jestClient = JestService.Instance.getJestClient();
//
//
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//
//        searchSourceBuilder.query(boolQueryBuilder);
//        Search search = new Search.Builder(searchSourceBuilder.toString())
//                .addIndex("device")
//                .addType("deviceMetaData")
//                .build();
//
//
//        SearchResult result = jestClient.execute(search);
//        System.out.println("22");
    }
}
