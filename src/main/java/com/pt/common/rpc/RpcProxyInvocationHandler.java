package com.pt.common.rpc;

import com.pt.common.rpc.annotation.RpcProxyMethod;
import com.pt.common.rpc.exception.RpcConfigException;
import com.pt.common.rpc.processor.LocalRpcProcessor;
import com.pt.common.rpc.processor.RpcProcessor;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * RpcProxyMethod代理InvocationHandler
 *
 * @author hechengchenLoggingManagedHttpClientConnection.javaLoggingManagedHttpClientConnection.java
 * @date 2017/10/20 下午5:45
 */
public class RpcProxyInvocationHandler implements InvocationHandler {

    private RpcProcessor rpcProcessor = new LocalRpcProcessor();

    private String protocol;

    private String serialization;

    public RpcProxyInvocationHandler(String protocol, String serialization) {
        this.protocol = protocol;
        this.serialization = serialization;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcProxyMethod rpcMethod = null;
        if (!method.isAnnotationPresent(RpcProxyMethod.class)) {
            throw new RpcConfigException("can't find annotation on method:" + method.getName());
        }
        //获取被RpcProxyMethod 注解的method 的注解
        rpcMethod = method.getAnnotation(RpcProxyMethod.class);
        //拿到注解中的信息
        String urlHolder = rpcMethod.host();
        if (StringUtils.isEmpty(urlHolder)) {
            throw new RpcConfigException("host url is null for method:" + method.getName());
        }

        Class<?> originBeanClass = proxy.getClass();
        // todo, 支持多种processor，如本地配置，disf等
        return rpcProcessor.processRpc(protocol, serialization, method.getGenericReturnType(),
                rpcMethod, method.getParameters(), args);
    }


}
