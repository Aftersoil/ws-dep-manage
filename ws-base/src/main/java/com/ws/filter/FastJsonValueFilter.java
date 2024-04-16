package com.ws.filter;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.ws.annotation.FastJsonFilter;
import com.ws.enu.FilterType;
import com.ws.tool.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * <p>fastjson过滤敏感数据实现,可以放在响应拦截使用</p>
 *
 * @author GSF
 */
@Slf4j
public class FastJsonValueFilter implements ValueFilter {

    @Override
    public Object process(Object object, String name, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(name);
            value = getFormatValue(field, value);
        } catch (NoSuchFieldException e) {
            return value;
        } catch (Exception e) {
            log.error(object + "解析json失败", e);
            return value;
        }
        return value;
    }

    @Override
    public Object apply(Object object, String name, Object value) {
        return ValueFilter.super.apply(object, name, value);
    }

    private static Object getFormatValue(@NotNull Field field, Object value) {
        if (field.isAnnotationPresent(FastJsonFilter.class)) {
            value = format(field.getAnnotation(FastJsonFilter.class).filterType(), value);
        }
        return value;
    }

    @Nullable
    private static Object format(@NotNull FilterType filterType, Object object) {
        if (filterType.equals(FilterType.NULL) || Objects.isNull(object)) {
            return null;
        }
        if (!(object instanceof String value)) {
            log.error(object + "该字段不是字符串类型");
            return null;
        }
        if (StringUtil.isEmpty(value)) {
            return null;
        }
        switch (filterType) {
            case NAME -> {
                if (Validator.isChineseName(value)) {
                    value = DesensitizedUtil.chineseName(value);
                } else {
                    value = StringUtil.concat(value.substring(0, 1), "**");
                }
            }
            case PASSWORD -> {
                value = "******";
            }
            case PHONE -> {
                if (PhoneUtil.isPhone(value)) {
                    value = DesensitizedUtil.mobilePhone(value);
                } else {
                    value = null;
                }
            }
            case EMAIL -> {
                if (Validator.isEmail(value)) {
                    value = DesensitizedUtil.email(value);
                } else {
                    value = null;
                }
            }
            case ID_CARD -> {
                if (IdcardUtil.isValidCard(value)) {
                    value = DesensitizedUtil.idCardNum(value, 1, 2);
                } else {
                    value = null;
                }
            }
            case ADDRESS -> {
                value = DesensitizedUtil.address(value, 8);
            }
            case ACCOUNT -> {
                if (value.length() >= 2) {
                    value = StrUtil.hide(value, 1, 1);
                } else {
                    value = null;
                }
            }
        }
        return value;
    }

}
