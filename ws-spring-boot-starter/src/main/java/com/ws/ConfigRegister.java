package com.ws;

import com.ws.annotation.EnableConfig;
import com.ws.exception.IExceptionHandler;
import com.ws.tool.CommonParam;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class ConfigRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(@NotNull AnnotationMetadata importingClassMetadata, @NotNull BeanDefinitionRegistry registry) {
        try {
            Class<?> clazz = Class.forName(importingClassMetadata.getClassName());
            CommonParam.mainClazz = clazz;
            EnableConfig enableConfig = clazz.getAnnotation(EnableConfig.class);
            if (enableConfig.enableExceptionHandle()) {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(IExceptionHandler.class);
                registry.registerBeanDefinition("iExceptionHandler", builder.getBeanDefinition());
            }
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ConfigManager.class);
            builder.setLazyInit(false);
            registry.registerBeanDefinition("configManager", builder.getBeanDefinition());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
