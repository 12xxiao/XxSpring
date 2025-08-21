package com.xx.spring;

import com.xx.service.Test;

import java.beans.Introspector;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class XxApplicationContext {
    private Class configClass;

    //存储 Bean 的元信息（BeanDefinition），键是 Bean 的名称，值是 Bean 的定义信息。
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //单例Bean的缓存池（单例池）
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private ArrayList<BeanPostProcessor> beanPostProcessorsList = new ArrayList<>();

    public XxApplicationContext(Class configClass) {
        this.configClass = configClass;

        Test test = new Test();

        //扫描，发现BeanDefinition对象，并存到beanDefinitionMap
        //判断给得这个类有没有@ComponentScan注解
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);

            String path = componentScanAnnotation.value();//扫描路径 com.xx.service
            //并不是扫描源文件service下的三个java文件，而是他们的java文件，而是class文件

            path = path.replace(".", "/");//com/xx/service

            ClassLoader classLoader = XxApplicationContext.class.getClassLoader();//获得类加载器
            URL resource = classLoader.getResource(path);//file:/D:/IdeaProjects/XxSpring/out/production/XxSpring/com/xx/service

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

                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            /**
                             * 在spring容器启动的过程中间，会去检查当前指定这个包（service）下面当前定义的bean里面有没有某个bean的类型是BeanPostProcessor
                             * 如果有，额外的提出来，把它Bean对象创建出来，把它加到beanPostProcessorList里面
                             */
                            if (clazz.isAnnotationPresent(Component.class)) {
                                //这个类（clazz）是否实现继承了BeanPostProcessor
                                //不用instanceof：针对对象去判断是不是某个类型
                                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    BeanPostProcessor instance = (BeanPostProcessor) clazz.newInstance();
                                    beanPostProcessorsList.add(instance);
                                }


                                Component component = clazz.getAnnotation(Component.class);
                                String beanName = component.value();

                                if (beanName.equals("")) {
                                    //spring里面默认情况下的Bean的名字生成器
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }

                                //有Component注解，是一个Bean
                                //生成BeanDefinition对象
                                BeanDefinition beanDefinition = new BeanDefinition();
                                //Bean的类型
                                beanDefinition.setType(clazz);

                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    //看看Scope注解里面定义了什么值
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                } else {
                                    //Bean的作用域
                                    beanDefinition.setScope("singleton");
                                }

                                beanDefinitionMap.put(beanName, beanDefinition);

                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
        }

        //实例化单例Bean
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);//将单例Bean存到map里面
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {

        //创建对象需要知道是哪个类
        Class clazz = beanDefinition.getType();

        try {
            Object instance = clazz.getConstructor().newInstance();

            //依赖注入
            //并不需要给所有属性赋值，只需要给加了@autowired的赋值
            //先拿到当前类的所有属性
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Autowired.class)) {
                    f.setAccessible(true);//增加访问权限
                    f.set(instance, getBean(f.getName()));
                }
            }

            //Aware
            if (instance instanceof BeanNameAware) {

                //spring告诉某个东西，给你当前这个Bean
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorsList) {
                beanPostProcessor.postProcessBeforeInitialization(beanName, instance);
            }

            // 初始化
            if (instance instanceof InitializingBean) {
                //调用当前Bean的afterPropertiesSet方法
                ((InitializingBean) instance).afterPropertiesSet();
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorsList) {
                beanPostProcessor.postProcessAfterInitialization(beanName, instance);
            }

            //BeanPostProcessor 初始化后 Aop

            return instance;

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    public Object getBean(String beanName) {
        //根据名字beanName，找到那个类
        //Bean有单例和多例，根据名字，判断是单例还是多例
        //如果是单例，从某个缓存池中去拿
        //如果是多例，去新创建一个

        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScope();
            if (scope.equals("singleton")) {
                Object bean = singletonObjects.get(beanName);
                if (bean == null) {
                    Object o = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, o);
                }
                return bean;
            } else {
                //多例
                return createBean(beanName, beanDefinition);
            }
        }
    }
}
