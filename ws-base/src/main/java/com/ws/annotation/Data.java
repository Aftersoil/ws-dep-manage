package com.ws.annotation;

import com.ws.enu.ColumnType;
import com.ws.enu.DataBaseType;

import java.lang.annotation.*;

/**
 * 标记model实体类
 *
 * @author GSF
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Data {

    String table() default "";

    String modelDefaultKeyword() default "";

    String title() default "";

    String[] names() default {};

    DataBaseType dataBaseType() default DataBaseType.mysql;

    ColumnType columnType() default ColumnType.antd;

}
