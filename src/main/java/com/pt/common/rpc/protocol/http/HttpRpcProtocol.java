package com.pt.common.rpc.protocol.http;

import com.pt.common.rpc.exception.RpcRemoteCallException;
import com.pt.common.rpc.exception.RpcRuntimeException;
import com.pt.common.rpc.protocol.RpcProtocol;
import com.pt.common.rpc.protocol.RpcRequest;
import com.pt.common.rpc.protocol.RpcResponse;
import com.pt.common.utils.HttpClientUtils;
import com.google.common.collect.Maps;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * http协议调用实现
 *
 * @author hechengchen
 * @date 2017/10/22 上午10:47
 */
public class HttpRpcProtocol implements RpcProtocol {


    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRpcProtocol.class);

    /**
     * 缓存http方法和对应实现，目前仅支持get，post
     */
    private static final Map<String, HttpRpc> httpRpcMap = Maps.newHashMap();

    /**
     * 默认httpclient
     */
    private static final HttpClient httpClient = HttpClientUtils.newCloseableHttpClient(500, -1,
            -1, -1, 0);

    static {
        httpRpcMap.put("get", new GetHttpRpc());
        httpRpcMap.put("post", new PostHttpRpc());
        httpRpcMap.put("put", new PutHttpRpc());
    }

    @Override
    public void execute(RpcRequest rpcRequest, RpcResponse rpcResponse) {

        String targetUrl = rpcRequest.getTargetUrl();
        String method = rpcRequest.getMethod();
        Map<String, Object> param = rpcRequest.getParam();
        Map<String, Map<String, Object>> annoParam = rpcRequest.getAnnotationedParam();
        int retryCount = rpcRequest.getRetryCount();

        HttpRpc httpRpc = null;
        byte[] respBytes = null;
        if ((httpRpc = httpRpcMap.get(method)) == null) {
            throw new RpcRuntimeException("can't support method:" + method + ",  url:" + targetUrl);
        }

        // 请求url，请求参数配置
        HttpRequestBase httpRequestBase = httpRpc.prepareRequest(targetUrl, param, annoParam,
                rpcRequest.getRpcSerializer());

        // 请求连接超时，读超时配置
        httpRequestBase.setConfig(HttpClientUtils.newRequestConfig(-1, rpcRequest.getConnTimeout
                (), rpcRequest.getReadTimeout()));

        HttpResponse httpResponse = null;
        Exception rpcException = null;

        // retryCount <= 0表示不重试，执行次数realRunCount = 1 + retryCount
        int realRunCount = retryCount <= 0 ? 1 : retryCount + 1;

        long rpcWaitStart = System.currentTimeMillis();

        for (int i = 0; i < realRunCount; i++) {
            try {
                httpResponse = httpClient.execute(httpRequestBase);
                rpcException = null;
                break;
            } catch (IOException e) {
                LOGGER.error("url:{} {} call fail: {}", httpRequestBase.getURI().toString(), i, e
                        .getMessage(), e);
                rpcException = e;
            }
        }
        long rpcWaitEnd = System.currentTimeMillis();
        if (rpcException != null || httpResponse == null) {
            assembleRpcLog(rpcResponse, httpRequestBase.getURI().toString(), method, false,
                    httpRequestBase, null, rpcWaitStart, rpcWaitEnd);
            throw new RpcRemoteCallException("service:" + httpRequestBase.getURI().toString() +
                    "" + " " + "fails, retrycount:" + retryCount, rpcException);
        }
        HttpEntity httpEntity = null;
        try {
            if (httpResponse.getEntity() != null) {
                httpEntity = httpResponse.getEntity();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    httpEntity.writeTo(byteArrayOutputStream);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                respBytes = byteArrayOutputStream.toByteArray();
            }
        } finally {
            try {
                assembleRpcLog(rpcResponse, httpRequestBase.getURI().toString(), method, true,
                        httpRequestBase, respBytes, rpcWaitStart, rpcWaitEnd);
                EntityUtils.consume(httpEntity);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RpcRuntimeException("http request fail, code = " + httpResponse
                    .getStatusLine().getStatusCode() + "content:" + new String(respBytes));
        }

        if (respBytes != null && respBytes.length > 0) {
            rpcResponse.setReturnObject(rpcResponse.getRpcSerializer().responseParamFormat
                    (rpcResponse.getReturnType(), respBytes));
            rpcResponse.setRpcSuccess(true);
        }
    }

    @Override
    public String getProtocolName() {
        return "http";
    }


    /**
     * 组装http rpc log
     *
     * @param rpcResponse
     * @param url
     * @param method
     * @param isSuccess
     * @param httpRequestBase
     * @param responseBytes
     */
    private void assembleRpcLog(RpcResponse rpcResponse, String url, String method, boolean
            isSuccess, HttpRequestBase httpRequestBase, byte[] responseBytes, long rpcStartMills,
                                long rpcEndMills) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpEntity logHttpEntity = httpRequestBase instanceof HttpEntityEnclosingRequestBase ? (
                (HttpEntityEnclosingRequestBase) httpRequestBase).getEntity() : null;
        if (logHttpEntity != null) {
            try {
                logHttpEntity.writeTo(outputStream);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        try {
            rpcResponse.getRpcLog().setRequestContent(new String(outputStream.toByteArray(),
                    "utf-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (ArrayUtils.isNotEmpty(httpRequestBase.getAllHeaders())) {
            rpcResponse.getRpcLog().setRequestHeader(ArrayUtils.toString(httpRequestBase
                    .getAllHeaders()));
            HashMap<String,String> requestHeaderMap=Maps.newHashMap();
            for (Header header : httpRequestBase.getAllHeaders()) {
                requestHeaderMap.put(header.getName(),header.getValue());
            }
            rpcResponse.getRpcLog().setRequestHeaderMap(requestHeaderMap);
        }
        rpcResponse.getRpcLog().setUrl(url);
        rpcResponse.getRpcLog().setMethodName(method);
        rpcResponse.getRpcLog().setRpcSuccess(isSuccess);
        rpcResponse.setRpcSuccess(isSuccess);
        rpcResponse.getRpcLog().setResponseContent(ArrayUtils.isEmpty(responseBytes) ? "" : new
                String(responseBytes));
        rpcResponse.getRpcLog().setRpcWaitCostMills(rpcEndMills - rpcStartMills);

        //        LOGGER.info("http {} rpc log: url:{} {}, request content:{}, response content:{}",
        //                isSuccess ? "success" : "fail", method, url, new String(outputStream
        // .toByteArray
        //                        ()), ArrayUtils.isEmpty(responseBytes) ? "" : new String
        // (responseBytes));
    }
}
