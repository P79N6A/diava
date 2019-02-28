package com.pt.common.rpc.ha;

import java.util.Collection;

/**
 * 高可用策略模板
 * @author hechengchen
 * @date 2017/11/3 下午4:46
 */
public interface HaStrategy {

    /**
     * 若failHost在执行过程中出错，则将入HaStrategy进行处理
     * @param failUrl 失败的ip+端口或链接
     * @param urlList 失败url所在的集合
     */
    void doHa(String failUrl, Collection<String> urlList);

}
