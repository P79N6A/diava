package com.pt.common.utils;

import com.pt.common.rpc.exception.RpcRuntimeException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author hechengchen
 * @date 2017/10/22 下午6:24
 */
public class TypeUtils {


    private static final Logger LOGGER = LoggerFactory.getLogger(TypeUtils.class);

    public static boolean isSimpleObject(Object obj) {
        if (obj == null) {
            return true;
        }
        Class<?> cls = obj.getClass();
        return cls.isPrimitive() || obj instanceof String || obj instanceof Character || obj
                instanceof Boolean || obj instanceof Number;
    }


    public static boolean isCollectionObject(Object obj) {
        return obj == null ? false : obj instanceof Collection;
    }


    public static boolean isContainerObject(Object obj) {
        return obj == null ? false : (isCollectionObject(obj) || obj instanceof Map);
    }

    public static boolean isUdfObject(Object obj) {
        return !isSimpleObject(obj) && !isContainerObject(obj);
    }

    public static boolean isMapObject(Object object) {
        return object instanceof Map;
    }

    public static List<Field> getAllFields(Class clazz) {
        List<Field> fields = Lists.newArrayList();
        while (clazz != null && !clazz.getName().toLowerCase().equals("java.lang.object")) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public static Map<String, Object> getAllFieldWithValue(Object object) {
        Map<String, Object> fieldNameValueMap = Maps.newHashMap();
        if (object == null) {
            return fieldNameValueMap;
        }
        List<Field> fields = getAllFields(object.getClass());
        if (CollectionUtils.isEmpty(fields)) {
            return fieldNameValueMap;
        }
        fields.forEach(field -> {
            field.setAccessible(true);
            Object objValue = null;
            try {
                objValue = field.get(object);
            } catch (IllegalAccessException e) {
                throw new RpcRuntimeException("can't fill url param in http post with " +
                        "object, " + "field:" + field.getName());
            }
            if (objValue != null) {
                fieldNameValueMap.put(field.getName(), objValue);
            }
        });
        return fieldNameValueMap;
    }

    public static boolean setFieldValue(Object object, String fieldName, Object fieldValue) {
        try {
            BeanUtils.setProperty(object, fieldName, fieldValue);
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

}
