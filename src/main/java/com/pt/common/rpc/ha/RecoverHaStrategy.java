package com.pt.common.rpc.ha;

import com.google.common.collect.Maps;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * 恢复策略，尽量恢复失败的链接
 *
 * @author hechengchen
 * @date 2017/11/3 下午4:52
 */
public class RecoverHaStrategy extends Thread implements HaStrategy {


    private static final Logger LOGGER = LoggerFactory.getLogger(RecoverHaStrategy.class);


    private static final TelnetClient TELNET_CLIENT = new TelnetClient();

    /**
     * telent间隔时间
     */
    private static final int TELNET_INTERNAL = 3000;
    /**
     * 重试恢复队列
     */
    private static final Map<String, Collection<String>> RECOVER_MAP = Maps.newConcurrentMap();
    private volatile boolean wait = false;


    public RecoverHaStrategy() {
        this(3000);
    }

    public RecoverHaStrategy(int retryTimeout) {
        super("ha-strategy-recover-thread");
        TELNET_CLIENT.setDefaultTimeout(retryTimeout < 1000 ? 1000 : retryTimeout);
    }

    /**
     * 若failHost持续不通，则加入重试队列进行重试，并从可用队列中摘除
     * 若failHost恢复后，则从重试队列摘除，重新加入可用队列
     *
     * @param failHost 失败的ip+端口或链接
     * @param hostList 失败host所在的集合
     */
    @Override

    public void doHa(String failHost, Collection<String> hostList) {
        if (StringUtils.isEmpty(failHost) || CollectionUtils.isEmpty(hostList) || hostList.size()
                == 1) {
            return;
        }

        if (!isAlive()) {
            start();
        }

        if (!RECOVER_MAP.containsKey(failHost) && !telnet(failHost)) {
            synchronized (this) {
                fail(failHost, hostList);
                if (wait) {
                    wait = false;
                    notify();
                }
            }
        }
    }

    @Override
    public void run() {
        for (; ; ) {
            try {
                if (RECOVER_MAP.size() == 0 && wait == false) {
                    synchronized (this) {
                        wait = true;
                        wait();
                    }
                }
                for (Map.Entry<String, Collection<String>> entry : RECOVER_MAP.entrySet()) {
                    if (telnet(entry.getKey())) {
                        recover(entry.getKey());
                    }
                }
                Thread.sleep(TELNET_INTERNAL);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }


    /**
     * 失败链接恢复
     *
     * @param failHost
     */
    private void recover(String failHost) {
        Collection<String> hostList = RECOVER_MAP.get(failHost);
        if (hostList != null) {
            hostList.add(failHost);
            RECOVER_MAP.remove(failHost);
            LOGGER.info("host:{} has recovered to available queue", failHost);
        }
    }

    /**
     * 将当前host进行fail操作
     *
     * @param failHost fail链接
     * @param hostList fail链接所在的集合
     */
    private void fail(String failHost, Collection<String> hostList) {
        if (hostList.remove(failHost)) {
            RECOVER_MAP.put(failHost, hostList);
            LOGGER.info("host:{} has added to recover queue", failHost);
        }
    }


    /**
     * talnet fail的host再次进行测试
     *
     * @param failHostPort 失败的ip + port
     * @return 是否能联通
     */
    private boolean telnet(String failHostPort) {
        String[] failHostAndPort = failHostPort.split(":");
        try {
            if (failHostAndPort.length == 2) {
                TELNET_CLIENT.connect(failHostAndPort[0], Integer.parseInt(failHostAndPort[1]));
            } else {
                TELNET_CLIENT.connect(failHostAndPort[0], 80);
            }
            return true;
        } catch (Exception e) {
            LOGGER.info("host:{} has still failed, failHost", failHostPort);
            return false;
        }
    }
}
