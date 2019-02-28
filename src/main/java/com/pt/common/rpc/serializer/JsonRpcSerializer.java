package com.pt.common.rpc.serializer;

import com.pt.common.RpcConstant;
import com.pt.common.rpc.exception.RpcRuntimeException;
import com.pt.common.utils.JsonUtils;
import com.pt.common.utils.TypeUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * @author hechengchen
 * @date 2017/10/22 上午10:09
 */
public class JsonRpcSerializer implements RpcSerializer {

    private static final Logger logger = LoggerFactory.getLogger(JsonRpcSerializer.class);

    @Override
    public HttpEntity requestParamFormat(Object requestParam, Set<String> excludeKey) {
        Map<String, Object> requestParamMap = null;
        String jsonStr = null;
        if (requestParam instanceof Map && (requestParamMap = (Map<String, Object>) requestParam)
                .containsKey(RpcConstant.ASSIGN_HTTP_BODY)) {
            jsonStr = requestParamMap.get(RpcConstant.ASSIGN_HTTP_BODY) == null ? null :
                    requestParamMap.get(RpcConstant.ASSIGN_HTTP_BODY).toString();
        }
        if (jsonStr == null) {
            Object realSeriObj = requestParam;
            boolean isRequestParamMap = requestParamMap == null ? false : true;
            if (!CollectionUtils.isEmpty(excludeKey)) {
                for (String key : excludeKey) {
                    if (isRequestParamMap) {
                        ((Map) realSeriObj).remove(key);
                    } else {
                        TypeUtils.setFieldValue(realSeriObj, key, null);
                    }

                }
            }
            jsonStr = requestParam instanceof String ? requestParam + "" : JsonUtils.toJson
                    (realSeriObj);
        }
        try {
            return new ByteArrayEntity(jsonStr.getBytes("utf8"), ContentType.APPLICATION_JSON);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            throw new RpcRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Object responseParamFormat(Type responseType, byte[] originResp) {
        if (originResp == null || originResp.length == 0) {
            return null;
        }
        String respStr = null;
        try {
            respStr = new String(originResp, "utf8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            respStr = new String(originResp);
        }
        if ("".equals(respStr)) {
            try {
                return responseType instanceof Class ? ((Class) responseType).newInstance() : null;
            } catch (Exception e) {
                return null;
            }
        }
        Object object = JsonUtils.fromJson(respStr, new TypeReference<Object>() {
            @Override
            public Type getType() {
                return responseType;
            }
        });
        return responseType.getTypeName().equals(String.class.getTypeName()) ? respStr : object;

    }
}
