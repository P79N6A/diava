package com.pt.common.rpc;

import com.pt.common.rpc.protocol.RpcProtocol;
import com.pt.common.rpc.protocol.http.HttpRpcProtocol;
import com.pt.common.rpc.serializer.FormJsonRpcSerializer;
import com.pt.common.rpc.serializer.JsonRpcSerializer;
import com.pt.common.rpc.serializer.RpcSerializer;
import com.google.common.collect.Maps;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * RpcProxy管理器，加载并管理所有protocol和serialization
 * @author hechengchen
 * @date 2017/10/22 上午10:56
 */
public class RpcProxyManager {

    private static Map<String, RpcSerializer> serializerMap;
    private static Map<String, RpcProtocol> protocolMap;

    static {
        protocolMap = Maps.newHashMap();
        protocolMap.put("http", new HttpRpcProtocol());

        serializerMap = Maps.newHashMap();
        serializerMap.put("json", new JsonRpcSerializer());
        serializerMap.put("form", new FormJsonRpcSerializer());

    }

    /**
     * 根据名称获取serialization
     * @param key serialization key
     * @return RpcSerializer
     */
    public static RpcSerializer getRpcSerialize(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return serializerMap.get(key.toLowerCase());
    }

    /**
     * 根据名称获取protocol
     * @param key protocol key
     * @return RPCProtocol
     */
    public static RpcProtocol  getRpcProtocol(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return protocolMap.get(key.toLowerCase());
    }

}
