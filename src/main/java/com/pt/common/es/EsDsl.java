package com.pt.common.es;

import com.pt.common.rpc.exception.RpcRuntimeException;
import com.pt.common.utils.TypeUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hechengchen
 * @date 2018/5/24 下午2:11
 */
public class EsDsl {

    private List<Map<Object, Object>> mustList = Lists.newArrayList();

    private List<Map<Object, Object>> mustNotList = Lists.newArrayList();

    private List<Map<Object, Object>> shoulddList = Lists.newArrayList();

    private List<Map<Object, Object>> sortList = Lists.newArrayList();

    private List<String> sources=Lists.newArrayList();

    private Integer from;

    private Integer size;

    private static final Integer DEFAULT_FROM = 0;
    private static final Integer DEFAULT_SIZE = 10;

    public static EsDsl newInstance() {
        return new EsDsl();
    }

    public EsDsl and(Operator operator) {
        mustList.add(operator.toMap());
        return this;
    }

    public EsDsl or(Operator operator) {
        shoulddList.add(operator.toMap());
        return this;
    }

    public EsDsl andNot(Operator operator) {
        mustNotList.add(operator.toMap());
        return this;
    }

    public EsDsl orderBy(Operator operator) {
        sortList.add(operator.toMap());
        return this;
    }

    public EsDsl fromAndSize(Integer from, Integer size) {
        this.from = from;
        this.size = size;
        return this;
    }

    public EsDsl source(Collection<String> sources) {
        this.sources.addAll(sources);
        return this;
    }

    public Map<String, Object> toDslMap() {
        Map<String, Object> resultMap = new HashMap<>(1);
        Map<String, Object> filterMap = new HashMap<>(1);
        Map<String, Object> boolMap = Maps.newHashMap();
        filterMap.put("bool", boolMap);
        if (CollectionUtils.isNotEmpty(mustList)) {
            boolMap.put("must", mustList);
        }
        if (CollectionUtils.isNotEmpty(mustNotList)) {
            boolMap.put("must_not", mustNotList);
        }
        if (CollectionUtils.isNotEmpty(shoulddList)) {
            boolMap.put("should", shoulddList);
        }
        resultMap.put("from", from == null ? DEFAULT_FROM : from);
        resultMap.put("size", size == null ? DEFAULT_SIZE : size);
        resultMap.put("filter", filterMap);
        resultMap.put("sort", sortList.size() == 1 ? sortList.get(0) : sortList);
        if (CollectionUtils.isNotEmpty(sources)) {
            resultMap.put("_source", sources);
        }
        return resultMap;
    }

    public EsDsl addEqObject(Object object) {
        Map<EsField, Object> fieldNameValueMap;
        if (object == null || (fieldNameValueMap = getEsFieldWithValue(object)) == null) {
            return this;
        }
        fieldNameValueMap.forEach((key, value) -> {
            mustList.add(EqUnary.newEqUnary(key.value(), value).toMap());
        });
        return this;
    }

    private Map<EsField, Object> getEsFieldWithValue(Object object) {
        Map<EsField, Object> fieldNameValueMap = Maps.newHashMap();
        if (object == null) {
            return fieldNameValueMap;
        }
        List<Field> fields = TypeUtils.getAllFields(object.getClass());
        if (CollectionUtils.isEmpty(fields)) {
            return fieldNameValueMap;
        }
        fields.forEach(field -> {
            EsField esFieldAnno;
            if ((esFieldAnno = field.getAnnotation(EsField.class)) != null) {
                field.setAccessible(true);
                Object objValue = null;
                try {
                    objValue = field.get(object);
                } catch (IllegalAccessException e) {
                    throw new RpcRuntimeException("can't fill url param in http post with " +
                            "object, " + "field:" + field.getName());
                }
                if (objValue != null) {
                    fieldNameValueMap.put(esFieldAnno, objValue);
                }
            }
        });
        return fieldNameValueMap;
    }


}
