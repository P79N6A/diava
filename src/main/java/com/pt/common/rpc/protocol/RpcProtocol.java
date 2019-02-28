package com.pt.common.rpc.protocol;

/**
 * Rpc调用协议接口
 * @author hechengchen
 * @date 2017/10/22 上午10:40
 */
public interface RpcProtocol {

    /**
     * rpc调用接口
     * @param rpcRequest rpc请求信息对象
     * @param rpcResponse rpc返回对象
     */
    void execute(RpcRequest rpcRequest, RpcResponse rpcResponse);

    /**
     * 获取协议名称
     * @return 协议名称
     */
    String getProtocolName();

}
