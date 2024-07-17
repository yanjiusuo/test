package com.jd.workflow.console.service.doc.app.dto;

import com.jd.workflow.console.service.doc.app.dto.impl.Cat;
import com.jd.workflow.console.service.doc.app.dto.impl.Dog;
import io.swagger.annotations.ApiModel;

@ApiModel(discriminator = "type", subTypes = {Dog.class, Cat.class})
public abstract class Animal {
    private String name;
    public abstract AnimalType getType();

    public static void main(String[] args) {

    }
}
