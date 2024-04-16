package com.ws.tool;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MysqlTypeMapInfo {

    public static final Map<String, String> JAVA_MAP_MYBATIS_SQL_TYPE = new HashMap<>();
    public static final Map<String, String> SQL_MAP_JAVA_TYPE = new HashMap<>();
    public static final Map<String, Integer> SQL_TYPE_MAP_DEFAULT_LENGTH = new HashMap<>();

    static {
        JAVA_MAP_MYBATIS_SQL_TYPE.put(Integer.class.getName(), "INTEGER");
        JAVA_MAP_MYBATIS_SQL_TYPE.put(Long.class.getName(), "BIGINT");
        JAVA_MAP_MYBATIS_SQL_TYPE.put(Short.class.getName(), "SMALLINT");
        JAVA_MAP_MYBATIS_SQL_TYPE.put(Float.class.getName(), "FLOAT");
        JAVA_MAP_MYBATIS_SQL_TYPE.put(Double.class.getName(), "DOUBLE");
        JAVA_MAP_MYBATIS_SQL_TYPE.put(Boolean.class.getName(), "VARCHAR");
        JAVA_MAP_MYBATIS_SQL_TYPE.put(Character.class.getName(), "CHAR");
        JAVA_MAP_MYBATIS_SQL_TYPE.put(BigDecimal.class.getName(), "DECIMAL");
        JAVA_MAP_MYBATIS_SQL_TYPE.put(String.class.getName(), "VARCHAR");
        JAVA_MAP_MYBATIS_SQL_TYPE.put(Date.class.getName(), "TIMESTAMP");
        JAVA_MAP_MYBATIS_SQL_TYPE.put(Byte[].class.getName(), "BLOB");
    }

    static {
        SQL_MAP_JAVA_TYPE.put("INTEGER", Integer.class.getName());
        SQL_MAP_JAVA_TYPE.put("INT", Integer.class.getName());
        SQL_MAP_JAVA_TYPE.put("DECIMAL", BigDecimal.class.getName());
        SQL_MAP_JAVA_TYPE.put("BIGINT", Long.class.getName());
        SQL_MAP_JAVA_TYPE.put("SMALLINT", Short.class.getName());
        SQL_MAP_JAVA_TYPE.put("FLOAT", Float.class.getName());
        SQL_MAP_JAVA_TYPE.put("DOUBLE", Double.class.getName());
        SQL_MAP_JAVA_TYPE.put("CHAR", Character.class.getName());
        SQL_MAP_JAVA_TYPE.put("VARCHAR", String.class.getName());
        SQL_MAP_JAVA_TYPE.put("LONGTEXT", String.class.getName());
        SQL_MAP_JAVA_TYPE.put("TEXT", String.class.getName());
        SQL_MAP_JAVA_TYPE.put("ENUM", String.class.getName());
        SQL_MAP_JAVA_TYPE.put("BIT", String.class.getName());
        SQL_MAP_JAVA_TYPE.put("TINYINT", String.class.getName());
        SQL_MAP_JAVA_TYPE.put("JSON", String.class.getName());
        SQL_MAP_JAVA_TYPE.put("TIMESTAMP", Date.class.getName());
        SQL_MAP_JAVA_TYPE.put("DATETIME", Date.class.getName());
        SQL_MAP_JAVA_TYPE.put("DATE", Date.class.getName());
        SQL_MAP_JAVA_TYPE.put("BLOB", Byte[].class.getName());
        SQL_MAP_JAVA_TYPE.put("VARBINARY", Byte[].class.getName());
    }

    static {
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("TINYINT", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("SMALLINT", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("MEDIUMINT", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("INT", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("INTEGER", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("BIGINT", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("FLOAT", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("DOUBLE", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("REAL", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("DECIMAL", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("NUMERIC", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("DATE", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("TIME", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("DATETIME", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("TIMESTAMP", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("YEAR", 4);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("CHAR", 1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("VARCHAR", 255);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("BINARY", 1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("VARBINARY", 255);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("TINYBLOB", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("BLOB", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("MEDIUMBLOB", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("LONGBLOB", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("TINYTEXT", 255);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("TEXT", 6553);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("MEDIUMTEXT", 6553);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("LONGTEXT", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("ENUM", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("SET", -1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("BOOLEAN", 1);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("SERIAL", 4);
        SQL_TYPE_MAP_DEFAULT_LENGTH.put("JSON", -1);
    }

    @NotNull
    public static String getMybatisType(@NotNull Field field) {
        String type = field.getType().getName();
        String mysqlType = JAVA_MAP_MYBATIS_SQL_TYPE.get(type);
        if (mysqlType == null) {
            throw new IllegalArgumentException("Unsupported field: " + field);
        }
        return mysqlType;
    }

    public static String getMybatisType(String javaType) {
        String mysqlType = JAVA_MAP_MYBATIS_SQL_TYPE.get(javaType);
        if (StringUtil.isEmpty(mysqlType)) {
            throw new IllegalArgumentException("Unsupported type: " + javaType);
        }
        return mysqlType;
    }

    public static String getJavaType(@NotNull String sqlType) {
        String javaType = SQL_MAP_JAVA_TYPE.get(sqlType.toUpperCase());
        if (StringUtil.isEmpty(javaType)) {
            throw new IllegalArgumentException("Unsupported type: " + sqlType);
        }
        return javaType;
    }

    public static Integer getSqlTypeDefaultLength(Field field) {
        Integer length = SQL_TYPE_MAP_DEFAULT_LENGTH.get(getMybatisType(field));
        if (Objects.isNull(length)) {
            throw new IllegalArgumentException("Unsupported field: " + field);
        }
        return length;
    }

    public static Integer getSqlTypeDefaultLengthBySqlType(String sqlType) {
        Integer length = SQL_TYPE_MAP_DEFAULT_LENGTH.get(sqlType);
        if (Objects.isNull(length)) {
            throw new IllegalArgumentException("Unsupported type: " + sqlType);
        }
        return length;
    }

    public static Integer getSqlTypeDefaultLength(String javaType) {
        Integer length = SQL_TYPE_MAP_DEFAULT_LENGTH.get(getMybatisType(javaType));
        if (Objects.isNull(length)) {
            throw new IllegalArgumentException("Unsupported javaType: " + javaType);
        }
        return length;
    }

}
