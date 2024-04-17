package com.ws.table;

import cn.hutool.core.util.StrUtil;
import com.ws.annotation.Column;
import com.ws.tool.StringUtil;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @author GSF
 */
@EqualsAndHashCode(callSuper = true)
@lombok.Data
public abstract class GenerateTable extends ModelInfo {

    public GenerateTable() {
    }

    public GenerateTable(Class<?> clazz) {
        super(clazz);
    }

    public abstract String getJdbcType(Field field);

    public abstract int getDefaultLength(Field field);

    public boolean getDefaultNullFlag(Field field) {
        return !getPrimaryKeyFlag(field);
    }

    public boolean getPrimaryKeyFlag(@NotNull Field field) {
        if (Objects.nonNull(field.getAnnotation(Column.class))) {
            return field.getAnnotation(Column.class).primary() || StrUtil.equals(field.getName(), "id");
        }
        return StrUtil.equals(field.getName(), "id");
    }

    public String getComment(@NotNull Field field) {
        String comment = null;
        Column column = field.getAnnotation(Column.class);
        if (Objects.nonNull(column)) {
            comment = column.comment();
            if (StringUtil.isEmpty(comment)) {
                comment = column.title();
            }
        }
        if (StringUtil.isEmpty(comment)) {
            comment = field.getName();
        }
        return comment;
    }

    public abstract void createTable(Connection connection) throws SQLException;

}