package com.pt.common.rpc.processor;

import com.pt.common.RpcConstant;
import com.pt.common.rpc.RpcProxyManager;
import com.pt.common.rpc.annotation.HttpHeaderParam;
import com.pt.common.rpc.annotation.HttpParam;
import com.pt.common.rpc.annotation.RpcProxyMethod;
import com.pt.common.rpc.exception.RpcConfigException;
import com.pt.common.rpc.ha.HaStrategy;
import com.pt.common.rpc.ha.HaStrategyFactory;
import com.pt.common.rpc.lb.LbStrategy;
import com.pt.common.rpc.lb.LbStrategyFactory;
import com.pt.common.rpc.protocol.RpcRequest;
import com.pt.common.rpc.protocol.RpcResponse;
import com.pt.common.rpc.serializer.RpcSerializer;
import com.pt.common.utils.SpanIdGenerator;
import com.pt.common.utils.SpringUtils;
import com.pt.common.utils.UrlUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 处理服务信息是配置在本地的情况，包括服务url，负载均衡策略存储在本地
 *
 * @author hechengchen
 * @date 2018/4/20 上午11:56
 */
public class LocalRpcProcessor extends AbstractRpcProcessor {


    /**
     * 缓存RpcProxyMethod中host的占位符和properties中url的值得对应关系
     */
    private Map<String, String> hostUrlCacheMap = Maps.newHashMap();

    /**
     * 获取默认的轮询策略
     */
    private LbStrategy rrLbStrategy = LbStrategyFactory.getDefaultLbStrategy();

    /**
     * 多个urls和unique url path map
     */
    private Map<String, String> urlsPathMap = Maps.newConcurrentMap();

    /**
     * 多个urls和host（ip+port）map
     */
    private Map<String, List<String>> urlsHostListMap = Maps.newConcurrentMap();

    /**
     * 获取默认的高可用策略
     */
    private HaStrategy haStrategy = HaStrategyFactory.getDefaultHaStrategy();


    @Override
    public RpcRequest getRpcRequest(String protocolName, String serializerName, RpcProxyMethod
            rpcProxyMethod, Parameter[] params, Object[] args) {

        // 获取普通入参Map
        Map<String, Object> requestParamMap = getParamsMap(params, args);

        // 获取被注解的入参Map
        Map<String, Map<String, Object>> annoRequestParamMap = getAnnoParamMap(params, args);

        addTraceRelatedHeader(annoRequestParamMap);

        RpcProxyMethod rpcMethod = rpcProxyMethod;

        String urlHolder = rpcMethod.host();

        // 将占位符替换为application.properies中配置值
        String fillParamsUrls = parseRpcPlaceholder(urlHolder);
        String targetUrl = assembleTargetUrl(fillParamsUrls);
        RpcRequest rpcRequest = new RpcRequest.Builder().urls(fillParamsUrls).targetUrl
                (targetUrl).method(rpcMethod.method()).param(requestParamMap).annotationedParam
                (annoRequestParamMap).serializerName(serializerName).connTimeout(rpcMethod
                .connectTimeount()).readTimeout(rpcMethod.readTimeout()).retryCount(rpcMethod
                .retryCount()).build();
        return rpcRequest;
    }

    @Override
    public RpcResponse getRpcResponse(String protocolName, String serializerName, Type returnType) {
        RpcSerializer serializer = RpcProxyManager.getRpcSerialize(serializerName);
        RpcResponse rpcResponse = new RpcResponse.Builder().protocolName(protocolName)
                .rpcSerializer(serializer).returnType(returnType).build();
        return rpcResponse;
    }

    @Override
    protected void doHa(String targetUrl, String urls) {
        haStrategy.doHa(UrlUtils.getHostPortByUrlStr(targetUrl), urlsHostListMap.get(urls));
    }

    private void addTraceRelatedHeader(Map<String, Map<String, Object>> annoRequestParamMap) {
        String traceId = MDC.get(RpcConstant.DIDI_HEADER_RID);
        if (!StringUtils.isEmpty(traceId)) {
            Map<String, Object> headParams = annoRequestParamMap.get(HttpHeaderParam.class.getSimpleName());
            if (CollectionUtils.isEmpty(headParams)) {
                headParams = Maps.newHashMap();
                annoRequestParamMap.put(HttpHeaderParam.class.getSimpleName(), headParams);
            }
            headParams.put(RpcConstant.DIDI_HEADER_RID, traceId);
            headParams.put(RpcConstant.DIDI_HEADER_SPANID, SpanIdGenerator.getSpanId());//生成cspanid
        }
    }

    /**
     * 将注解中placeholder替换为spring中的配置
     *
     * @param placeholderKey placeholder
     * @return spring对对应placeholder的配置
     */
    private String parseRpcPlaceholder(String placeholderKey) {
        String value = placeholderKey;
        if (StringUtils.isEmpty(placeholderKey)) {
            return placeholderKey;
        }
        if (placeholderKey.startsWith("${") && placeholderKey.endsWith("}")) {
            String realKey = placeholderKey.substring(2, placeholderKey.length() - 1);
            String placeHolderValue = null;
            if ((placeHolderValue = hostUrlCacheMap.get(realKey)) == null) {
                placeHolderValue = SpringUtils.getSpringProperty(realKey);
                hostUrlCacheMap.put(realKey, placeHolderValue);
            }
            if (placeHolderValue != null) {
                value = placeHolderValue;
            }
        }
        return value;
    }

