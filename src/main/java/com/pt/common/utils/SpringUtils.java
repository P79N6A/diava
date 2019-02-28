package com.pt.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author hechengchen
 * @date 2018/4/4 上午10:32
 */
@Component
public class SpringUtils implements BeanFactoryPostProcessor {


    private static Map<String, PropertiesLoaderSupport> ppcMap = null;

    //    private static ApplicationContext applicationContext;

    private static ConfigurableListableBeanFactory configurableListableBeanFactory;

    public static <T> T getBean(String beanName) {
        return (T)configurableListableBeanFactory.getBean(beanName);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> tClass) {
        return configurableListableBeanFactory.getBeansOfType(tClass);
    }

    public static List<String> getContextBasePackages(BeanFactory beanFactory) {
        return AutoConfigurationPackages.get(beanFactory);
    }

    public static String getCurrentProfiles() {
        return getSpringProperty("spring.profiles.active");
    }

    //    @Override
    //    public void setApplicationContext(ApplicationContext applicationContext) throws
    // BeansException {
    //        this.applicationContext = applicationContext;
    //    }

    /**
     * 根据propertyKey获取spring placeholder中配置的属性
     *
     * @param propertyKey 属性key
     * @return 属性值
     */
    public static String getSpringProperty(String propertyKey) {
        String value = null;
        if (ppcMap == null) {
            ppcMap = getBeansOfType(PropertiesLoaderSupport.class);
        }
        for (PropertiesLoaderSupport ppc : ppcMap.values()) {

            if (!(ppc instanceof PropertySourcesPlaceholderConfigurer)) {
                continue;
            }

            PropertySources pss = ((PropertySourcesPlaceholderConfigurer) ppc)
                    .getAppliedPropertySources();
            for (PropertySource ps : pss) {
                Object objValue = ps.getProperty(propertyKey);
                if (objValue != null) {
                    value = objValue.toString();
                    break;
                }
            }
            if (value != null) {
                break;
            }
        }
        return value;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws
            BeansException {
        this.configurableListableBeanFactory = beanFactory;

    }
}
