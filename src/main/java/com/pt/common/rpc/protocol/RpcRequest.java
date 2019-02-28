package com.pt.common.rpc.protocol;

import com.pt.common.rpc.RpcProxyManager;
import com.pt.common.rpc.serializer.RpcSerializer;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author hechengchen
 * @date 2018/3/27 下午3:58
 */
public class RpcRequest {

    /**
     * 调用目标地址，可以是ip + port，也可以是域名，可能是多个url
     */
    private String urls;

    /**
     * 目标调用url
     */
    private String targetUrl;
    /**
     * 若是http请求，需要该参数
     */
    private String method;

    /**
     * 方法的入参，key是参数名,value是参数值。ps：jdk1.8以前，取得的方法参数为arg0，
     * jdk1.8以上才能取得参数名
     */
    private Map<String, Object> param;

    /**
     * 方法入参中，加入注解的参数，需要各个协议特殊处理
     */
    private Map<String, Map<String, Object>> annotationedParam;

    /**
     * 序列化工具名称
     */
    private String serializerName;

    /**
     * 序列化工具
     */
    private RpcSerializer rpcSerializer;

    /**
     * 连接建立超时时间，默认为10s
     */
    private int connTimeout;

    /**
     * 读取超时时间，默认为10s
     */
    private int readTimeout;

    /**
     * 失败重试次数，默认不重试
     */
    private int retryCount;

    /**
     * java接口返回参数type，用于反序列化
     */
    private Type returnType;

    private RpcRequest(Builder builder) {
        setUrls(builder.urls);
        setTargetUrl(builder.targetUrl);
        setMethod(builder.method);
        setParam(builder.param);
        setAnnotationedParam(builder.annotationedParam);

        // 保证RpcSerializer在serializerName前被set
        setRpcSerializer(builder.rpcSerializer);
        setSerializerName(builder.serializerName);
        setConnTimeout(builder.connTimeout);
        setReadTimeout(builder.readTimeout);
        setRetryCount(builder.retryCount);
        setReturnType(builder.returnType);
    }


    public String getUrls() {
        return urls;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }

    public String getSerializerName() {
        return serializerName;
    }

    public void setSerializerName(String serializerName) {
        this.serializerName = serializerName;
        if (getRpcSerializer() == null) {
            setRpcSerializer(RpcProxyManager.getRpcSerialize(serializerName));
        }
    }

    public RpcSerializer getRpcSerializer() {
        return rpcSerializer;
    }

    public void setRpcSerializer(RpcSerializer rpcSerializer) {
        this.rpcSerializer = rpcSerializer;
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Map<String, Map<String, Object>> getAnnotationedParam() {
        return annotationedParam;
    }

    public void setAnnotationedParam(Map<String, Map<String, Object>> annotationedParam) {
        this.annotationedParam = annotationedParam;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return "RpcRequest{" + "urls='" + urls + '\'' + ", targetUrl='" + targetUrl + '\'' + ", "
                + "method='" + method + '\'' + ", param=" + param + ", annotationedParam=" +
                annotationedParam + ", serializerName=" + serializerName + ", connTimeout=" +
                connTimeout + ", readTimeout=" + readTimeout + ", retryCount=" + retryCount + ", " +
                "" + "" + "returnType=" + returnType + '}';
    }

    public static final class Builder {
        private String urls;
        private String targetUrl;
        private String method;
        private Map<String, Object> param;
        private Map<String, Map<String, Object>> annotationedParam;
        private String serializerName;
        private RpcSerializer rpcSerializer;
        private int connTimeout;
        private int readTimeout;
        private int retryCount;
        private Type returnType;

        public Builder() {
        }

        public Builder urls(String val) {
            urls = val;
            return this;
        }

        public Builder targetUrl(String val) {
            targetUrl = val;
            return this;
        }

        public Builder method(String val) {
            method = val;
            return this;
        }

        public Builder param(Map<String, Object> val) {
            param = val;
            return this;
        }

        public Builder serializerName(String val) {
            serializerName = val;
            return this;
        }

        public Builder connTimeout(int val) {
            connTimeout = val;
            return this;
        }

        public Builder readTimeout(int val) {
            readTimeout = val;
            return this;
        }

        public Builder retryCount(int val) {
            retryCount = val;
            return this;
        }

        public Builder returnType(Type val) {
            returnType = val;
            return this;
        }

        public Builder annotationedParam(Map<String, Map<String, Object>> val) {
            annotationedParam = val;
            return this;
        }

        public Builder rpcSerializer(RpcSerializer val) {
            rpcSerializer = val;
            return this;
        }

        public RpcRequest build() {
            return new RpcRequest(this);
        }
    }
}
