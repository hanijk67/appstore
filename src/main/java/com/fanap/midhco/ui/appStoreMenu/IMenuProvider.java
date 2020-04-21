package com.fanap.midhco.ui.appStoreMenu;

import java.util.List;

/**
 * Created by admin123 on 6/21/2016.
 */
public interface IMenuProvider {
    public List<? extends IMenuItem> getMenuItems() throws Exception;
}
