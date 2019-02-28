package com.pt.common.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hechengchen
 * @date 2017/8/8 上午11:32
 */
public class UrlUtils {


    public static Set<String> getUrlPathParam(String url) {
        String[] params;
        if (StringUtils.isEmpty(url) || ArrayUtils.isEmpty((params = org.apache.commons.lang
                .StringUtils.substringsBetween(url, "$[", "]")))) {
            return null;
        } else {
            return Sets.newHashSet(params);
        }
    }

    /**
     * 获取指定level以前的所有路径
     *
     * @param requestUri 请求url
     * @param level      路径段数
     * @return 指定level以前的所有路径
     */
    public static String getFullPathByLevel(String requestUri, int level) {
        if (StringUtils.isEmpty(requestUri)) {
            return "";
        }
        String tempReqUri = requestUri;
        if (requestUri.startsWith("/")) {
            tempReqUri = requestUri.substring(1);
        }
        String[] paths = tempReqUri.split("/");
        if (paths.length < level) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            if (StringUtils.isEmpty(paths[i])) {
                continue;
            }
            sb.append(paths[i]).append("/");
        }
        return sb.length() == 0 ? "" : "/" + sb.toString();
    }

    /**
     * 根据level获取指定的uri
     *
     * @param requestUri uri
     * @param level      段数
     * @return 指定uri
     */
    public static String getPathByLevel(String requestUri, int level) {
        if (StringUtils.isEmpty(requestUri)) {
            return "";
        }
        String[] paths = requestUri.split("/");
        if (paths == null || paths.length < level + 1) {
            return "";
        }
        // paths = requestUri.split("\\");
        return paths[level];
    }

    public static Map<String, String> getUrlParamMap(String queryString) {
        Map<String, String> paramMap = Maps.newHashMap();
        if (StringUtils.isEmpty(queryString)) {
            return paramMap;
        }
        String[] paramStrs = queryString.split("&");
        for (String ps : paramStrs) {
            String[] param = ps.split("=");
            String realKey = param[0].replace("&", "");
            paramMap.put(realKey, param.length > 1 ? param[1] : realKey);
        }
        return paramMap;
    }

    public static String addParameter(String uri, List<BasicNameValuePair> pairs) {
        StringBuffer sb = new StringBuffer(uri);
        if (!uri.contains("?")) {
            boolean flag = false;
            for (BasicNameValuePair pair : pairs) {
                if (flag) {
                    sb.append("&");
                } else {
                    sb.append("?");
                    flag = true;
                }
                sb.append(pair.getName()).append("=").append(pair.getValue());
            }
        } else {
            for (BasicNameValuePair pair : pairs) {
                sb.append("&").append(pair.getName()).append("=").append(pair.getValue());
            }
        }
        return sb.toString();

    }

    /**
     * 增加参数 比如 uri = "/query" queryString ="name=xx" 拼装后结果我 uri = "/query?name=xx"
     *
     * @param uri
     * @param queryString
     * @param pairs
     * @return
     */
    public static String addParameter(String uri, String queryString, List<BasicNameValuePair>
            pairs) {
        if (!StringUtils.isEmpty(queryString)) {
            StringBuilder sb = new StringBuilder(uri);
            if (sb.indexOf("?") < 0) {
                sb.append("?");
            } else {
                sb.append("&");
            }
            sb.append(queryString);
            uri = sb.toString();
        }
        return addParameter(uri, pairs);
    }

    public static URL uriFormat(String urlStr) {
        if (StringUtils.isEmpty(urlStr)) {
            return null;
        }
        URL url = null;
        try {
            if (!urlStr.startsWith("http")) {
                url = new URL("http://" + urlStr);
            } else {
                url = new URL(urlStr);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getHostPortByUrlStr(String url) {
        String hostPort = null;
        try {
            hostPort = getHostPortByUrl(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return hostPort;
    }

    public static String getHostPortByUrl(URL url) {
        if (url == null) {
            return null;
        }
        // 同时适配域名和ip + port的模式
        return url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "");
    }

    public static String concatHostAndPath(String host, String path) {
        String hostWithProtocol = host;
        if (!hostWithProtocol.startsWith("http")) {
            hostWithProtocol = "http://" + hostWithProtocol;
        }
        if (!hostWithProtocol.endsWith("/") && !path.startsWith("/")) {
            return hostWithProtocol + "/" + path;
        }
        return hostWithProtocol + path;
    }

    public static String urlPathFormat(String originUrl) {
        String appUrl = originUrl.startsWith("/") ? originUrl : "/" + originUrl;
        return appUrl.endsWith("/") ? appUrl : appUrl + "/";
    }

    /**
     * 构造url字符串
     *
     * @param urlPath url路径
     * @param params  url参数集合
     * @return url 举例：/misp/mgt/app/?locale=zh-CN&utc_offset=480&canonical_country_code=CN
     */
    public static String buildUrlStr(String urlPath, Map<String, String> params) {
        if (Strings.isNullOrEmpty(urlPath)) {
            return null;
        }
        if (CollectionUtils.isEmpty(params)) {
            return urlPath;
        }
        String urlParams = Joiner.on("&").withKeyValueSeparator("=").join(params);
        StringBuilder urlBuilder = new StringBuilder(urlPath);
        if (urlPath.contains("?")) {
            urlBuilder.append("&").append(urlParams);
        } else {
            urlBuilder.append("?").append(urlParams);
        }
        String url = urlBuilder.toString();
        if (!url.contains("/?")) {
            url = url.replace("?", "/?");
        }
        return url;
    }


}

