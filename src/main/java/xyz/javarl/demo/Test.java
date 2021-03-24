package xyz.javarl.demo;

import xyz.javarl.demo.service.UserService;
import xyz.javarl.spring.JavarlApplicationContext;

public class Test {
    public static void main(String[] args) {
        //创建bean
        JavarlApplicationContext applicationContext = new JavarlApplicationContext(AppConfig.class);  //初始化容器
        UserService userService = (UserService) applicationContext.getBean("userService");  //获取对象
//        UserService userService1 = (UserService) applicationContext.getBean("userService");  //获取对象
//        UserService userService2 = (UserService) applicationContext.getBean("userService");  //获取对象

        System.out.println(userService);
//        System.out.println(userService1);
//        System.out.println(userService2);

        userService.test();
    }
}
