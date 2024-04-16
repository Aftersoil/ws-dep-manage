package com.ws.base.model;

import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.*;
import com.ws.annotation.Column;
import com.ws.tool.CacheTool;
import com.ws.tool.RequestUtil;
import com.ws.tool.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author GSF
 * <p>BaseModel</p>
 */
public class BaseModel implements Serializable {

    public List<Field> modelFields() {
        return CacheTool.getModelFields(this.getClass());
    }

    public Map<String, Field> modelFieldsMap() {
        return this.modelFields().stream().collect(Collectors.toMap(Field::getName, value -> value));
    }

    public void setModelAnyValueByFieldName(String name, Object value) {
        this.setModelValuesFromMapByFieldName(new HashMap<>() {{
            put(name, value);
        }});
    }

    public Object modelAnyValueByFieldName(String name) {
        return this.toMap().get(name);
    }

    public @Nullable Field modelPrimaryField() {
        return CacheTool.getModelPrimaryField(this.getClass());
    }

    public void setModelValuesFromMapByFieldName(Map<String, Object> map) {
        if (Objects.nonNull(map)) {
            Map<String, Field> fields = this.modelFieldsMap();
            fields.forEach((fieldName, field) -> {
                if (Objects.nonNull(map.get(fieldName))) {
                    field.setAccessible(true);
                    try {
                        field.set(this, Convert.convert(field.getType(), map.get(fieldName)));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    public void setModelValuesFromMapByFieldNameWithTitle(Map<String, Object> map) {
        if (Objects.nonNull(map)) {
            Map<String, Field> fields = this.modelFieldsMap();
            fields.forEach((fieldName, field) -> {
                String title = fieldName;
                Column annotation = field.getAnnotation(Column.class);
                if (Objects.nonNull(annotation) && StringUtil.isNotEmpty(annotation.title())) {
                    title = annotation.title();
                }
                if (Objects.nonNull(map.get(title))) {
                    field.setAccessible(true);
                    try {
                        field.set(this, Convert.convert(field.getType(), map.get(title)));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    public void setModelValuesFromRequest(HttpServletRequest request) throws IOException {
        this.setModelValuesFromMapByFieldName(this.setModelValuesFromRequestBodyParam(request));
    }

    public void setModelValuesFromModelJsonStr(String json) {
        this.setModelValuesFromMapByFieldName(JSON.parseObject(json));
    }

    public boolean fieldIsExist(String name) {
        return this.modelFieldsMap().containsKey(name);
    }

    public boolean fieldIsExist(Field field) {
        return this.modelFields().contains(field);
    }

    /**
     * <p>兴许有重写需求</p>
     *
     * @param request 请求
     * @return Map<String, Object> 请求参数
     **/
    public Map<String, Object> setModelValuesFromRequestBodyParam(HttpServletRequest request) throws IOException {
        return RequestUtil.getRequestParams(request);
    }

    public Map<String, Object> toMap() {
        List<Field> fields = this.modelFields();
        Map<String, Object> map = new HashMap<>(fields.size());
        fields.forEach(field -> {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return map;
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }

    public String toJson(SerializerFeature... features) {
        return JSON.toJSONString(this, features);
    }

    public String toJson(ValueFilter filter, SerializerFeature... features) {
        return JSON.toJSONString(this, filter, features);
    }

    public String toJson(SerializeConfig serializeConfig, SerializerFeature... features) {
        return JSON.toJSONString(this, serializeConfig, features);
    }

    public String toJson(SerializeConfig serializeConfig, SerializeFilter filter, SerializerFeature... features) {
        return this.toJson(serializeConfig, new SerializeFilter[]{filter}, features);
    }

    public String toJson(SerializeConfig serializeConfig, SerializeFilter[] filter, SerializerFeature... features) {
        return JSON.toJSONString(this, serializeConfig, filter, features);
    }

    public String toJsonDateFormat(String format) {
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.put(Date.class, new SimpleDateFormatSerializer(format));
        return this.toJson(serializeConfig);
    }

    public String toJsonDateFormat(String format, SerializerFeature... features) {
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.put(Date.class, new SimpleDateFormatSerializer(format));
        return this.toJson(serializeConfig, features);
    }

    public String toJsonDateFormat(String format, ValueFilter filter, SerializerFeature... features) {
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.put(Date.class, new SimpleDateFormatSerializer(format));
        return this.toJson(serializeConfig, filter, features);
    }

    public String toJsonyMdHms() {
        return this.toJsonDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public String toJsonyMd() {
        return this.toJsonDateFormat("yyyy-MM-dd");
    }

}
