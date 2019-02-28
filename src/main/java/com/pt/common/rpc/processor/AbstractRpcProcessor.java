package com.pt.common.rpc.processor;

import com.pt.common.rpc.RpcProxyManager;
import com.pt.common.rpc.annotation.RpcProxyMethod;
import com.pt.common.rpc.exception.RpcConfigException;
import com.pt.common.rpc.exception.RpcRemoteCallException;
import com.pt.common.rpc.protocol.RpcProtocol;
import com.pt.common.rpc.protocol.RpcRequest;
import com.pt.common.rpc.protocol.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * 负责获取RpcRequest，RpcResponse，并调用Protocol.execute
 *
 * @author hechengchen
 * @date 2018/4/20 下午4:04
 */
public abstract class AbstractRpcProcessor implements RpcProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRpcProcessor.class);


    @Override
    public Object processRpc(String protocolName, String serializerName, Type returnType,
                             RpcProxyMethod rpcProxyMethod, Parameter[] params, Object[] args) {

        RpcProtocol protocol = RpcProxyManager.getRpcProtocol(protocolName);

        RpcRequest rpcRequest = getRpcRequest(protocolName, serializerName, rpcProxyMethod,
                params, args);
        RpcResponse rpcResponse = getRpcResponse(protocolName, serializerName, returnType);

        if (rpcRequest == null || rpcResponse == null) {
            throw new RpcConfigException("rpc request or rpc response error.rpc request:" +
                    rpcRequest + ", rpc response:" + rpcResponse);
        }

        try {
            protocol.execute(rpcRequest, rpcResponse);
        } catch (RpcRemoteCallException rrce) {
            LOGGER.error(rrce.getMessage(), rrce);
            doHa(rpcRequest.getTargetUrl(), rpcRequest.getUrls());
            throw rrce;
        } catch (Throwable throwable) {
            LOGGER.error(throwable.getMessage(), throwable);
            throw throwable;
        } finally {
            rpcResponse.getRpcLog().printLog();
        }
        return rpcResponse.getReturnObject();
    }

    /**
     * 组装RpcRequest，request的url，负载均衡等信息可以来至本地配置，也可能来至远程注册中心
     *
     * @param protocolName   通信协议名称
     * @param serializeName  序列化协议
     * @param rpcProxyMethod 被注解的方法
     * @param params         参数元数据
     * @param args           参数值
     * @return RpcRequest
     */
    protected abstract RpcRequest getRpcRequest(String protocolName, String serializeName,
                                                RpcProxyMethod rpcProxyMethod, Parameter[]
                                                        params, Object[] args);

    /**
     * 组装RpcResponse，response的熔断处理方法时等，可以来至注册中心，可以本地配置
     *
     * @param protocolName  通信协议名称
     * @param serializeName 序列化协议
     * @param returnType    申明的返回类型
     * @return RpcResponse
     */
    protected abstract RpcResponse getRpcResponse(String protocolName, String serializeName, Type
            returnType);

    /**
     * 高可用操作，摘除失效url，可能从配置中心摘除，可能从本地内存中摘除
     *
     * @param targetUrl 目标url
     * @param urls      目标url所在的url列表
     */
    protected abstract void doHa(String targetUrl, String urls);


}
