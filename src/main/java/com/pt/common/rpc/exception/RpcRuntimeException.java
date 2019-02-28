package com.pt.common.rpc.exception;

/**
 * rpc组件运行异常，包括参数传递错误，序列化错误等
 * @author hechengchen
 * @date 2017/10/22 上午11:01
 */
public class RpcRuntimeException extends RuntimeException {


    public RpcRuntimeException(String message) {
        super(message);
    }

    public RpcRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
