package xyz.javarl.spring;

public interface BeanPostProcessor {

    /**
     *  bean前置和后置处理器
     */

    Object postProcessBeforeInitialization(Object bean, String beanName);  //用于初始化之前

    Object postProcessAfterInitialization(Object bean, String beanName);  //用于初始化之后
}
