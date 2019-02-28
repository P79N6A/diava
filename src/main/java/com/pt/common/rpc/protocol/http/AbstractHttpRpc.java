package com.pt.common.rpc.protocol.http;

import com.pt.common.rpc.annotation.HttpHeaderParam;
import com.pt.common.rpc.annotation.HttpParam;
import com.pt.common.rpc.serializer.RpcSerializer;
import com.pt.common.utils.TypeUtils;
import com.pt.common.utils.UrlUtils;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;

/**
 * @author hechengchen
 * @date 2018/4/16 下午7:55
 */
public abstract class AbstractHttpRpc implements HttpRpc {

    @Override
    public HttpRequestBase prepareRequest(String url, Map<String, Object> params, Map<String,
            Map<String, Object>> annoParams, RpcSerializer rpcSerializer) {

        Set<String> urlParamKeys = UrlUtils.getUrlPathParam(url);

        // 将HttpParam注解的参数放入普通参数中
        Map<String, Object> mergeParams = mergeParamAndAnnoParam(params, annoParams);

        // 处理url参数
        String paramUrl = doPrepareUrl(url, urlParamKeys, mergeParams);

        // 处理HttpRequest，不同类型，get post等自行实现
        HttpRequestBase httpRequestBase = doPrepareRequest(paramUrl, mergeParams, annoParams,
                urlParamKeys, rpcSerializer);

        // 处理http header
        doPrepareHeader(httpRequestBase, mergeParams, annoParams);

        // 默认为短连接
        httpRequestBase.addHeader("Connection", "close");
        return httpRequestBase;
    }


    private Map<String, Object> mergeParamAndAnnoParam(Map<String, Object> params, Map<String,
            Map<String, Object>> annoParams) {
        if (MapUtils.isEmpty(annoParams) || annoParams.get(HttpParam.class.getSimpleName()) ==
                null) {
            return params;
        }
        Map<String, Object> mergeMap;
        if (MapUtils.isEmpty(params)) {
            params = Maps.newHashMap();
            params.put("arg0", mergeMap = Maps.newHashMap());
        } else if (params.size() == 1 && params.keySet().toArray()[0].equals("arg0")) {
            Object firstValue = params.values().toArray()[0];
            if (firstValue != null && firstValue instanceof Map) {
                mergeMap = (Map<String, Object>) firstValue;
            } else {
                mergeMap = params;
            }
        } else {
            mergeMap = params;
        }

        annoParams.get(HttpParam.class.getSimpleName()).forEach((key, value) -> {
            mergeMap.put(key, value);
        });
        return params;
    }


    /**
     * 根据注解处理http header
     *
     * @param httpRequestBase http request
     * @param params          rpc普通参数
     * @param annoParams      rpc注解参数
     */
    protected void doPrepareHeader(HttpRequestBase httpRequestBase, Map<String, Object> params,
                                   Map<String, Map<String, Object>> annoParams) {
        if (httpRequestBase == null || MapUtils.isEmpty(annoParams)) {
            return;
        }
        if (MapUtils.isNotEmpty(annoParams)) {
            annoParams.entrySet().stream().filter(stringMapEntry -> stringMapEntry.getKey()
                    .equals(HttpHeaderParam.class.getSimpleName())).forEach(stringMapEntry -> {
                if (stringMapEntry.getValue() != null) {
                    stringMapEntry.getValue().entrySet().forEach(headerEntity -> {
                        httpRequestBase.addHeader(headerEntity.getKey(), headerEntity.getValue()
                                .toString());
                    });
                }
            });
        }
    }

    /**
     * 处理http body，put、post需要，get不需要
     *
     * @param params        rpc普通参数
     * @param annoParams    rpc注解参数
     * @param excludeParams 不需要序列化的参数
     * @param rpcSerializer 序列化组件
     * @return HttpEntity http body的封装
     */
    protected HttpEntity doPrepareBody(Map<String, Object> params, Map<String, Map<String,
            Object>> annoParams, Set<String> excludeParams, RpcSerializer rpcSerializer) {
        HttpEntity httpEntity = null;
        if (!MapUtils.isEmpty(params)) {
            if (params.size() == 1) {
                Object paramObj = params.values().toArray()[0];
                if ((!TypeUtils.isSimpleObject(paramObj)) || (paramObj instanceof String &&
                        (paramObj.toString().startsWith("{") || paramObj.toString().startsWith
                                ("[")))) {
                    httpEntity = rpcSerializer.requestParamFormat(paramObj, excludeParams);
                } else {
                    httpEntity = rpcSerializer.requestParamFormat(params, null);
                }
            } else {
                httpEntity = rpcSerializer.requestParamFormat(params, null);
            }
        }
        return httpEntity;
    }

    /**
     * 处理url
     *
     * @param url         rpc url
     * @param urlParamSet url参数set
     * @param params      参数params，填充url中的参数
     * @return 参数化后的url
     */
    protected String doPrepareUrl(String url, Set<String> urlParamSet, Map<String, Object> params) {

        String paramUrl = url;
        if (CollectionUtils.isEmpty(urlParamSet) || MapUtils.isEmpty(params)) {
            return paramUrl;
        }

        Map<String, Object> urlParamValueMap;
        if (params.size() == 1) {
            Object paramObj = params.values().toArray()[0];
            urlParamValueMap = paramObj instanceof Map ? (Map<String, Object>) paramObj :
                    TypeUtils.getAllFieldWithValue(paramObj);
        } else {
            urlParamValueMap = params;
        }
        for (String paramKey : urlParamSet) {
            Object objValue = urlParamValueMap.get(paramKey);
            String value = objValue == null ? "" : objValue.toString();
            paramUrl = paramUrl.replace("$[" + paramKey + "]", value);
        }
        return paramUrl;
    }

    /**
     * 各个http method的个性化http request处理
     *
     * @param url           参数化后的rpc url
     * @param params        普通通rpc参数
     * @param annoParams    注解rpc参数
     * @param excludeParams 不序列化的参数
     * @param rpcSerializer 序列化组件
     * @return HttpRequest
     */
    protected abstract HttpRequestBase doPrepareRequest(String url, Map<String, Object> params,
                                                        Map<String, Map<String, Object>>
                                                                annoParams, Set<String>
                                                                excludeParams, RpcSerializer
                                                                rpcSerializer);


}
