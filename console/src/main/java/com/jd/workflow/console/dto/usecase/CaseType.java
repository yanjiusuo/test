package com.jd.workflow.console.dto.usecase;

import lombok.Data;

@Data
public class CaseType {
    //    {
//        "jsf": {
//        "selected": [
//        "123_456_789","2432-234-213"
//        ]
//    },
//        "http": {
//        "selected": [
//        "123_456_789"
//        ]
//    }
//    }
    private CaseSelected jsf;
    private CaseSelected http;
}
