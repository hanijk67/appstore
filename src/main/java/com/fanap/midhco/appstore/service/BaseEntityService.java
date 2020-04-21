package com.fanap.midhco.appstore.service;

import com.fanap.midhco.appstore.entities.BaseEntitiy;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DayDate;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Session;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Created by Hamid on 7/8/2016.
 */
public class BaseEntityService {
    public static final BaseEntityService Instance = new BaseEntityService();

    private BaseEntityService() {
    }

    public void saveOrUpdate(BaseEntitiy entitiy, Session session) {
        if (entitiy.getId() == null) {
            entitiy.setCreationDate(DateTime.now());
            entitiy.setCreatorUser(PrincipalUtil.getCurrentUser());
        } else {
            entitiy.setLastModifyDate(DateTime.now());
            entitiy.setLastModifyUser(PrincipalUtil.getCurrentUser());
        }
        session.saveOrUpdate(entitiy);
    }

    public static boolean checkAllFieldsNull(Object obj) {
        try {
            if (obj == null) {
                return true;
            } else {
                for (Field field : obj.getClass().getFields()) {
                    Object value = field.get(obj);
                    if (value == null)
                        continue;

                    if (value instanceof DateTime[]) {
                        DateTime[] dts = (DateTime[]) value;
                        if (dts.length > 0)
                            if (!DateTime.isNullOrUnknown(dts[0]) || !DateTime.isNullOrUnknown(dts[1]))
                                return false;
                    } else if (value instanceof DayDate[]) {
                        DayDate[] dts = (DayDate[]) value;
                        if (!DayDate.isNullOrUnknown(dts[0]) || !DayDate.isNullOrUnknown(dts[1]))
                            return false;
                    } else if (value instanceof Long[]) {
                        Long[] lng = (Long[]) value;
                        if (lng[0] != null || lng[1] != null)
                            return false;
                    } else if (value instanceof Collection) {
                        Collection col = (Collection) value;
                        if (col.size() > 0)
                            return false;
                    } else
                        return false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

}
