//package cn.colin.db;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
//
//import java.util.Map;
//
///**
// * @author Administrator
// */
//public class RoutingDataSource extends AbstractRoutingDataSource {
//    private final String defaultDataSourceKey;
//
//    public RoutingDataSource(String defaultDataSourceKey, Map<Object, Object> targetDataSource) {
//        this.defaultDataSourceKey = defaultDataSourceKey;
//        super.setTargetDataSources(targetDataSource);
//    }
//
//    @Override
//    protected Object determineCurrentLookupKey() {
//        String dataSource = DataSourceContextHolder.getDataSourceKey();
//        return StringUtils.isEmpty(dataSource) ? defaultDataSourceKey : dataSource;
//    }
//}
