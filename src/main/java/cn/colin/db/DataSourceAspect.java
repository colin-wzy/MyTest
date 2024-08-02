//package cn.colin.db;
//
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
///**
// * @author Administrator
// */
//@Aspect
//@Component
//@Order(1)
//public class DataSourceAspect {
//
//    @Before("@within(dataSource) || @annotation(dataSource)")
//    public void switchDataSource(DataSource dataSource) {
//        DataSourceContextHolder.setDataSourceKey(dataSource.value());
//    }
//}
