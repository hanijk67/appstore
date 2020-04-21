package com.fanap.midhco.ui.appStoreMenu;

import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin123 on 6/21/2016.
 */
public class AppStoreMenu {
    public static AppStoreMenu Instance = new AppStoreMenu();

    IMenuProvider menuProvider;
    Map<String, Class> panelId2ClassMap = new HashMap<String, Class>();
    Map<String, IMenuItem> panelClass2IdMap = new HashMap<String, IMenuItem>();

    private AppStoreMenu() {
    }

    public void init() {
        try {
            menuProvider = XMLMenuProvider.Instance;
            List<? extends IMenuItem> menuItems = menuProvider.getMenuItems();
            for(IMenuItem menuItem : menuItems) {
                Class menuItemClass = Class.forName(menuItem.getMenuClass());
                panelId2ClassMap.put(menuItem.getIdCode(), menuItemClass);
                panelClass2IdMap.put(menuItem.getMenuClass(), menuItem);
            }
        } catch (Exception ex) {
            throw new AppStoreRuntimeException("Error loading menu Items: ", ex);
        }
    }

    public Class getPanelClass(String panelId) {
        return panelId2ClassMap.get(panelId);
    }

    public IMenuItem getMenuItem(Class pageClass) {
        return panelClass2IdMap.get(pageClass.getCanonicalName());
    }
}
