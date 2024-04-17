package com.ws.base.result;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.*;
import com.ws.enu.CommonErrorInfo;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author GSF
 * <p>响应数据格式</p>
 */
@Data
public class ResultBody<T> implements Serializable {

    /**
     * <p>响应结果</p>
     **/
    T data;

    /**
     * <p>响应代码</p>
     **/
    String code;

    /**
     * <p>提示信息</p>
     **/
    String message;

    boolean status;

    public ResultBody() {
    }

    /**
     * @param data    响应数据
     * @param message 提示信息
     * @return ResultBody
     **/
    public static <T> ResultBody<T> build(T data, String code, String message, boolean status) {
        ResultBody<T> resultBody = new ResultBody<>();
        resultBody.setData(data);
        resultBody.setCode(code);
        resultBody.setMessage(message);
        resultBody.setStatus(status);
        return resultBody;
    }

    /**
     * <p>成功</p>
     *
     * @return ResultBody
     **/
    public static <T> ResultBody<T> success() {
        return ResultBody.success(null);
    }

    /**
     * <p>成功</p>
     *
     * @param data 响应数据
     * @return ResultBody
     **/
    public static <T> ResultBody<T> success(T data) {
        return ResultBody.success(data, CommonErrorInfo.SUCCESS.getResultMsg());
    }

    /**
     * <p>成功</p>
     *
     * @param data    响应数据
     * @param message 提示信息
     * @return ResultBody
     **/
    public static <T> ResultBody<T> success(T data, String message) {
        return ResultBody.success(data, CommonErrorInfo.SUCCESS.getResultCode(), message);
    }

    /**
     * <p>成功</p>
     *
     * @param data    响应数据
     * @param message 提示信息
     * @return ResultBody
     **/
    public static <T> ResultBody<T> success(T data, String code, String message) {
        return ResultBody.build(data, code, message, true);
    }

    /**
     * <p>警告</p>
     *
     * @return ResultBody
     **/
    public static <T> ResultBody<T> warn() {
        return ResultBody.warn(null);
    }

    /**
     * <p>警告</p>
     *
     * @return ResultBody
     **/
    public static <T> ResultBody<T> warn(String message) {
        return ResultBody.warn(null, message);
    }

    /**
     * <p>警告</p>
     *
     * @return ResultBody
     **/
    public static <T> ResultBody<T> warn(T data) {
        return ResultBody.warn(data, CommonErrorInfo.SUCCESS.getResultMsg());
    }

    /**
     * <p>警告</p>
     *
     * @param data    响应数据
     * @param message 提示信息
     * @return ResultBody
     **/
    public static <T> ResultBody<T> warn(T data, String message) {
        return ResultBody.warn(data, "-1", message);
    }

    /**
     * <p>警告</p>
     *
     * @param data    响应数据
     * @param code    响应码
     * @param message 提示信息
     * @return ResultBody
     **/
    public static <T> ResultBody<T> warn(T data, String code, String message) {
        return ResultBody.build(data, code, message, false);
    }

    /**
     * <p>失败</p>
     *
     * @param message 提示信息
     * @return ResultBody
     **/
    public static <T> ResultBody<T> error(String message) {
        return ResultBody.error(CommonErrorInfo.ERROR.getResultCode(), message);
    }

    public static <T> ResultBody<T> error(HttpStatus httpStatus) {
        return ResultBody.error(String.valueOf(httpStatus.value()), httpStatus.getReasonPhrase());
    }

    public static <T> ResultBody<T> error(CommonErrorInfo commonErrorInfo) {
        return ResultBody.error(commonErrorInfo.getResultCode(), commonErrorInfo.getResultMsg());
    }

    /**
     * <p>失败</p>
     *
     * @param code    响应码
     * @param message 提示信息
     * @return ResultBody
     **/
    public static <T> ResultBody<T> error(String code, String message) {
        return ResultBody.build(null, code, message, false);
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

    public Map<String, Object> toMap() {
        List<Field> fields = new ArrayList<>();
        Class<?> clazz = this.getClass();
        while (clazz != null) {
            fields.addAll(List.of(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        Map<String, Object> map = new HashMap<>(fields.size());
        Logger log = LoggerFactory.getLogger(this.getClass());
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                log.error("无法访问的字段", e);
            }
        }
        return map;
    }

}