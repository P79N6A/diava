package com.pt.common.es;

import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hechengchen
 * @date 2018/5/24 上午11:24
 */
public class InUnary implements UnaryOperator {

    private String key;
    private Collection<Object> value;

    public InUnary(String key, Collection<Object> value) {
        this.key = key;
        this.value = value;
    }

    public static InUnary newInUnary(String key, Collection<Object> value) {
        Assert.hasText(key, "key is null");
        Assert.notEmpty(value, "value is null");
        return new InUnary(key, value);
    }

    @Override
    public Map<Object, Object> toMap() {
        Map<Object, Object> termMap = new HashMap<>(1);
        Map<Object, Object> eqMap = new HashMap<>(1);
        termMap.put("terms", eqMap);
        eqMap.put(key, value);
        return termMap;
    }

    @Override
    public String getUnaryOperator() {
        return "in";
    }
}
