package com.jd.workflow.console.elastic.repository.impl;

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.elastic.entity.InterfaceManageDoc;
import com.jd.workflow.console.elastic.entity.MethodManageDoc;
import com.jd.workflow.console.elastic.repository.InterfaceManageRepository;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.StringHelper;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.repository.support.AbstractElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.MappingElasticsearchEntityInformation;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * es基本query的使用：https://blog.csdn.net/u014646662/article/details/89010759
 */

@Repository
public class InterfaceManageRepositoryImpl extends AbstractElasticsearchRepository<InterfaceManageDoc,String> implements InterfaceManageRepository<InterfaceManageDoc,String> {
    /**
     * 当AbstractElasticsearchRepository提供的方法不足以完成全部功能时，即可通过该对象自定义
     * 操作行为。
     */
    private ElasticsearchOperations elasticsearchOperations;

    /**
     * 注入ElasticsearchOperations，并实例化BookElasticsearchRepository
     * 这里注入ElasticsearchOperations对象就是在application.yml中配置的参数自动装载的对象
     * <p>
     * 坑二：必须super(createElasticsearchEntityInformation(), elasticsearchOperations)，否则
     * 会报错
     */
    @Autowired
    public InterfaceManageRepositoryImpl(ElasticsearchOperations elasticsearchOperations,SimpleElasticsearchMappingContext simpleElasticsearchMappingContext) {
        super(createElasticsearchEntityInformation(simpleElasticsearchMappingContext), elasticsearchOperations);
        this.elasticsearchOperations = elasticsearchOperations;


    }

    /**
     * 创建ElasticsearchEntityInformation对象，该对象实现对索引对象相关信息的读取
     */
    private static ElasticsearchEntityInformation<InterfaceManageDoc, String> createElasticsearchEntityInformation(SimpleElasticsearchMappingContext ctx) {

        MappingElasticsearchEntityInformation<InterfaceManageDoc, String> mapping = new MappingElasticsearchEntityInformation<InterfaceManageDoc,String>(
                (ElasticsearchPersistentEntity<InterfaceManageDoc>) ctx.getPersistentEntity(InterfaceManageDoc.class));

        return mapping;
    }


    @Override
    protected String stringIdRepresentation(String s) {
        return s;
    }

    @Override
    public Page<InterfaceManageDoc> searchInterface(String search,Integer type,int page,int size) {
        List<String> searchStrs = StringHelper.split(search.trim(), " ");
        if(searchStrs.size() > 2){
            throw new BizException("最多只支持2个组合条件!");
        }

        searchStrs = searchStrs.stream().filter(item->!StringHelper.isBlank(item)).collect(Collectors.toList());
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.termQuery("type",type+""));
        queryBuilder.must(QueryBuilders.termQuery("visibility","0"));

        if(searchStrs.size() == 2){
            queryBuilder.must().add(newQueryBuilder(searchStrs.get(0)));
            queryBuilder.must().add(newQueryBuilder(searchStrs.get(1)));
        }else{
            queryBuilder.must().add(newQueryBuilder(search));
        }

        PageRequest pagable = PageRequest.of(page - 1, size);

        Page<InterfaceManageDoc> result = search(queryBuilder, pagable);
        return result;
    }
    private QueryBuilder newQueryBuilder(String search){
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.should(QueryBuilders.termsQuery("serviceCode",search.toLowerCase()));
        queryBuilder.should(QueryBuilders.matchPhraseQuery("name",search)); // text
        queryBuilder.should(QueryBuilders.matchQuery("cloudFileTags",search)); // text
        queryBuilder.should(QueryBuilders.matchPhraseQuery("appName",search).boost(3.0f)); // text
        queryBuilder.should(QueryBuilders.matchPhraseQuery("appCode",search).boost(3.0f));// text
        queryBuilder.should(QueryBuilders.matchPhraseQuery("deptName",search)); // text
        queryBuilder.should(QueryBuilders.termsQuery("adminCode",search).boost(1.0f));
        queryBuilder.minimumShouldMatch(1);
        return queryBuilder;
    }

    @Override
    public List<InterfaceManageDoc> listByIds(List<Long> ids) {
        TermsQueryBuilder builder = QueryBuilders.termsQuery("interfaceId", ids);

        Iterator<InterfaceManageDoc> iterator = search(builder).iterator();
        List<InterfaceManageDoc> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    public static void main(String[] args) {
        String string = QueryBuilders.matchPhraseQuery("appName", "search").toString();
        System.out.println(string);
    }

}
