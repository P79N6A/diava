package com.pt.common.rpc.protocol;

import com.pt.common.RpcConstant;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author hechengchen
 * @date 2018/3/27 下午5:01
 */
public class RpcLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcLog.class);
    private static final Logger BaMaiLOGGER = LoggerFactory.getLogger("BaiMai");

    private String url;
    /**
     * post,get,put等
     */
    private String methodName;
    private String serializerName;
    private String protocalName;
    private String requestContent;
    private String requestHeader;
    private Map<String,String> requestHeaderMap= Maps.newHashMap();
    private String responseContent;
    private boolean rpcSuccess;

    private long startTimeMills;
    private long endTimeMills;
    private long rpcWaitCostMills;
    private long diavaCostMills;

    private RpcLog(Builder builder) {
        setUrl(builder.url);
        setSerializerName(builder.serializerName);
        setProtocalName(builder.protocalName);
        setRequestContent(builder.requestContent);
        setRequestHeader(builder.requestHeader);
        setResponseContent(builder.responseContent);
        setRpcSuccess(builder.rpcSuccess);
        setStartTimeMills(builder.startTimeMills);
        setEndTimeMills(builder.endTimeMills);
        setRpcWaitCostMills(builder.rpcWaitCostMills);
        setDiavaCostMills(builder.diavaCostMills);
    }


    public void printLog() {
        printLog(null);
    }


    public void printLog(String logPrefix) {
        String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date
                (getStartTimeMills()));
        setEndTimeMills(System.currentTimeMillis());
        long totalCost = getEndTimeMills() - getStartTimeMills();
        long diavaCost = totalCost - getRpcWaitCostMills();

        LOGGER.info("{}{} {} rpc log:||methodName={}||url={}||startTime={}||total cost={}||rpc wait cost={}" +
                "||diava " + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" +
                "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "cost={}" + "||request " +
                "header={}" + "||request" + "" + " " + "content={}" + "||response " +
                "content={}", StringUtils.isEmpty(logPrefix) ? "" : logPrefix + ", ",
                getProtocalName(), isRpcSuccess() ? "success" : "fail", methodName,getUrl(), startTime,
                totalCost, getRpcWaitCostMills(), diavaCost, getRequestHeader(),
                getRequestContent(), getResponseContent());
        printBaMaiLog(logPrefix, startTime, totalCost, diavaCost);//接入把脉打印日志
    }

    private void printBaMaiLog(String logPrefix, String startTime, long totalCost, long diavaCost) {
        StringBuilder sb=new StringBuilder(rpcSuccess?"_com_http_success":"_com_http_failure");
        if (MDC.get(RpcConstant.DIDI_HEADER_RID)!=null){//traceId
            sb.append("||").append(RpcConstant.TRACEID).append("=").append(MDC.get(RpcConstant.DIDI_HEADER_RID));
        }
        if (MDC.get(RpcConstant.DIDI_HEADER_SPANID)!=null){//spanId
            sb.append("||").append(RpcConstant.SPANID).append("=").append(MDC.get(RpcConstant.DIDI_HEADER_SPANID));
        }
        if (requestHeaderMap.containsKey(RpcConstant.DIDI_HEADER_SPANID)){//cspanId
            sb.append("||").append(RpcConstant.CSPANID).append("=").append(requestHeaderMap.get(RpcConstant.DIDI_HEADER_SPANID));
        }
        sb.append("||").append("url=").append(url);
        sb.append("||").append("method_name=").append(methodName);
        sb.append("||").append("proc_time=").append(totalCost);
        sb.append("||").append("errno=").append(rpcSuccess?"200":"500");//后期改成去状态码
        sb.append("||").append("errmsg=").append(rpcSuccess?"success":"fail");
        if(!StringUtils.isEmpty(logPrefix)) {
            sb.append("||").append("logPrefix=").append(logPrefix);
        }
        sb.append("||").append("protocolName=").append(protocalName);
        sb.append("||").append("startTime=").append(startTime);
        sb.append("||").append("rpcWaitTime=").append(getRpcWaitCostMills());
        sb.append("||").append("diavaCost=").append(diavaCost);
        sb.append("||").append("requestHeader=").append(requestHeaderMap.toString());
        sb.append("||").append("requestContent=").append(getRequestContent());
        sb.append("||").append("responseContent=").append(getResponseContent());
        BaMaiLOGGER.info(sb.toString());
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getRequestContent() {
        return requestContent;
    }

    public void setRequestContent(String requestContent) {
        this.requestContent = requestContent;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public boolean isRpcSuccess() {
        return rpcSuccess;
    }

    public void setRpcSuccess(boolean rpcSuccess) {
        this.rpcSuccess = rpcSuccess;
    }

    public long getStartTimeMills() {
        return startTimeMills;
    }

    public void setStartTimeMills(long startTimeMills) {
        this.startTimeMills = startTimeMills;
    }

    public long getEndTimeMills() {
        return endTimeMills;
    }

    public void setEndTimeMills(long endTimeMills) {
        this.endTimeMills = endTimeMills;
    }

    public long getRpcWaitCostMills() {
        return rpcWaitCostMills;
    }

    public void setRpcWaitCostMills(long rpcWaitCostMills) {
        this.rpcWaitCostMills = rpcWaitCostMills;
    }

    public long getDiavaCostMills() {
        return diavaCostMills;
    }

    public void setDiavaCostMills(long diavaCostMills) {
        this.diavaCostMills = diavaCostMills;
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
    }

    public Map<String, String> getRequestHeaderMap() {
        return requestHeaderMap;
    }

    public void setRequestHeaderMap(Map<String, String> requestHeaderMap) {
        this.requestHeaderMap = requestHeaderMap;
    }

    public String getSerializerName() {
        return serializerName;
    }

    public void setSerializerName(String serializerName) {
        this.serializerName = serializerName;
    }

    public String getProtocalName() {
        return protocalName;
    }

    public void setProtocalName(String protocalName) {
        this.protocalName = protocalName;
    }

    public static final class Builder {
        private String url;
        private String serializerName;
        private String protocalName;
        private String requestContent;
        private String responseContent;
        private String requestHeader;
        private boolean rpcSuccess;
        private long startTimeMills;
        private long endTimeMills;
        private long rpcWaitCostMills;
        private long diavaCostMills;

        public Builder() {
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Builder serializerName(String val) {
            serializerName = val;
            return this;
        }

        public Builder protocalName(String val) {
            protocalName = val;
            return this;
        }

        public Builder requestContent(String val) {
            requestContent = val;
            return this;
        }

        public Builder responseContent(String val) {
            responseContent = val;
            return this;
        }

        public Builder rpcSuccess(boolean val) {
            rpcSuccess = val;
            return this;
        }

        public Builder startTimeMills(long val) {
            startTimeMills = val;
            return this;
        }

        public Builder endTimeMills(long val) {
            endTimeMills = val;
            return this;
        }

        public Builder rpcWaitCostMills(long val) {
            rpcWaitCostMills = val;
            return this;
        }

        public Builder diavaCostMills(long val) {
            diavaCostMills = val;
            return this;
        }

        public Builder requestHeader(String val) {
            requestHeader = val;
            return this;
        }

        public RpcLog build() {
            return new RpcLog(this);
        }
    }
}
