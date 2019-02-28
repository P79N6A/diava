package com.pt.common.rpc;

import com.pt.common.rpc.annotation.RpcProxy;
import com.pt.common.rpc.exception.RpcConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Set;

/**
 * scan所有被RpcProxy注解的接口
 * @author lichaohui
 * @date 2017/10/21 下午12:00
 */
public class ClassPathRpcProxyScanner extends ClassPathBeanDefinitionScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClassPathRpcProxyScanner.class);

    public ClassPathRpcProxyScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void registerFilters() {
        // if specified, use the given annotation and / or marker interface
        /**
         * 过滤，只include RpcProxy.class的接口
         */
        addIncludeFilter(new AnnotationTypeFilter(RpcProxy.class));
        addExcludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory
                    metadataReaderFactory) throws IOException {
                String className = metadataReader.getClassMetadata().getClassName();
                return className.endsWith("package-info");
            }
        });
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            logger.warn("no rpc interface to proxy");
        } else {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    /**
     * 重写ClassPathScanningCandidateComponentProvider的isCandidateComponent，使得接口的BeanDefinition
     * 也鞥返回
     * @param beanDefinition RpcProxy注解的接口的beanDefinition
     * @return 是否是candidate
     */
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return super.isCandidateComponent(beanDefinition) || (beanDefinition.getMetadata()
                .isInterface() && !beanDefinition.getMetadata().isAnnotation());
    }

    /**
     * 完善beanDefinition，包括FactoryBean的设置，protocol和serialization的获取
     * @param beanDefinitions scan到需要被RpcProxy注解接口的BeanDefinition
     */
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();

            logger.info("Creating http json rpc with name '" + holder.getBeanName());

            //获取到接口的class
            definition.getPropertyValues().add("rpcProxyClass", definition.getBeanClassName());
            Class<?> originBeanClass = null;
            try {
                definition.resolveBeanClass(this.getClass().getClassLoader());
                originBeanClass = definition.getBeanClass();
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
                String beanClassName = definition.getBeanClassName();
                if (!StringUtils.isEmpty(beanClassName)) {
                    try {
                        originBeanClass = Class.forName(beanClassName);
                    } catch (ClassNotFoundException e1) {
                        logger.error(e.getMessage(), e1);
                    }
                }
            }
            if (originBeanClass == null) {
                throw new RpcConfigException("cann't init class " + definition.getBeanClassName());
            }
            RpcProxy rpcProxy = originBeanClass.getAnnotation(RpcProxy.class);
            // 设置通信协议
            definition.getPropertyValues().add("protocol", rpcProxy.protocol());
            // 设置数据序列化方式
            definition.getPropertyValues().add("serialization", rpcProxy.serialization());
            //设置factoryBean  调用getObject 进行动态代理
            //将所有接口设置成FactoryBean 对象 以便在getObejct 中进行动态代理获取实际对象
            definition.setBeanClass(RpcProxyFactoryBean.class);
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }

    }
}
