package com.jd.workflow.console.elastic.repository;

import com.jd.workflow.console.elastic.entity.InterfaceManageDoc;
import com.jd.workflow.console.elastic.entity.MethodManageDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MethodManageRepository<MethodManageDoc,String> extends CrudRepository<MethodManageDoc,String> {
    public Page<MethodManageDoc> searchMethod(List<Long> interfaceIds,Integer type, String search, int page, int size);

    public List<MethodManageDoc> listByIds(List<Long> interfaceIds);
}
