package com.pt.common.rpc.serializer;

import com.pt.common.rpc.exception.RpcRuntimeException;
import com.pt.common.utils.TypeUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 适配订单系统需求，入参是form，出参是json
 *
 * @author hechengchen
 * @date 2018/3/9 下午7:36
 */
public class FormJsonRpcSerializer implements RpcSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormJsonRpcSerializer.class);

    private JsonRpcSerializer jsonRpcSerializer = new JsonRpcSerializer();

    @Override
    public HttpEntity requestParamFormat(Object requestParam, Set<String> excludeKey) {
        HttpEntity httpEntity = null;
        if (requestParam == null) {
            return httpEntity;
        }
        LinkedHashMap<String, Object> paramMap = Maps.newLinkedHashMap();
        if (!TypeUtils.isMapObject(requestParam)) {
            List<Field> fields = TypeUtils.getAllFields(requestParam.getClass());
            if (!CollectionUtils.isEmpty(fields)) {
                for (Field field : fields) {
                    // 过滤transient字段
                    if (field.getModifiers() != 130 && (excludeKey == null || !excludeKey
                            .contains(field.getName()))) {
                        field.setAccessible(true);
                        Object objValue = null;
                        try {
                            objValue = field.get(requestParam);
                        } catch (IllegalAccessException e) {
                            throw new RpcRuntimeException("can't fill url param in http post " +
                                    "with" + " " + "object, " + "field:" + field.getName());
                        }
                        if (objValue != null) {
                            paramMap.put(field.getName(), objValue);
                        }
                    }
                }
            }
        } else {
            paramMap = ((LinkedHashMap<String, Object>) requestParam);
        }

        List<NameValuePair> paramList = Lists.newArrayList();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            if (StringUtils.isNotEmpty(entry.getKey()) && entry.getValue() != null) {
                paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
        }
        try {
            httpEntity = new UrlEncodedFormEntity(paramList, "utf8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return httpEntity;
    }

    @Override
    public Object responseParamFormat(Type responseType, byte[] originResp) {
        return jsonRpcSerializer.responseParamFormat(responseType, originResp);
    }
}
