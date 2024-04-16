package com.ws.cache.column;

import com.ws.annotation.Column;
import com.ws.tool.StringUtil;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Objects;

@Data
public class ColumnType {

    String name;
    String dataType;
    Integer order = 0;
    String title;

    public ColumnType() {

    }

    public ColumnType(Field field) {
        this.name = field.getName();
        this.dataType = field.getType().getSimpleName();
        this.order = fieldOrder(field);
        this.title = fieldTitle(field);
    }

    private String fieldTitle(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (Objects.nonNull(column)) {
            if (StringUtil.isEmpty(column.title())) {
                return column.comment();
            }
            return column.title();
        }
        return field.getName();
    }

    private Integer fieldOrder(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (Objects.nonNull(column)) {
            return column.order();
        }
        return 0;
    }

}
