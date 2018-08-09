package com.zkjl.posite_cloud.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author ydw
 * Created on 2018/5/17
 */
@Component
public class BeanNameContext implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Object getBean(String s) throws BeansException {
        return applicationContext.getBean(s);
    }

    public <T> T getBean(String s, Class<T> aClass) throws BeansException {
        return applicationContext.getBean(s, aClass);
    }

    public <T> T getBean(Class<T> aClass) throws BeansException {
        return applicationContext.getBean(aClass);
    }

    public Object getBean(String s, Object... objects) throws BeansException {
        return applicationContext.getBean(s, objects);
    }

    public <T> T getBean(Class<T> aClass, Object... objects) throws BeansException {
        return applicationContext.getBean(aClass, objects);
    }
}
