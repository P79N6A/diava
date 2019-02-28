package com.pt.common.rpc.exception;

/**
 * 远程调用错误，如404，网络异常，字节读取异常
 * @author hechengchen
 * @date 2017/10/22 上午11:01
 */
public class RpcRemoteCallException extends RuntimeException {


    public RpcRemoteCallException(String message) {
        super(message);
    }

    public RpcRemoteCallException(String message, Throwable cause) {
        super(message, cause);
    }

}
