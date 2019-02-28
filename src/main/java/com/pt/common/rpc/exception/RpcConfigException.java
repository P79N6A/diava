package com.pt.common.rpc.exception;

/**
 * rpc组件配置异常
 * @author hechengchen
 * @date 2017/10/22 上午11:01
 */
public class RpcConfigException extends RuntimeException {


    public RpcConfigException(String message) {
        super(message);
    }

    public RpcConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
