package com.pt.common.rpc.lb;

import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮训负载均衡策略
 *
 * @author hechengchen
 * @date 2017/10/23 下午4:56
 */
public class RrLbStrategy implements LbStrategy {

    /**
     * 缓存当前urls调用轮训的index
     */
    private Map<String, AtomicInteger> hostRrMap = Maps.newHashMap();

    /**
     * 缓存当前urls和url列表的对应关系
     */
    private Map<String, List<String>> hostUrlsUrlMap = Maps.newHashMap();

    @Override
    public String electHost(String urls) {
        if (StringUtils.isEmpty(urls)) {
            return null;
        }
        if (!urls.contains(URL_SPLIT)) {
            return urls;
        }

        List<String> urlList = hostUrlsUrlMap.get(urls);
        if (CollectionUtils.isEmpty(urlList)) {
            hostUrlsUrlMap.put(urls, (urlList = splitUrl(urls)));
        }
        return urlList.get(getRrIndex(urls) % (urlList.size()));
    }

    @Override
    public String electHost(List<String> urlList) {
        if (CollectionUtils.isEmpty(urlList)) {
            return null;
        }
        if (urlList.size() == 1) {
            return urlList.get(0);
        }
        return urlList.get(getRrIndex(urlList.toString()) % urlList.size());
    }


    private int getRrIndex(String key) {
        AtomicInteger curIndex = hostRrMap.get(key);
        if (curIndex == null) {
            hostRrMap.put(key, (curIndex = new AtomicInteger(-1)));
        }
        int intCurIndex;
        for (; ; ) {
            if ((intCurIndex = curIndex.incrementAndGet()) == Integer.MAX_VALUE) {
                curIndex.set(0);
                continue;
            }
            break;
        }
        return intCurIndex;
    }

    private List<String> splitUrl(String urls) {
        return Arrays.asList(urls.split(URL_SPLIT));
    }
}
