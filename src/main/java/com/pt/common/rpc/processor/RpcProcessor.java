package com.pt.common.rpc.processor;

import com.pt.common.rpc.annotation.RpcProxyMethod;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * @author hechengchen
 * @date 2018/4/20 上午11:52
 */
public interface RpcProcessor {

    /**
     * 处理rpc请求
     *
     * @param protocolName   请求协议，http，socket，dirpc等
     * @param serializerName 序列化协议
     * @param returnType     请求返回类型
     * @param rpcProxyMethod 被注释的请求方法
     * @param params         请求参数元数据
     * @param args           请求参数值
     * @return 请求接口，并且序列化为returnType
     */
    Object processRpc(String protocolName, String serializerName, Type returnType, RpcProxyMethod
            rpcProxyMethod, Parameter[] params, Object[] args);

}