    /**
     * 将普通参数组装为参数名:参数值的kv形式，不包含Annotation的参数
     *
     * @param params 参数声明
     * @param args   参数值
     * @return 参数名：参数值map
     */
    private Map<String, Object> getParamsMap(Parameter[] params, Object[] args) {

        Map<String, Object> requestParamMap = Maps.newHashMap();

        if (ArrayUtils.isEmpty(params)) {
            return requestParamMap;
        }

        for (int i = 0; i < params.length && i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }
            Parameter tempParam = params[i];
            Annotation[] annotations = tempParam.getAnnotations();
            if (ArrayUtils.isEmpty(annotations)) {
                requestParamMap.put(tempParam.getName(), args[i]);
            }
        }
        return requestParamMap;
    }

    /**
     * 将注解的参数组装为注解名：参数名：参数值的形式
     * 带注解的参数必须为Map<String, Object>形式，其中key为参数名，value为参数值。
     * 1）无法直接取得方法申明中的参数名，java8支持，但需要启动时加 -paramters参数，兼容性不好
     * 2）无法在注解中加value属性，因为此处各个协议定义多个Annotation，所以无法进行取值
     *
     * @param params 参数声明
     * @param args   参数值
     * @return 注解名：参数名：参数值map
     */
    private Map<String, Map<String, Object>> getAnnoParamMap(Parameter[] params, Object[] args) {
        Map<String, Map<String, Object>> allAnnoRequestParamMap = Maps.newHashMap();
        if (ArrayUtils.isEmpty(params)) {
            return allAnnoRequestParamMap;
        }

        for (int i = 0; i < params.length && i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }
            Parameter tempParam = params[i];
            Annotation[] annotations = tempParam.getAnnotations();
            if (ArrayUtils.isEmpty(annotations)) {
                continue;
            }
            boolean isArgMap = args[i] instanceof Map;

            for (Annotation annotation : annotations) {
                String annoName = annotation.annotationType().getSimpleName();
                Map<String, Object> singleAnnoedReqParamMap;
                if ((singleAnnoedReqParamMap = allAnnoRequestParamMap.get(annoName)) == null) {
                    singleAnnoedReqParamMap = Maps.newHashMap();
                    allAnnoRequestParamMap.put(annoName, singleAnnoedReqParamMap);
                }
                if (isArgMap) {
                    for (Map.Entry<String, Object> originEntity : ((Map<String, Object>) args[i])
                            .entrySet()) {
                        singleAnnoedReqParamMap.put(originEntity.getKey(), originEntity.getValue());
                    }
                } else {
                    String annoValue = null;
                    if (annotation instanceof HttpHeaderParam) {
                        annoValue = ((HttpHeaderParam) annotation).value();
                    } else if (annotation instanceof HttpParam) {
                        annoValue = ((HttpParam) annotation).value();
                    }
                    if (annoValue == null) {
                        throw new RpcConfigException("field:" + tempParam.getName() + "'s " +
                                "annotation:" + annoName + "has null value");
                    }
                    singleAnnoedReqParamMap.put(annoValue, args[i]);
                }
            }
        }
        return allAnnoRequestParamMap;
    }


    /**
     * 根据负载均衡策略选出host，然后拼接接口路径
     *
     * @param urls 用户配置的多个url
     * @return 本次请求的目标路径
     */
    private String assembleTargetUrl(String urls) {

        // 将多个urls string处理为唯一的path和host list
        handleUrlsForHostAndPath(urls);
        // 获取请求路径
        String requestPath = urlsPathMap.get(urls);
        // 获取请求主机
        String electedHost = rrLbStrategy.electHost(urlsHostListMap.get(urls));
        // 连接host和路径
        return UrlUtils.concatHostAndPath(electedHost, requestPath);
    }

    /**
     * 将多个urls string解析为hostList和唯一的path
     *
     * @param urls 配置的多个urls string
     */
    private void handleUrlsForHostAndPath(String urls) {
        if (urlsPathMap.containsKey(urls) && urlsHostListMap.containsKey(urls)) {
            return;
        }
        String[] urlArray = urls.split(LbStrategy.URL_SPLIT);
        Set<String> pathSet = Sets.newHashSet();
        Set<String> hostSet = Sets.newHashSet();
        for (String urlStr : urlArray) {
            URL url = UrlUtils.uriFormat(urlStr);
            if (StringUtils.isEmpty(url.getQuery())) {
                pathSet.add(url.getPath());
            } else {
                pathSet.add(url.getPath() + "?" + url.getQuery());
            }
            hostSet.add(url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" +
                    url.getPort() : ""));
        }
        if (pathSet.size() != 1) {
            throw new RpcConfigException("urls:" + urls + " doesn't contain unique url path");
        }
        urlsPathMap.put(urls, pathSet.toArray(new String[0])[0]);
        urlsHostListMap.put(urls, Lists.newArrayList(hostSet));
    }

}
