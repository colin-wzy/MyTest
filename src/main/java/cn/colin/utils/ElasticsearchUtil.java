package cn.colin.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ElasticsearchUtil {

    private static ElasticsearchClient client;

    @Resource
    public void setClient(ElasticsearchClient client) {
        ElasticsearchUtil.client = client;
    }

    @SneakyThrows
    public static boolean createIndex(String indexName, IndexSettings settings, Map<String, Property> mappings) {
        CreateIndexRequest.Builder createIndexRequestBuilder = new CreateIndexRequest.Builder().index(indexName);
        if (settings != null) {
            createIndexRequestBuilder.settings(s -> s.index(settings));
        }
        if (mappings != null) {
            createIndexRequestBuilder.mappings(m -> m.properties(mappings));
        }
        return client.indices().create(createIndexRequestBuilder.build()).acknowledged();
    }

    // 删除索引
    @SneakyThrows
    public static boolean deleteIndex(String indexName) {
        DeleteIndexResponse response = client.indices().delete(d -> d.index(indexName));
        return response.acknowledged();
    }

    // 判断索引是否存在
    @SneakyThrows
    public static boolean indexExists(String indexName) {
        BooleanResponse response = client.indices().exists(e -> e.index(indexName));
        return response.value();
    }

    // 判断索引是否存在
    @SneakyThrows
    public static String findAllExists() {
        GetIndexResponse getIndexResponse = client.indices().get(e -> e.index("*"));
        return getIndexResponse.toString();
    }

    // 插入数据
    @SneakyThrows
    public static <T> String insertData(String indexName, String documentId, T data) {
        IndexResponse response = client.index(i -> i
                .index(indexName)
                .id(documentId)
                .document(data)
                .refresh(Refresh.True)
        );
        return response.id();
    }

    // 批量插入数据
    @SneakyThrows
    public static <T> BulkResponse insertBulkData(String indexName, Map<String, T> dataMap) {
        BulkRequest.Builder br = new BulkRequest.Builder();
        dataMap.forEach((id, data) -> br.operations(op ->
                op.index(idx -> idx.index(indexName).id(id).document(data))));
        return client.bulk(br.build());
    }

    // 获取数据
    @SneakyThrows
    public static <T> T getDataById(String indexName, String id, Class<T> clazz) {
        GetResponse<T> response = client.get(g -> g.index(indexName).id(id), clazz);
        return response.source();
    }

    // 匹配搜索，适用于text
    @SneakyThrows
    public static <T> List<T> matchData(String indexName, String field, String value, Class<T> clazz) {
        SearchResponse<T> response = client.search(s -> s.index(indexName)
                .query(q -> q.match(m -> m.field(field).query(value))), clazz);
        return response.hits().hits().stream().map(Hit::source).toList();
    }

    // 短语匹配搜索，适用于text
    @SneakyThrows
    public static <T> List<T> matchPhraseData(String indexName, String field, String value, Class<T> clazz) {
        SearchResponse<T> response = client.search(s -> s.index(indexName)
                .query(q -> q.matchPhrase(m -> m.field(field).query(value))), clazz);
        return response.hits().hits().stream().map(Hit::source).toList();
    }

    // 模糊搜索，适用于text
    @SneakyThrows
    public static <T> List<T> fuzzyData(String indexName, String field, String value, Class<T> clazz) {
        SearchResponse<T> response = client.search(s -> s.index(indexName)
                .query(q -> q.fuzzy(f -> f.field(field).value(value))), clazz);
        return response.hits().hits().stream().map(Hit::source).toList();
    }

    // 精确搜索，适用于key
    @SneakyThrows
    public static <T> List<T> termData(String indexName, String field, String value, Class<T> clazz) {
        SearchResponse<T> response = client.search(s -> s.index(indexName)
                .query(q -> q.term(f -> f.field(field).value(value))), clazz);
        return response.hits().hits().stream().map(Hit::source).toList();
    }

    // 自定义搜索
    @SneakyThrows
    public static <T> List<T> customSearchData(SearchRequest searchRequest, Class<T> clazz) {
        SearchResponse<T> response = client.search(searchRequest, clazz);
        return response.hits().hits().stream().map(Hit::source).toList();
    }
}
