package com.xx.spring;

import com.xx.service.Test;

import java.io.File;
import java.net.URL;

public class XxApplicationContext {
    private Class configClass;

    public XxApplicationContext(Class configClass) {
        this.configClass = configClass;
1
        Test test = new Test();
        //扫描
        //判断给得这个类有没有ComponentScan注解
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);

            String path = componentScanAnnotation.value();//扫描路径 com.xx.service
            //并不是扫描源文件service下的三个java文件，而是他们的java文件，而是class文件

            path = path.replace(".", "/");//com/xx/service

            ClassLoader classLoader = XxApplicationContext.class.getClassLoader();//获得类加载器
            URL resource = classLoader.getResource(path);

            File file = new File(resource.getFile());//File对象既可以表示一个文件，也可以表示一个目录

            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    String fileName = f.getAbsolutePath();
//                    System.out.println(fileName);

                    if (fileName.endsWith(".class")) {
                        //com\xx\service\UserService
                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        className = className.replace("\\", ".");
                        //判断一个类是不是一个bean：上面有没有Component注解，用反射
                        //用反射需要拿到Class对象

                        System.out.println(className);
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {
                                // Bean
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }


                    }
                }

            }
        }
    }

    public Object getBean(String beanName) {
        return null;
    }
}
