package com.fanap.midhco.appstore.persistence;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import java.util.Locale;

/**
 * Created by admin123 on 6/5/2016.
 */
public class Dao {
    static final Logger logger = Logger.getLogger(Dao.class);
    public static final Dao Instance = new Dao();

    private static final ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
    private SessionFactory sessionFactory;
    private Configuration conf;



    private Dao() {
        logger.debug("initializing Dao configuration .... ");
        try {
            conf = new Configuration();
            conf = conf.configure("hibernate.cfg.xml");
            conf.setProperty("hibernate.connection.url", ConfigUtil.getProperty(ConfigUtil.DB_URL));
            conf.setProperty("hibernate.connection.username", ConfigUtil.getProperty(ConfigUtil.DB_USERNAME));

//            String dbPassword = ConfigUtil.getProperty(ConfigUtil.DB_PASSOWRD);
//            dbPassword = new String(com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(dbPassword));
//            conf.setProperty("hibernate.connection.password", dbPassword);
            conf.setProperty("hibernate.connection.password", ConfigUtil.getProperty(ConfigUtil.DB_PASSOWRD));

            conf.setProperty("hibernate.show_sql", ConfigUtil.getProperty(ConfigUtil.DB_SHOWSQL));
            conf.setProperty("hibernate.use_sql_comments", ConfigUtil.getProperty(ConfigUtil.DB_SHOWSQL));
            conf.setProperty("hibernate.generate_statistics", ConfigUtil.getProperty(ConfigUtil.DB_SHOWSQL));
            String hbm2ddl = ConfigUtil.getProperty(ConfigUtil.DB_HBM2DDL);
            if (!(hbm2ddl.equalsIgnoreCase("none") || hbm2ddl.equalsIgnoreCase("update")))
                hbm2ddl = "none";

            if(hbm2ddl.trim().equals("update") || hbm2ddl.trim().equals("create"))
                hbm2ddl = "none";

            conf.setProperty("hibernate.hbm2ddl.auto", hbm2ddl);

            Locale.setDefault(Locale.ENGLISH);

            conf.setProperty("hibernate.connection.CharSet", "UTF-8");
            conf.setProperty("hibernate.connection.characterEncoding", "UTF-8");
            conf.setProperty("hibernate.connection.useUnicode", "true");
            conf.setProperty("automaticTestTable", "autoTestTable");
            conf.setProperty("testConnectionOnCheckout", "true");

            sessionFactory = conf.buildSessionFactory();
            logger.debug("end Dao configuration ....");
        } catch (Exception ex) {
            throw new RuntimeException("Error initializing dao ", ex);
        }
    }

    public Session getCurrentSession() {
        checkAndRebuildSessionFactory();
        Session session = currentSession.get();
        if (session == null || !session.isOpen()) {
            session = sessionFactory.openSession();
            currentSession.set(session);
        }
        return session;
    }

    private void checkAndRebuildSessionFactory() {
        if(sessionFactory==null || sessionFactory.isClosed()){
            try {
                rebuildSessionFactory(conf);
            } catch (Exception e) {
                logger.error("error on getting session factory ....");
                logger.error(e.getMessage());
            }
        }
    }

    public Session getNewSession() {
        checkAndRebuildSessionFactory();
        return sessionFactory.openSession();
    }


    /**
     * Rebuild the SessionFactory with the given Hibernate Configuration.
     *
     * @param cfg
     */
    public  void rebuildSessionFactory(Configuration cfg)
            throws Exception {
        synchronized(sessionFactory) {
            try {
                sessionFactory = cfg.buildSessionFactory();
                conf = cfg;
            } catch (Exception ex) {
                throw ex;
            }
        }
    }


    public void closeSessionFactory() {
        if (sessionFactory != null)
            sessionFactory.close();
    }

    public static void main(String[] args) {
        Configuration conf = new Configuration();
        conf = conf.configure("hibernate.cfg.xml");

        conf.setProperty("hibernate.connection.url", ConfigUtil.getProperty(ConfigUtil.DB_URL));
        conf.setProperty("hibernate.connection.username", ConfigUtil.getProperty(ConfigUtil.DB_USERNAME));
//        String dbPassword = ConfigUtil.getProperty(ConfigUtil.DB_PASSOWRD);
//        dbPassword = new String(com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(dbPassword));
        conf.setProperty("hibernate.connection.password", ConfigUtil.getProperty(ConfigUtil.DB_PASSOWRD));

        SchemaUpdate schemaUpdate = new SchemaUpdate(conf);
        schemaUpdate.execute(true, false);
        System.exit(0);

    }
}
