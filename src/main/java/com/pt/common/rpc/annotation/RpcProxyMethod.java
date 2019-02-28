package com.pt.common.rpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 注解到需要代理的接口的方法上，对于该方法的调用，会自动转换成rpc远程调用
 * 方法入参会转换为rpc的出参，rpc的返回参数会被转换为接口的出参。
 * {@code @RpcProxy(protocal="http", serialization="json")}<br/>
 *  public interface HttpJsonRpcTest {<br/>
 *  &nbsp;&nbsp;&nbsp;&nbsp;{@code @RpcProxyMethod}(host = "${test.host1}", method = "get")<br/>
 *  &nbsp;&nbsp;&nbsp;&nbsp;RpcTest.App getWithObjInObjOut(RpcTest.User user);<br/>
 *  }<br/>
 *  使用时只需@Autowire即可<br/>
 *  {@code @Autowire}<br/>
 *  private HttpJsonRpcTest httpJsonRpcTest;<br/>
 *  // 以下实现一次远程调用，地址是${test.host1}，方法是get。调用的具体实现由框架自动代理<br/>
 *  // 若调用时出现超时或者404，则会抛出RpcRuntimeException<br/>
 *  App a = httpJsonRpcTest.getWithObjInObjOut(u);
 * @author hechengchen
 * @date 2017/10/20 下午5:24
 */

@Target(METHOD)
@Documented
@Retention(RUNTIME)
public @interface RpcProxyMethod {

    /**
     * 注解ip + port，多个使用逗号隔开
     * @return
     */
    String host();

    /**
     * 请求方法，默认为get
     */
    String method() default "get";

    /**
     * 连接超时
     * @return
     */
    int connectTimeount() default 10000;

    /**
     * 读取超时
     * @return
     */
    int readTimeout() default 10000;

    /**
     * 重试次数
     * @return
     */
    int retryCount() default 0;
}
