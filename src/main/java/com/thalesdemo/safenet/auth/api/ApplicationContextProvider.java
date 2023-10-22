// package com.thalesdemo.safenet.auth.api;

// import org.springframework.beans.BeansException;
// import org.springframework.context.ApplicationContext;
// import org.springframework.context.ApplicationContextAware;
// import org.springframework.context.ConfigurableApplicationContext;
// import org.springframework.stereotype.Component;

// @Component
// public class ApplicationContextProvider implements ApplicationContextAware {

//     private static ApplicationContext context;

//     @Override
//     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//         context = applicationContext;
//     }

//     public static ApplicationContext getContext() {
//         return context;
//     }

//     public static void close() {
//         if (context != null && context instanceof ConfigurableApplicationContext) {
//             ((ConfigurableApplicationContext) context).close();
//         }
//     }
// }
