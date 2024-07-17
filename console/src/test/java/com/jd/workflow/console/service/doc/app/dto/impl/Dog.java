package com.jd.workflow.console.service.doc.app.dto.impl;

import com.jd.workflow.console.service.doc.app.dto.Animal;
import com.jd.workflow.console.service.doc.app.dto.AnimalType;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(parent = Animal.class)
public class Dog extends Animal {

    @Override
    public AnimalType getType() {
        return null;
    }
}
