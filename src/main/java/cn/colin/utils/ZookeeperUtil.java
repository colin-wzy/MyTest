package cn.colin.utils;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Component;

@Component
public class ZookeeperUtil {
    private static CuratorFramework client;

    @Resource
    public void setCuratorFramework(CuratorFramework client) {
        ZookeeperUtil.client = client;
    }

    @SneakyThrows
    public static boolean createNode(String path, String data) {
        if (nodeExists(path)) {
            client.create().creatingParentsIfNeeded().forPath(path, data.getBytes());
            return true;
        } else {
            return false;
        }
    }

    @SneakyThrows
    public static String getNodeData(String path) {
        if (nodeExists(path)) {
            byte[] data = client.getData().forPath(path);
            return new String(data);
        } else {
            return null;
        }
    }

    @SneakyThrows
    public static boolean updateNodeData(String path, String data) {
        if (nodeExists(path)) {
            client.setData().forPath(path, data.getBytes());
            return true;
        } else {
            return false;
        }
    }

    @SneakyThrows
    public static boolean deleteNode(String path) {
        if (nodeExists(path)) {
            client.delete().deletingChildrenIfNeeded().forPath(path);
            return true;
        } else {
            return false;
        }
    }

    @SneakyThrows
    public static boolean nodeExists(String path) {
        return client.checkExists().forPath(path) != null;
    }
}
