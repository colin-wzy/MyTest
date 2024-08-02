//package cn.colin.db;
//
///**
// * @author Administrator
// */
//public class DataSourceContextHolder {
//    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
//
//    public static void setDataSourceKey(String dataSourceKey){
//        CONTEXT_HOLDER.set(dataSourceKey);
//    }
//
//    public static String getDataSourceKey(){
//        return CONTEXT_HOLDER.get();
//    }
//
//    public static void clearDataSourceKey(){
//        CONTEXT_HOLDER.remove();
//    }
//}
