package com.pt.common.es;

import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Asserts;

import java.util.Map;
import java.util.Objects;

/**
 * @author hechengchen
 * @date 2018/6/15 下午3:24
 */
public class MapUtil {

    public static <T> T getObject(Map<String, Object> map, String key) {
        if (MapUtils.isEmpty(map) || StringUtils.isEmpty(key)) {
            return null;
        }
        Object result = map.get(key);
        return result == null ? null : (T) result;
    }

    /**
     * 替换指定的Key
     *
     * @param map
     * @param oldKey
     * @param newKey
     * @return
     */
    public static Map<String, Object> replaceKey(Map<String, Object> map, String oldKey, String newKey) {
        if (MapUtils.isEmpty(map)) {
            return map;
        }
        if (map.containsKey(newKey)) {
            throw new IllegalArgumentException("map already has the key [" + newKey + "]");
        }
        if (map.containsKey(oldKey)) {
            Object value = map.remove(oldKey);
            map.put(newKey, value);
        }
        return map;
    }

    /**
     * 选择指定的KV对
     *
     * @param keys
     * @return
     */
    public static Map<String, Object> chooseKvs(Map<String, Object> originMap, String... keys) {
        Asserts.check(Objects.nonNull(keys) && originMap != null, "keys cannot null");
        Map<String, Object> newMap = Maps.newHashMap();
        for (String key : keys) {
            Object o = originMap.get(key);
            if (Objects.nonNull(o))
                newMap.put(key, o);
        }
        return newMap;
    }

    /**
     * 升级嵌套map中的kv对
     *
     * @param originData
     * @param key
     * @return
     */
    public static Map<String, Object> upgradeKvs(Map<String, Object> originData, String key) {
        if (originData == null) {
            return Maps.newHashMap();
        }
        if (!originData.containsKey(key)) {
            return originData;
        }
        Object o = originData.remove(key);
        if (o instanceof Map) {
            originData.putAll((Map) o);
        } else {
            originData.put(key, o);
        }
        return originData;
    }
}
