package com.jd.workflow.console.service.doc.app.jsf;

import com.jd.workflow.console.service.doc.app.dto.AllTypeEntity;
import com.jd.workflow.console.service.doc.app.dto.Person;

public interface JsfTestInterface {
    public AllTypeEntity allType(AllTypeEntity allTypeEntity);

    public Long save(Person person);


}
