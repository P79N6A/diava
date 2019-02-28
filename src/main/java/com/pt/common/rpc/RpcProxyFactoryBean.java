package com.pt.common.rpc;

import com.pt.common.rpc.exception.RpcConfigException;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * 所有RpcProxy接口的FactoryBean，返回代理实例
 *
 * @author hechengchen
 * @date 2017/10/21 下午3:26
 */
public class RpcProxyFactoryBean<T> implements FactoryBean<T> {

    private Class<T> rpcProxyClass;

    private String protocol;

    private String serialization;

    @Override
    public T getObject() {
        if (RpcProxyManager.getRpcProtocol(protocol) == null || RpcProxyManager.getRpcSerialize
                (serialization) == null) {
            throw new RpcConfigException("protocol or serialization is null");
        }
        return (T) Proxy.newProxyInstance(this.rpcProxyClass.getClassLoader(), new
                Class[]{rpcProxyClass}, new RpcProxyInvocationHandler(protocol, serialization));
    }

    @Override
    public Class<T> getObjectType() {
        return rpcProxyClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setRpcProxyClass(Class<T> rpcProxyClass) {
        this.rpcProxyClass = rpcProxyClass;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }


}
