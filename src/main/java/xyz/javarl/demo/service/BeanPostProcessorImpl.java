package xyz.javarl.demo.service;

import xyz.javarl.spring.BeanPostProcessor;
import xyz.javarl.spring.Component;


@Component
public class BeanPostProcessorImpl implements BeanPostProcessor {

    /**
     * 值得注意的时，我们可以看到两个处理方法都是可以返回object，实际上我们甚至可以通过这两个方法
     * 更改我们已经autowired注入好了的对象实例
     */

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化之前");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化之后");
        return bean;
    }
}
