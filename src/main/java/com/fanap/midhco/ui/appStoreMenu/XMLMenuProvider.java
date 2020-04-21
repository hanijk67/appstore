package com.fanap.midhco.ui.appStoreMenu;

import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.ui.appStoreMenu.jaxbs.AppStoreMenuPanel;
import com.fanap.midhco.ui.appStoreMenu.jaxbs.Menu;
import com.fanap.midhco.ui.appStoreMenu.jaxbs.ObjectFactory;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by admin123 on 6/21/2016.
 */
public class XMLMenuProvider implements IMenuProvider {
    private static final Logger logger = Logger.getLogger(XMLMenuProvider.class);
    public static final XMLMenuProvider Instance = new XMLMenuProvider();

    AppStoreMenuPanel appStoreMenuPanel;

    private XMLMenuProvider() {
        InputStream appStoreMenuInputStream = null;
        try {
            appStoreMenuInputStream = this.getClass().getClassLoader().getResourceAsStream("/appStoreMenu.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            appStoreMenuPanel = (AppStoreMenuPanel) jaxbUnmarshaller.unmarshal(appStoreMenuInputStream);
        } catch (Exception ex) {
            throw new AppStoreRuntimeException("error loading ");
        } finally {
            if(appStoreMenuInputStream != null)
                try {
                    appStoreMenuInputStream.close();
                } catch (IOException e) {
                    logger.error("error closing appStoreMenu XML inputStream!", e);
                }
        }
    }

    public List<Menu> getMenuItems() throws JAXBException {
        return appStoreMenuPanel.getMenus();
    }
}
