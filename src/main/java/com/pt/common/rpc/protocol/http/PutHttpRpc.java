package com.pt.common.rpc.protocol.http;

import com.pt.common.rpc.serializer.RpcSerializer;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.Map;
import java.util.Set;

/**
 * http post实现,将方法出参转换为json，并放到http body中。
 *
 * @author hechengchen
 * @date 2017/10/22 下午5:44
 */
public class PutHttpRpc extends AbstractHttpRpc {


    @Override
    protected HttpRequestBase doPrepareRequest(String url, Map<String, Object> params,
                                               Map<String, Map<String, Object>> annoParams,
                                               Set<String> excludeParams, RpcSerializer
                                                           rpcSerializer) {
        String putUrl = url;
        HttpPut httpPut = new HttpPut(putUrl);
        HttpEntity httpEntity = doPrepareBody(params, annoParams, excludeParams, rpcSerializer);
        if (httpEntity != null) {
            httpPut.setEntity(httpEntity);
        }
        return httpPut;
    }
}
