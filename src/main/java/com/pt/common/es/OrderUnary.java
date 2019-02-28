package com.pt.common.es;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hechengchen
 * @date 2018/5/28 下午3:48
 */
public class OrderUnary implements UnaryOperator {

    private String key;
    private OrderEnum orderEnum;

    public OrderUnary(String key, OrderEnum orderEnum) {
        this.key = key;
        this.orderEnum = orderEnum;
    }

    public static OrderUnary newOrderUnay(String key, OrderEnum orderEnum) {
        Assert.hasText(key, "key is null");
        Assert.notNull(orderEnum, "order is null");
        return new OrderUnary(key, orderEnum);
    }

    @Override
    public String getUnaryOperator() {
        return "order";
    }

    @Override
    public Map<Object, Object> toMap() {
        Map<Object, Object> keyMap = new HashMap<>(1);
        Map<String, Object> orderMap = new HashMap<>(1);
        orderMap.put("order", orderEnum.toString().toLowerCase());
        keyMap.put(key, orderMap);
        return keyMap;
    }

    public enum OrderEnum {
        DESC, // 降序，从大到小
        ASC; //升序，从小到大
    }


}
