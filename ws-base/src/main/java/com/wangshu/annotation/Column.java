package com.wangshu.annotation;

import com.wangshu.base.model.BaseModel;
import com.wangshu.enu.Condition;

import java.lang.annotation.*;

/**
 * 标记连表查询字段
 * JOIN
 *
 * @author GSF
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * <p>默认是BaseModel,自动获取当前类</p>
     **/
    Class<? extends BaseModel> table() default BaseModel.class;

    boolean primary() default false;

    String jdbcType() default "";

    int length() default -1;

    String comment() default "";

    /**
     * <p>当前属性别名,默认可以为空,仅用于显示给前端</p>
     **/
    String title() default "";

    boolean keyword() default false;

    int order() default 0;

    /**
     * <p>查询条件,当为{all}时,指代所有条件</p>
     **/
    Condition[] conditions() default {Condition.equal};

    boolean enableFullText() default false;

}
