package com.pt.common.es;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: gao.xing
 * @Date: 2018/6/28 15:21
 * @Description:
 */
public class ExistsUnary implements UnaryOperator {


    private String key;
    private Object value;

    private ExistsUnary(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public static UnaryOperator newExistsUnary(String key, Object value) {
        return new ExistsUnary(key, value);
    }

    @Override
    public String getUnaryOperator() {
        return "exists";
    }

    @Override
    public Map<Object, Object> toMap() {
        Map<Object, Object> existsMap = new HashMap<>(1);
        Map<Object, Object> eqMap = new HashMap<>(1);
        existsMap.put("exists", eqMap);
        eqMap.put(key, value);
        return existsMap;
    }
}
