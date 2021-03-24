package xyz.javarl.demo.service;
import xyz.javarl.spring.*;

@Component("userService")
//@Lazy
//@Scope("prototype")
public class UserService implements BeanNameAware, InitializingBean {

    @Autowired
    private User user;

    private String beanName;  //获取到bean的名字

    private String userName;

    public void test() {
        System.out.println(user);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        this.userName = "xxx";  //简单演实，其实一般在此阶段中，数据库/IO/网络的初始化都是在这个阶段完成的
    }
}
