package xyz.javarl.spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JavarlApplicationContext {

    //配置类
    private Class configClass;
    private Map<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();  //使用beanName和beanDefinition对象键值对存储
    private Map<String,Object> singletonObjects = new ConcurrentHashMap<>(); //单例池
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();  //处理器池

    public JavarlApplicationContext(Class configClass) {
        this.configClass = configClass;

        List<Class> classList = scan(configClass);  //扫描得到class对象

        //将扫描到的cls对象进行遍历，分别读取他们的注解属性，然后将注解属性写入到一个BeanDefinitionMap类中
        for (Class cls : classList) {
            if (cls.isAnnotationPresent(Component.class)) {  //如果存在Component注解我们才对其进行处理

                if (BeanPostProcessor.class.isAssignableFrom(cls)) {  //判断cls是否实现了BeanPostProcessor接口
                    try {
                        BeanPostProcessor instance = (BeanPostProcessor) cls.getDeclaredConstructor().newInstance();
                        beanPostProcessorList.add(instance);  //将这个实例添加到我们的处理器池中
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }


                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setBeanClass(cls);  //将cls对象存入到属性对象中
                Component componentAnnotation = (Component) cls.getAnnotation(Component.class);
                String beanName = componentAnnotation.value(); //获取到实例名称

                if (cls.isAnnotationPresent(Scope.class)) {  //如果有Scope注解
                    Scope scopeAnnotation = (Scope) cls.getAnnotation(Scope.class);
                    String scope = scopeAnnotation.value();  //获取得到此类实例创建形式是为单例还是原型的信息
                    beanDefinition.setScope(scope);
                } else {
                    //默认单例
                    beanDefinition.setScope("singleton");
                }

                beanDefinitionMap.put(beanName, beanDefinition);  //将获取到的class对象，整合好各种属性放入到map中
            }
        }
        instanceSingletonBean();


    }

    private List<Class> scan(Class configClass) {
        List<Class> classList = new ArrayList<>();  //用于存储扫描到的class文件
        //通过configClass获取注解进行扫描
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value(); //获取到ComponentScan注解上定义的扫描的包 xyz.javarl.demo.service
        path = path.replace(".","/");  //path转换成xyz/javarl/demo/service
        ClassLoader classLoader = configClass.getClassLoader();//获取到一个应用类加载器，应用类加载器可加载classpath中的字节码文件
        URL url = classLoader.getResource(path); //获取到我们classpath的url，此时的url就是指定文件夹E:\IdeaProjects\spring-learning\target\classes\xyz\javarl\demo\service\
        File file = new File(url.getFile());//获取这个文件夹
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                String absolutePath = f.getAbsolutePath();
                absolutePath = absolutePath.substring(absolutePath.indexOf("xyz"), absolutePath.indexOf(".class"));//xyz\javarl\demo\service\User截取成如下
                absolutePath = absolutePath.replace("\\",".");
                try {
                    Class<?> cls = classLoader.loadClass(absolutePath);  //加载对应路径下的class并获取返回相应的class对象
                    if (cls.isAnnotationPresent(Component.class)) {   //只有再存在Component注解的时候才将这个class对象添加进容器
                        classList.add(cls);  //将class对象添加到容器中
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return classList;
    }

    private void instanceSingletonBean() {  //实例化单例bean
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {  //如果是单例的 那么就在这里初始化，然后放入到单例池中
                if (!singletonObjects.containsKey(beanName)) {  //如果单例池中没有就创建
                    Object bean = doCreatBean(beanName, beanDefinition);
                    singletonObjects.put(beanName,bean);//将创建的实例存入到单例池中
                }
            }
        }
    }

    /**
     * 我们通过类型名和相关属性创建类实例
     * @param beanName  类名
     * @param beanDefinition 类的相关属性
     * @return
     */
    private Object doCreatBean(String beanName, BeanDefinition beanDefinition) {
        try {
            //1.实例化
            Class beanClass = beanDefinition.getBeanClass();
            Object bean = beanClass.getDeclaredConstructor().newInstance();  //通过反射调用到beanClass的构造器，然后实例化对象
            //            //这里拓展一下getDeclaredConstructor() 方法和getConstructor()方法的区别，后者只能获取public修饰的方法，而前者可以忽略修饰符

            //2.autowired注入
            Field[] fields = beanClass.getDeclaredFields();  //获取所有的属性
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) { //如果这个属性存在@Autowired注解
                    field.setAccessible(true); //允许强制反射
                    field.set(bean,getBean(field.getName()));
                }
            }

            //3. aware 将我们的变量名赋值给一个String beanName变量
            if (bean instanceof BeanNameAware) {  //由于这里我们不能确定这个bean对象到底是那个对象,所以我们需要向下转型
                ((BeanNameAware) bean).setBeanName(beanName);
            }

            //在初始化之前可以进行
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                bean = beanPostProcessor.postProcessBeforeInitialization(bean, beanName);
            }

            //4. 初始化阶段，在创建完对象之后，调用对象的初始化方法完成初始化操作
            if (bean instanceof InitializingBean) {
                ((InitializingBean) bean).afterPropertiesSet();
            }

            /*
                初始化之后可以进行
                我们的aop技术其实就是使用了后置处理器的方法，来创建代理。
             */

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                bean = beanPostProcessor.postProcessAfterInitialization(bean, beanName);
            }

            return bean;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }



        return null;
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {  //如果class对象池中有beanName
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("prototype")) {
                //创建bean
                Object bean = doCreatBean(beanName, beanDefinition);
                return bean;
            } else if (beanDefinition.getScope().equals("singleton")) {
                //单例--从单例池中读取实例
                Object bean = singletonObjects.get(beanName);
                if (bean == null) {  //这里有可能我们需要注入的对象初始化在我们现在这个对象的后面，所以我们在注入的时候提前初始化
                    Object new_bean = doCreatBean(beanName, beanDefinition);
                    singletonObjects.put(beanName,new_bean);  //将我们新创建的这个单例对象添加到单例池中
                    return new_bean;
                }
                return bean;
            }
        }
        return null;
    }

}
