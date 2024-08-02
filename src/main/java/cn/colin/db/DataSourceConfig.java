//package cn.colin.db;
//
//import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
//import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author weiyang
// * @date 2024/5/28 9:40
// */
//@Configuration
//@EnableTransactionManagement
//public class DataSourceConfig {
//    public static final String DB_WZY = "wzy";
//
//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource.wzy")
//    public DataSource wzy() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean
//    @Primary
//    public DataSource routingDataSource() {
//        Map<Object, Object> targetDataSource = new HashMap<>(2);
//        targetDataSource.put(DB_WZY, wzy());
//        RoutingDataSource routingDataSource = new RoutingDataSource(DB_WZY, targetDataSource);
//        routingDataSource.setDefaultTargetDataSource(wzy());
//        return routingDataSource;
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager(DataSource routingDataSource) {
//        return new DataSourceTransactionManager(routingDataSource);
//    }
//
//    @Bean
//    public MybatisPlusInterceptor mybatisPlusInterceptor() {
//        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
//        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
//        return interceptor;
//    }
//}
