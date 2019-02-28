package com.pt.common.utils;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * @author hechengchen
 * @date 2017/8/8 下午9:58
 */
public class HttpClientUtils {

    /**
     * 创建http client连接池
     *
     * @param maxConnCount 最大连接数
     * @return 支持连接池的HttpClient
     */
    public static CloseableHttpClient newCloseableHttpClient(int maxConnCount, int
            connReqTimeoout, int connTimeout, int readTimeout, int retryCount) {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("http", plainsf).register("https",
                        sslsf).build();


        HttpClientBuilder httpClientBuilder = HttpClients.custom();

        // 即便使用PoolingConnectionManager，但是在MainClientExec中，如果检测到HttpResponse的
        // Connection: close, 则会关闭该Socket，并且不进入Pool
        PoolingHttpClientConnectionManager ohcm = new PoolingHttpClientConnectionManager(registry);
        ohcm.setMaxTotal(maxConnCount); // 设置连接池最大连接数
        ohcm.setDefaultMaxPerRoute(maxConnCount); // 每个路由基础的连接(默认，每个路由基础上的连接不超过2个)
        httpClientBuilder.setConnectionManager(ohcm);

        if (retryCount >= 0) {
            httpClientBuilder.setRetryHandler(newHttpRequestRetryHandler(retryCount));
        }
        if (connReqTimeoout > 0 || connTimeout > 0 || readTimeout > 0) {
            httpClientBuilder.setDefaultRequestConfig(newRequestConfig(connReqTimeoout,
                    connTimeout, readTimeout));
        }
        httpClientBuilder.setDefaultSocketConfig(newSocketConfig());
        return httpClientBuilder.build();
    }

    /**
     * 根据产品线配置构造RequestConfig
     *
     * @param connReqTimeout 从连接池获取http连接超时时间
     * @param connTimeout    连接超时时间
     * @param readTimeout    读取超时时间
     * @return RequestConfig
     */
    public static RequestConfig newRequestConfig(int connReqTimeout, int connTimeout, int
            readTimeout) {
        RequestConfig.Builder rb = RequestConfig.custom();
        if (connReqTimeout > 0) {
            rb.setConnectionRequestTimeout(connReqTimeout);
        }
        if (connTimeout > 0) {
            rb.setConnectTimeout(connTimeout);
        }
        if (readTimeout > 0) {
            rb.setSocketTimeout(readTimeout);
        }
        return rb.build();
    }

    /**
     * 根据产品配置构造SocketConfig
     *
     * @return SocketCOnfig
     */
    public static SocketConfig newSocketConfig() {
        // 关闭内格尔算法，避免write - write - read延迟40ms的问题
        return SocketConfig.custom().setSoKeepAlive(true).setSoReuseAddress(true).setTcpNoDelay
                (true).build();
    }

    /**
     * 根据产品线配置设置重试次数
     *
     * @param retryCount 重试次数
     * @return HttpRequestRetryHandler
     */
    public static HttpRequestRetryHandler newHttpRequestRetryHandler(int retryCount) {
        return new DefaultHttpRequestRetryHandler(retryCount < 0 ? 0 : retryCount, false);
    }

}
