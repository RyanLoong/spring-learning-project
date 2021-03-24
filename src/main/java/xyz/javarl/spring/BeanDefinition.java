package xyz.javarl.spring;

public class BeanDefinition {

    private Class beanClass;  //bean的class对象
    private String scope;
    private Boolean isLazy;  //是否是懒加载

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Boolean getLazy() {
        return isLazy;
    }

    public void setLazy(Boolean lazy) {
        isLazy = lazy;
    }
}
