package com.pt.common.rpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 注解到需要代理的接口上，rpc调用实现类会自动生成。默认为http + json
 * {@code @RpcProxy(protocal="http", serialization="json")}<br/>
 *  public interface HttpJsonRpcTest {<br/>
 *  &nbsp;&nbsp;&nbsp;&nbsp;{@code @RpcProxyMethod}(host = "${test.host1}", method = "get")<br/>
 *  &nbsp;&nbsp;&nbsp;&nbsp;RpcTest.App getWithObjInObjOut(RpcTest.User user);<br/>
 *  }<br/>
 *  使用时只需@Autowire即可<br/>
 *  {@code @Autowire}<br/>
 *  private HttpJsonRpcTest httpJsonRpcTest;
 * @author hechengchen
 * @date 2017/10/20 下午5:24
 */

@Target(TYPE)
@Documented
@Retention(RUNTIME)
public @interface RpcProxy {

    /**
     * 通信协议，默认为http
     * @return
     */
    String protocol() default "http";

    /**
     * 数据序列化方式，默认为json
     * @return
     */
    String serialization() default "json";

}
