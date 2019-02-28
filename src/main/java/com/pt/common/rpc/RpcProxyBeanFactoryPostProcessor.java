package com.pt.common.rpc;

import com.pt.common.utils.SpringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 自定义BeanFactoryPostProcessor，处理RpcProxy注解
 * @author lichaohui
 * @date 2017/10/21 下午3:19
 */
@Component
public class RpcProxyBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    /**
     * 默认为com.didi
     */
    private String basePackages = "com.didi";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        ClassPathRpcProxyScanner scanner = new ClassPathRpcProxyScanner((BeanDefinitionRegistry) beanFactory);
        //过滤指定的类
        scanner.registerFilters();
        List<String> basePackageList = SpringUtils.getContextBasePackages(beanFactory);
        if (CollectionUtils.isEmpty(basePackageList)) {
            basePackageList.add(basePackages);
        }
        scanner.doScan(basePackageList.toArray(new String[0]));
    }
}
