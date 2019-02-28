package com.pt.common.utils;

import com.pt.common.rpc.exception.RpcRuntimeException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author hechengchen
 * @date 2018/5/4 上午11:49
 */
public class JsonUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        //是否允许解析使用Java/C++ 样式的注释（包括'/'+'*' 和'//' 变量）。
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        //是否将允许使用非双引号属性名字
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        //是否允许单引号来包住属性名称和字符串值
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //是否允许JSON字符串包含非引号控制字符（值小于32的ASCII字符，包含制表符和换行符）。 如果该属性关闭，则如果遇到这些字符，则会抛出异常。
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        //当遇到未知属性（没有映射到属性，没有任何setter或者任何可以处理它的handler），是否应该抛出一个JsonMappingException异常。
        // 这个特性一般式所有其他处理方法对未知属性处理都无效后才被尝试，属性保留未处理状态。
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //空数组'[]'反序列化为null,处理PHP中的array为空时为'[]',不为空时为map的情形
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
    }

    /**
     * 反序列化
     *
     * @param jsonStr   Json字符串
     * @param classType 反序列化的类型
     * @return
     */
    public static <T> T fromJson(String jsonStr, Class<T> classType) {
        try {
            return objectMapper.readValue(jsonStr, classType);
        } catch (IOException e) {
            throw new RpcRuntimeException(e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String jsonStr, TypeReference typeReference) {
        try {
            return objectMapper.readValue(jsonStr, typeReference);
        } catch (IOException e) {
            throw new RpcRuntimeException("parse failed", e);
        }
    }

    /**
     * 序列化
     *
     * @param object
     * @return
     */
    public static <T> String toJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RpcRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 根据json string获取原生json解析对象
     * @param jsonStr json字符串
     * @return json原生解析对象
     */
    public static JsonNode readTree(String jsonStr) {
        try {
            return StringUtils.isEmpty(jsonStr) ? null : objectMapper.readTree(jsonStr);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RpcRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 序列化json原生解析对象
     * @param n json原生解析对象
     * @param valueType 序列化目标类型Class
     * @param <T> 序列化目标类型
     * @return 序列化结果
     */
    public static <T> T tree2Value(TreeNode n, Class<T> valueType) {
        if (n == null || valueType == null) {
            return null;
        }
        try {
            return objectMapper.treeToValue(n, valueType);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RpcRuntimeException(e.getMessage(), e);
        }
    }
    

}
