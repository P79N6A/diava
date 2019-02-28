package com.pt.common.rpc.serializer;

import org.apache.http.HttpEntity;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * 序列化工具
 * @author hechengchen
 * @date 2017/10/22 上午10:07
 */
public interface RpcSerializer {

    /**
     * 序列化出参，即rpc请求参数
     * @param requestParam 调用代理接口方法的参数
     * @param excludeKey 需要排除的key
     * @return 请求参数序列化后的二进制
     */
    HttpEntity requestParamFormat(Object requestParam, Set<String> excludeKey);

    /**
     * 序列化返回参数，即rpc请求结构
     * @param responseType 代理接口要求返回的参数类型
     * @param originResp rpc请求返回的二进制
     * @return T的实例
     */
    Object responseParamFormat(Type responseType, byte[] originResp);

}
