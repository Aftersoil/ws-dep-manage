package com.ws.annotation;

import com.ws.ConfigRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author GSF
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ConfigRegister.class)
public @interface EnableConfig {

    String[] modelPackage() default {};

    boolean enableAutoInitTable() default false;

    String[] targetDataSource() default {"*"};

    boolean enableExceptionHandle() default true;

}
