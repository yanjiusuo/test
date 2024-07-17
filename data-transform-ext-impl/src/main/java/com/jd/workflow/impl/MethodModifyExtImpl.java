package com.jd.workflow.impl;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6
 */

import com.jd.matrix.sdk.annotation.Extension;
import com.jd.workflow.DataTransformApp;
import com.jd.workflow.domain.MethodModifyModel;
import com.jd.workflow.matrix.ext.matrix1.MethodModifyExt;
import com.jd.workflow.matrix.service.SpaceLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6
 */
@Slf4j
@Extension(code = DataTransformApp.CODE)
public class MethodModifyExtImpl implements MethodModifyExt {

    @Autowired
    private SpaceLogService spaceLogService;

    @Override
    public void onMethodBeforeUpdate(MethodModifyModel methodModifyModel) {
        log.info("MethodModifyExtImpl onMethodBeforeUpdate");

        log.info("MethodModifyExtImpl onMethodBeforeUpdate");
    }

    @Override
    public void onMethodAfterUpdate(MethodModifyModel methodModifyModel) {
        log.info("MethodModifyExtImpl onMethodAfterUpdate");
        spaceLogService.createSpaceMethodLog(methodModifyModel.getInterfaceInfo().getId(), methodModifyModel.getErp(), "（扩展点日志）修改了接口：" + methodModifyModel.getMethodInfo().getName());
        log.info("MethodModifyExtImpl onMethodAfterUpdate success");
    }
}
