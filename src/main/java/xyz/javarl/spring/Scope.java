package xyz.javarl.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)  //限定此扫描注解只能用于类上面
public @interface Scope {
    String value() default "singleton";  // 默认是单例

}
