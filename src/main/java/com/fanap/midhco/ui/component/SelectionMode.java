package com.fanap.midhco.ui.component;

/**
 * Created by admin123 on 6/22/2016.
 */
public enum SelectionMode {
    None, Single, Multiple, MultipleOrQuery,WithoutAdd;

    public boolean isSelectable() {
        return this == Multiple || this == Single || this == MultipleOrQuery;
    }
}
