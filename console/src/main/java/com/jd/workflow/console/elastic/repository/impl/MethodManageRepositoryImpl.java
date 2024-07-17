package com.jd.workflow.console.elastic.repository.impl;

import com.jd.workflow.console.elastic.entity.InterfaceManageDoc;
import com.jd.workflow.console.elastic.entity.MethodManageDoc;
import com.jd.workflow.console.elastic.repository.InterfaceManageRepository;
import com.jd.workflow.console.elastic.repository.MethodManageRepository;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.StringHelper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.repository.support.AbstractElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.MappingElasticsearchEntityInformation;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


@Repository
public class MethodManageRepositoryImpl extends AbstractElasticsearchRepository<MethodManageDoc,String> implements MethodManageRepository<MethodManageDoc,String> {
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
    public MethodManageRepositoryImpl(ElasticsearchOperations elasticsearchOperations, SimpleElasticsearchMappingContext simpleElasticsearchMappingContext) {
        super(createElasticsearchEntityInformation(simpleElasticsearchMappingContext), elasticsearchOperations);
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * 创建ElasticsearchEntityInformation对象，该对象实现对索引对象相关信息的读取
     */
    private static ElasticsearchEntityInformation<MethodManageDoc, String> createElasticsearchEntityInformation(SimpleElasticsearchMappingContext ctx) {

        MappingElasticsearchEntityInformation<MethodManageDoc, String> mapping = new MappingElasticsearchEntityInformation<MethodManageDoc,String>(
                (ElasticsearchPersistentEntity<MethodManageDoc>) ctx.getPersistentEntity(MethodManageDoc.class));
        return mapping;
    }

 
    @Override
    protected String stringIdRepresentation(String s) {
        return s;
    }

    @Override
    public Page<MethodManageDoc> searchMethod(List<Long> interfaceIds,Integer type, String search, int page, int size) {
        List<String> searchStrs = StringHelper.split(search.trim(), " ");
        searchStrs = searchStrs.stream().filter(item->!StringHelper.isBlank(item)).collect(Collectors.toList());

        Guard.assertTrue(page > 0,"页数必须大0");
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.termQuery("type",type));
        queryBuilder.must(QueryBuilders.termQuery("visibility","0"));


        queryBuilder.minimumShouldMatch(1);
        if(interfaceIds!=null && !interfaceIds.isEmpty()){
            queryBuilder.should(QueryBuilders.termsQuery("interfaceId",interfaceIds).boost(3.0f));
        }
        queryBuilder.should(QueryBuilders.matchPhraseQuery("name",search));

        queryBuilder.should(QueryBuilders.termsQuery("methodCode",searchStrs));
        queryBuilder.should(QueryBuilders.matchPhraseQuery("docInfo",search));
        queryBuilder.should(QueryBuilders.matchPhraseQuery("path",search));

        queryBuilder.should(QueryBuilders.matchPhraseQuery("content",search).boost(0.5f));
        Page<MethodManageDoc> result = search(queryBuilder, PageRequest.of(page-1, size));
        return result;
    }

    @Override
    public List<MethodManageDoc> listByIds(List<Long> ids) {
        TermsQueryBuilder builder = QueryBuilders.termsQuery("interfaceId", ids);

        Iterator<MethodManageDoc> iterator = search(builder).iterator();
        List<MethodManageDoc> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }
}
