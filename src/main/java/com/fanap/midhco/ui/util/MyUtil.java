package com.fanap.midhco.ui.util;

/**
 * Created by admin123 on 6/22/2016.
 */

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;

import java.util.List;

public class MyUtil {

    public static void enable(List fields) {
        int index, classIndex;
        for (Object field : fields) {
            index = findDisabledIndex(field);
            if (index != -2) {
                ((FormComponent) field).remove((Behavior) ((FormComponent) field).getBehaviors().get(index));
                classIndex = getDisabled((FormComponent) field);
                if (classIndex != -2)
                    ((FormComponent) field).remove((Behavior) ((FormComponent) field).getBehaviors().get(classIndex));
            }
        }
    }

    private static int getDisabled(FormComponent cmp) {
        int i = -1;
        for (Object attr : cmp.getBehaviors()) {
            AttributeModifier attrM = (AttributeModifier) attr;
            i++;
            if (attrM.getAttribute().equals("class")) {
                return i;
            }
        }
        return -2;
    }

    private static int findDisabledIndex(Object field) {
        int i = -1;
        for (Object att : ((FormComponent) field).getBehaviors()) {
            AttributeModifier attributeM = (AttributeModifier) att;
            i++;
            if (attributeM.getAttribute().equals("disabled"))
                return i;
        }
        return -2;
    }

    public static void disable(List fields) {
        for (Object cmp : fields) {
            int index = haveDisable((FormComponent) cmp);
            if (index == -2) {
                ((FormComponent) cmp).add(new AttributeModifier("class", new Model("disabled")));
                ((FormComponent) cmp).add(new AttributeModifier("disabled", new Model("")));
            }
        }
    }

    private static int haveReadOnly(FormComponent cmp) {
        int i = -1;
        for (Object attributeModifier : cmp.getBehaviors()) {
            i++;
            if (attributeModifier instanceof AttributeModifier)
                if (((AttributeModifier) attributeModifier).getAttribute().equals("readonly"))
                    return i;
        }
        return -2;
    }

    public static void readOnly(List fields) {
        for (Object cmp : fields) {
            int index = haveReadOnly((FormComponent) cmp);
            if (index == -2) {
                ((FormComponent) cmp).add(new AttributeModifier("class", new Model("disabled")));
                ((FormComponent) cmp).add(new AttributeModifier("readonly", new Model("readonly")));
            }
        }
    }

    public static int haveDisable(FormComponent cmp) {
        int i = -1;
        if (cmp == null)
            return -3;
        if (cmp.getBehaviors() != null)
            for (Object attributeModifier : cmp.getBehaviors()) {
                i++;
                if (attributeModifier instanceof AttributeModifier)
                    if (((AttributeModifier) attributeModifier).getAttribute().equals("disabled"))
                        return i;
            }
        return -2;
    }

    public static void disable(FormComponent cmp) {
        int index = haveDisable(cmp);
        if (index == -2) {
            cmp.add(new AttributeModifier("class", new Model("disabled")));
            cmp.add(new AttributeModifier("disabled", new Model("")));
        }
    }
}
