package com.pt.common.rpc.protocol.http;

import com.pt.common.rpc.exception.RpcRuntimeException;
import com.pt.common.rpc.serializer.RpcSerializer;
import com.pt.common.utils.TypeUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * http get实现，所有参数需要拼装到url后面
 *
 * @author hechengchen
 * @date 2017/10/22 下午5:41
 */
public class GetHttpRpc extends AbstractHttpRpc {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetHttpRpc.class);

    @Override
    public HttpRequestBase doPrepareRequest(String url, Map<String, Object> params, Map<String,
            Map<String, Object>> annoParams, Set<String> excludeParams, RpcSerializer
            rpcSerializer) {
        String targetUrl = url;
        if (MapUtils.isEmpty(params)) {
            return new HttpGet(targetUrl);
        }
        String requestParamUrl = map2UrlParam(params, excludeParams);
        if (StringUtils.isNotEmpty(requestParamUrl)) {
            if (targetUrl.contains("?")) {
                targetUrl += targetUrl.endsWith("&") ? requestParamUrl : ("&" + requestParamUrl);
            } else {
                targetUrl += "?" + requestParamUrl;
            }
        }
        return new HttpGet(targetUrl);
    }

    /**
     * 将参数加到url中
     *
     * @param params     rpc普通参数
     * @param excludeKey 不需要加到url?后面的参数
     * @return 参数化的url
     */
    private String map2UrlParam(Map<String, Object> params, Set<String> excludeKey) {

        StringBuilder urlParams = new StringBuilder();

        // 如果参数只有一个，则判断是primitive还是对象
        // 则检测该类中的所有field是不是原生类型
        //
        if (params.size() == 1) {
            String key = params.keySet().toArray()[0].toString();
            Object value = params.get(key);
            // 如果是1个primitive，则直接转换。
            if (TypeUtils.isSimpleObject(value)) {
                urlParams.append(key).append("=").append(value);
            } else if (TypeUtils.isContainerObject(value)) {
                // 暂不支持Collection类型的参数
                if (value instanceof Collection) {
                    throw new RpcRuntimeException("http get method don't support collection " +
                            "param" + " only");
                }
                if (value instanceof Map && MapUtils.isNotEmpty((Map) value)) {
                    Map valueMap = (Map) value;
                    for (Object objKey : valueMap.keySet()) {
                        if (objKey != null && valueMap.get(objKey) != null && (CollectionUtils
                                .isEmpty(excludeKey) || !excludeKey.contains(objKey))) {
                            urlParams.append(objKey).append("=").append(valueMap.get(objKey))
                                    .append("&");
                        }
                    }
                }
            } else {
                // 则检测该类中的所有field是不是原生类型,然后将该类中每个不为空的值拼接到url

                List<Field> fields = TypeUtils.getAllFields(value.getClass());

                if (!CollectionUtils.isEmpty(fields)) {
                    for (Field f : fields) {
                        if (CollectionUtils.isEmpty(excludeKey) || !excludeKey.contains(f.getName
                                ())) {
                            Object fieldValue = null;
                            f.setAccessible(true);
                            try {
                                fieldValue = f.get(value);
                            } catch (IllegalAccessException e) {
                                LOGGER.error(e.getMessage(), e);
                                throw new RpcRuntimeException(e.getMessage(), e);
                            }
                            if (fieldValue == null) {
                                continue;
                            }
                            if (!TypeUtils.isSimpleObject(fieldValue)) {
                                throw new RpcRuntimeException("get request contains complex " +
                                        "object " + "type param:" + f.getName());
                            }
                            urlParams.append(f.getName()).append("=").append(fieldValue).append
                                    ("&");
                        }

                    }
                }
            }
        } else {
            // 如果多个参数，则检测每个参数是不是都为原生类型
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                if (!TypeUtils.isSimpleObject(entry.getValue())) {
                    throw new RpcRuntimeException("get request contains complex object type " +
                            "param:" + entry.getKey());

                }
                urlParams.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        String urlParamStr = urlParams.toString();
        if (urlParamStr.endsWith("&")) {
            urlParamStr = urlParamStr.substring(0, urlParamStr.length() - 1);
        }
        return urlParamStr;
    }
}
