package com.ws.cache;

import com.ws.annotation.Column;
import com.ws.annotation.Data;
import com.ws.base.model.BaseModel;
import com.ws.cache.column.ColumnType;
import com.ws.cache.column.ColumnTypeFactory;
import com.ws.tool.CommonTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@lombok.Data
public class ModelCache {

    public List<Field> fields;
    public Map<String, Field> fieldsMap;
    public List<Field> baseFields;
    public Field primaryField;
    public List<ColumnType> columnTypes;

    public ModelCache(Class<? extends BaseModel> modelClazz) {
        this.fields = CommonTool.getClazzFields(modelClazz);
        this.fieldsMap = this.fields.stream().collect(Collectors.toMap(Field::getName, value -> value));
        this.baseFields = this.fields.stream().filter(field -> Objects.nonNull(field.getAnnotation(Column.class))).toList();
        this.primaryField = modelPrimaryField();
        this.columnTypes = modelColumnType(modelClazz);
    }

    private @Nullable Field modelPrimaryField() {
        List<Field> list = this.baseFields.stream().filter(field -> {
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (Objects.nonNull(columnAnnotation)) {
                return columnAnnotation.primary();
            }
            return false;
        }).toList();
        return list.isEmpty() ? null : list.getFirst();
    }

    private @NotNull List<ColumnType> modelColumnType(@NotNull Class<? extends BaseModel> modelClazz) {
        List<ColumnType> columnTypes = new ArrayList<>();
        Data annotation = modelClazz.getAnnotation(Data.class);
        if (Objects.nonNull(annotation)) {
            com.ws.enu.ColumnType columnType = annotation.columnType();
            for (Field field : this.baseFields) {
                columnTypes.add(ColumnTypeFactory.getInstance().create(columnType, field));
            }
        }
        return columnTypes;
    }

}
