package spring.context;

import beans.BeanDefinition;
import beans.BeanPostProcessor;
import beans.BeanWrapper;
import spring.autowired.MyAutowired;
import spring.autowired.MyController;
import spring.autowired.MyService;
import spring.core.BeanFactory;
import support.BeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyApplicationContext implements BeanFactory {
    private String[] configLocations;

    private BeanDefinitionReader reader;
    //保存bean的配置信息
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
   //用来保证单例
    private Map<String,Object> beanCacheMap = new HashMap<>();
    //用来存储所有被代理过的对象
    private Map<String,BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();

    public MyApplicationContext(String ... configLocations){
        this.configLocations=configLocations;
        refresh();
    }

    public void refresh(){
        //1.定位
        this.reader = new BeanDefinitionReader(configLocations);

        //2.加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();
        //3.注册
        doRegistry(beanDefinitions);
        //4.lazy-init==false时，要是执行依赖注入，此处自动调用getBean()
        doAutoWired();
    }

    private void doAutoWired() {
        for(Map.Entry<String,BeanDefinition> beanDefinitionEntry:
                this.beanDefinitionMap.entrySet()){
            String beanName = beanDefinitionEntry.getKey();

            if(beanDefinitionEntry.getValue().isLazyInit()){
                getBean(beanName);
            }
        }
    }

    public void populateBean(String beanName,Object instance){
        Class clazz = instance.getClass();
        if(!(clazz.isAnnotationPresent(MyController.class) ||
            clazz.isAnnotationPresent(MyService.class)))
            return;
        Field[] fields = clazz.getDeclaredFields();
        for(Field field:fields){
            if(!field.isAnnotationPresent(MyAutowired.class)) continue;
            MyAutowired autowired = field.getAnnotation(MyAutowired.class);
            String autowiredBeanName = autowired.value().trim();
            if("".equals(autowiredBeanName)){
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);
            try {
                field.set(instance,this.beanWrapperMap.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    //将BeanDefinitions注册到beanDefinitionMap中
    private void doRegistry(List<String> beanDefinitions) {
        for(String className :beanDefinitions){
            //beanName有三种情况，第一种是默认是类名首字母小写
            //第二种是自定义名字，第三种是接口注入
            try {
                Class<?> beanClass = Class.forName(className);
                //如果是一个接口，则不能实例化，需用它的实现类来实例化
                if(beanClass.isInterface()) continue;
                BeanDefinition beanDefinition = reader.registerBean(className);
                if(beanDefinition!=null){
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),
                            beanDefinition);
                }
                Class<?>[] interfaces = beanClass.getInterfaces();
                for(Class<?> i:interfaces){
                    this.beanDefinitionMap.put(i.getName(),beanDefinition);
                }
                //容器初始化完毕
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
    }
    //通过BeanDefinition中的信息，然后通过反射机制创建一个实例并返回
    //Spring不会把最原始的对象放出去，而是会用一个BeanWrapper来进行包装
    //装饰器模式：保留原来的OOP关系，并可对其进行扩展，增强
    public Object getBean(String beanName){

        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        String className =beanDefinition.getBeanClassName();
        //生成通知事件
        BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
        Object instance = instantionBean(beanDefinition);
        if(null==instance) return null;
        beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
        BeanWrapper beanWrapper = new BeanWrapper(instance);
        beanWrapper.setPostProcessor(beanPostProcessor);
        this.beanWrapperMap.put(beanName,beanWrapper);
        beanPostProcessor.postProcessAfterInitialization(instance,beanName);
        populateBean(beanName,instance);
        return this.beanWrapperMap.get(beanName).getWrappedClass();
    }

        private Object instantionBean(BeanDefinition beanDefinition){
            Object instance = null;
            String className = beanDefinition.getBeanClassName();
            try {
                Class<?> clazz = Class.forName(className);
                if(!this.beanCacheMap.containsKey(className)){
                    instance = this.beanCacheMap.get(className);
                    this.beanCacheMap.put(className,instance);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return instance;

        }
}
