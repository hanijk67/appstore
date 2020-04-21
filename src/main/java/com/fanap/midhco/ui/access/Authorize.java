package com.fanap.midhco.ui.access;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.LOCAL_VARIABLE, ElementType.FIELD})
@Documented
@Inherited
public @interface Authorize {
    public abstract Access view() default Access.NULL;
    public abstract Access[] views() default {};
}
