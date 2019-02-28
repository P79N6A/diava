package com.pt.common.es;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hechengchen
 * @date 2018/5/24 下午7:17
 */
public class GteLteUnary implements UnaryOperator {


    private String key;
    private Object smallestValue;
    private Object biggestValue;

    public GteLteUnary(String key, Object smallestValue, Object biggestValue) {
        this.key = key;
        this.smallestValue = smallestValue;
        this.biggestValue = biggestValue;
    }

    public static GteLteUnary newGteLteUnary(String key, Object smallestValue, Object biggestValue) {
        return new GteLteUnary(key, smallestValue, biggestValue);
    }

    @Override
    public Map<Object, Object> toMap() {
        Map<Object, Object> rangeMap = new HashMap<>(1);
        Map<Object, Object> compareMap = new HashMap<>(1);
        Map<Object, Object> gteLteMap = new HashMap<>(2);
        gteLteMap.put("gte", smallestValue);
        gteLteMap.put("lte", biggestValue);
        compareMap.put(key, gteLteMap);
        rangeMap.put("range", compareMap);
        return rangeMap;
    }

    @Override
    public String getUnaryOperator() {
        return ">=<=";
    }

}
