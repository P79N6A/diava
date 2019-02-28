package com.pt.common.rpc.protocol;

import com.pt.common.rpc.RpcProxyManager;
import com.pt.common.rpc.serializer.RpcSerializer;

import java.lang.reflect.Type;

/**
 * @author hechengchen
 * @date 2018/3/27 下午4:41
 */
public class RpcResponse {

    private Object returnObject;
    private Type returnType;
    private RpcSerializer rpcSerializer;
    private String serializerName;
    private String protocolName;
    private boolean rpcSuccess;
    private RpcLog rpcLog;

    private RpcResponse(Builder builder) {
        setReturnType(builder.returnType);
        setRpcSerializer(builder.rpcSerializer);
        setSerializerName(builder.serializerName);
        setProtocolName(builder.protocolName);
        setRpcLog(builder.rpcLog != null ? builder.rpcLog : new RpcLog.Builder().startTimeMills
                (System.currentTimeMillis()).protocalName(builder.protocolName).build());
    }

    public RpcLog getRpcLog() {
        return rpcLog;
    }

    public void setRpcLog(RpcLog rpcLog) {
        this.rpcLog = rpcLog;
    }

    public Object getReturnObject() {
        return returnObject;
    }

    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public RpcSerializer getRpcSerializer() {
        return rpcSerializer;
    }

    public void setRpcSerializer(RpcSerializer rpcSerializer) {
        this.rpcSerializer = rpcSerializer;
    }

    public boolean isRpcSuccess() {
        return rpcSuccess;
    }

    public void setRpcSuccess(boolean rpcSuccess) {
        this.rpcSuccess = rpcSuccess;
    }

    public String getSerializerName() {
        return serializerName;
    }

    public void setSerializerName(String serializerName) {
        this.serializerName = serializerName;
        if (getRpcSerializer() == null) {
            setRpcSerializer(RpcProxyManager.getRpcSerialize(serializerName));
        }
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    @Override
    public String toString() {
        return "RpcResponse{" + "returnObject=" + returnObject + ", returnType=" + returnType +
                ", rpcSerializer=" + rpcSerializer + ", rpcSuccess=" + rpcSuccess + ", rpcLog=" +
                rpcLog + '}';
    }

    public static final class Builder {
        private Type returnType;
        private RpcSerializer rpcSerializer;
        private String serializerName;
        private RpcLog rpcLog;
        private String protocolName;

        public Builder() {
        }

        public Builder returnType(Type val) {
            returnType = val;
            return this;
        }

        public Builder rpcSerializer(RpcSerializer val) {
            rpcSerializer = val;
            return this;
        }

        public Builder serializerName(String val) {
            serializerName = val;
            return this;
        }

        public Builder rpcLog(RpcLog val) {
            rpcLog = val;
            return this;
        }

        public RpcResponse build() {
            return new RpcResponse(this);
        }

        public Builder protocolName(String val) {
            protocolName = val;
            return this;
        }
    }
}
