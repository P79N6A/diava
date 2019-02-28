package com.pt.common.rpc.protocol.http;

import com.pt.common.rpc.serializer.RpcSerializer;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.Map;

/**
 * http不同方法rpc时的参数处理
 *
 * @author hechengchen
 * @date 2017/10/22 下午5:40
 */
public interface HttpRpc {

    /**
     * 根据代理接口的方法生成http请求的参数
     *
     * @param url           目标url
     * @param params        代理接口方法的入参
     * @oaran annoParams    带注解的入参，需要特殊处理
     * @param rpcSerializer 序列化工具
     * @return http请求参数
     */
    HttpRequestBase prepareRequest(String url, Map<String, Object> params, Map<String,
            Map<String, Object>> annoParams, RpcSerializer rpcSerializer);

}
