package com.pt.common.rpc.ha;


/**
 * 高可用策略factory，目前只支持恢复策略
 *
 * @author hechengchen
 * @date 2017/11/3 下午10:47
 */
public class HaStrategyFactory {


    private static final HaStrategy DEFAULT_HA_STRATEGY = new RecoverHaStrategy();

    /**
     * 返回默认高可用策略
     * @return 恢复策略
     */
    public static HaStrategy getDefaultHaStrategy() {
        return DEFAULT_HA_STRATEGY;
    }

}
