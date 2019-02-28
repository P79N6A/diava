package com.pt.common.rpc.lb;

/**
 * 负载均衡策略factory
 * @author hechengchen
 * @date 2017/11/4 上午12:05
 */
public class LbStrategyFactory {


    private static final LbStrategy DEFAULT_LB_STRATEGY = new RrLbStrategy();

    /**
     * 返回默认复杂均衡策略
     * @return 默认复杂均衡策略
     */
    public static LbStrategy getDefaultLbStrategy() {
        return DEFAULT_LB_STRATEGY;
    }

}
