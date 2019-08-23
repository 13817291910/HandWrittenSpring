package support;

import beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
//用来对配置文件进行查找，读取和解析
public class BeanDefinitionReader {
    private Properties config = new Properties();

    private List<String> registryBeanClasses = new ArrayList<>();
    //在配置文件中，用来获取自动扫描的包名的key
    private final String SCAN_PACKAGE ="scanPackage";

    public BeanDefinitionReader(String... locations){
        InputStream is = this.getClass().getClassLoader().
                getResourceAsStream(locations[0].replace("classpath",""));
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null!=is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<String> loadBeanDefinitions(){
        return null;
    }

    //每注册一个className，就返回一个BeanDefinition,只是为了对配置信息做一个包装
    public BeanDefinition registerBean(String className){
        if(this.registryBeanClasses.contains(className)){
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClassName(className);
            //在IOC中首字母小写
            beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".")+1)));
        }

        return null;
    }

    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    //递归扫描所有相关联的class，并且保存到一个list中
    public void doScanner(String packageName){
        URL url = this.getClass().getClassLoader().getResource("/"+packageName);

        File classDir = new File(url.getFile());

        for(File file:classDir.listFiles()){
            if(file.isDirectory()){
                doScanner(packageName+"."+file.getName());
            }else{
                registryBeanClasses.add(packageName+"."+file.getName().replace(".class",""));
            }
        }


    }

    public Properties getConfig(){
        return this.config;
    }


}
