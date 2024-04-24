package com.ws;

import com.ws.annotation.Data;
import com.ws.annotation.EnableConfig;
import com.ws.base.controller.BaseDataController;
import com.ws.base.model.BaseModel;
import com.ws.base.service.BaseDataService;
import com.ws.table.GenerateTable;
import com.ws.table.GenerateTableMysql;
import com.ws.tool.CacheTool;
import com.ws.tool.CommonParam;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author GSF
 */
@Slf4j
public class ConfigManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private EnableConfig enableConfig;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.enableConfig = CommonParam.mainClazz.getAnnotation(EnableConfig.class);
        CommonParam.applicationContext = applicationContext;
        try {
            CommonParam.modelClazz = CommonParam.getTargetPackageModelClazz(this.enableConfig.modelPackage());
            this.cacheConfig();
            this.tableConfig();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void cacheConfig() {
        CacheTool.initModelCache(new ArrayList<>(CommonParam.modelClazz));
        CacheTool.initServiceCache(applicationContext.getBeansOfType(BaseDataService.class).values().stream().map(item -> item.getClass()).collect(Collectors.toList()));
        CacheTool.initControllerCache(applicationContext.getBeansOfType(BaseDataController.class).values().stream().map(item -> item.getClass()).collect(Collectors.toList()));
    }

    private void tableConfig() {
        if (this.enableConfig.enableAutoInitTable() && Objects.nonNull(this.enableConfig.modelPackage())) {
            List<String> targetDataSource = List.of(this.enableConfig.targetDataSource());
            Map<String, DataSource> dataSourceMap = applicationContext.getBeansOfType(DataSource.class);
            if (!targetDataSource.contains("*")) {
                for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
                    if (!targetDataSource.contains(entry.getKey())) {
                        dataSourceMap.remove(entry.getKey());
                    }
                }
            }
            dataSourceMap.forEach((k, v) -> {
                try (Connection connection = v.getConnection()) {
                    for (Class<? extends BaseModel> modelClazz : CommonParam.modelClazz) {
                        Data dataAnnotation = modelClazz.getAnnotation(Data.class);
                        GenerateTable generateTable = null;
                        switch (dataAnnotation.dataBaseType()) {
                            case mysql -> generateTable = new GenerateTableMysql(modelClazz);
                            case sqlServer -> generateTable = null;
                            case oracle -> generateTable = null;
                        }
                        if (Objects.isNull(generateTable)) {
                            log.warn("暂无对应数据库类型实现: {}", dataAnnotation.dataBaseType());
                        } else {
                            generateTable.createTable(connection);
                        }
                    }
                } catch (SQLException e) {
                    log.error("数据源: {}, 获取连接失败", k);
                }
            });
        }
    }

//    private @NotNull String getProxyClazzFullName(@NotNull Object obj) {
//        String name = obj.getClass().getName();
//        if (name.contains("$$")) {
//            return name.substring(0, name.indexOf("$$"));
//        }
//        return name;
//    }

//    private void optionalMapper() {
//        Configuration configuration = sqlSessionFactory.getConfiguration();
//        Map<String, BaseMapper> mapperBeans = this.applicationContext.getBeansOfType(BaseMapper.class);
//        for (BaseMapper mapperBean : mapperBeans.values()) {
//            if (mapperBean instanceof OptionalMapper<?>) {
//                Class<?> mapperClazz = mapperBean.getClass().getInterfaces()[0];
//                Class<? extends BaseModel> mapperModel = getMapperModel(mapperClazz);
//                MappedStatement.Builder executeSqlMappedStatement = getExecuteSqlMappedStatement(mapperClazz, configuration);
//                MappedStatement.Builder sqlSaveMappedStatement = getSqlSaveMappedStatement(mapperClazz, configuration);
//                configuration.addMappedStatement(executeSqlMappedStatement.build());
//                configuration.addMappedStatement(sqlSaveMappedStatement.build());
//            }
//        }
//    }
//
//    public MappedStatement.Builder getExecuteSqlMappedStatement(Class<?> mapperClazz, Configuration configuration) {
//        String id = StringUtil.concat(mapperClazz.getName(), ".", OptionalMethod.executeSql.name());
//        SqlSource sqlSource = parameterObject -> new BoundSql(configuration, (String) parameterObject, List.of(), parameterObject);
//        return new MappedStatement.Builder(configuration, id, sqlSource, SqlCommandType.SELECT);
//    }
//
//    public MappedStatement.Builder getSqlSaveMappedStatement(Class<?> mapperClazz, Configuration configuration) {
//        String id = StringUtil.concat(mapperClazz.getName(), ".", OptionalMethod.sqlSave.name());
//        SqlSource sqlSource = new SqlSource() {
//            @Override
//            public BoundSql getBoundSql(Object parameterObject) {
//                return new BoundSql(configuration, (String) parameterObject, List.of(), parameterObject);
//            }
//        };
//        return new MappedStatement.Builder(configuration, id, sqlSource, SqlCommandType.INSERT);
//    }

}
