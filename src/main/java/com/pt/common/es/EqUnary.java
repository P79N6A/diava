package com.pt.common.es;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hechengchen
 * @date 2018/5/23 下午8:56
 */
public class EqUnary implements UnaryOperator {


    private String key;
    private Object value;

    private EqUnary(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public static UnaryOperator newEqUnary(String key, Object value) {
        return new EqUnary(key, value);
    }


    @Override
    public Map<Object, Object> toMap() {
        Map<Object, Object> termMap = new HashMap<>(1);
        Map<Object, Object> eqMap = new HashMap<>(1);
        termMap.put("term", eqMap);
        eqMap.put(key, value);
        return termMap;
    }

    @Override
    public String getUnaryOperator() {
        return "=";
    }

}
