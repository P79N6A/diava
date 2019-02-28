package com.pt.common.rpc.lb;

import java.util.List;

/**
 * 远程调用负责均衡策略，目前仅支持轮训策略
 * @author hechengchen
 * @date 2017/10/23 下午4:55
 */
public interface LbStrategy {


    /**
     * 多个url之间的分隔符
     */
    String URL_SPLIT = ",";

    /**
     * 根据负载均衡策略，从多个url中选择当前调用的url
     * @param urls 多个url，以逗号分隔
     * @return 此次调用的url
     */
    String electHost(String urls);

    /**
     * 根据负载均衡策略，从多个url中选择当前调用的url
     * @param urlList urlList
     * @return 此次elect中的url
     */
    String electHost(List<String> urlList);

}
