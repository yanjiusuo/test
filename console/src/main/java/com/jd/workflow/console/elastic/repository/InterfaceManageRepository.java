package com.jd.workflow.console.elastic.repository;

import com.jd.workflow.console.elastic.entity.InterfaceManageDoc;
import com.jd.workflow.console.elastic.entity.MethodManageDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InterfaceManageRepository<InterfaceManageDoc,String> extends CrudRepository<InterfaceManageDoc,String> {
    public Page<InterfaceManageDoc> searchInterface(String search,Integer type, int page, int size);

    public List<InterfaceManageDoc> listByIds(List<Long> ids);

}
