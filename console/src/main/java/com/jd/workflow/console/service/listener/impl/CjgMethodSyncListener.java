package com.jd.workflow.console.service.listener.impl;

import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.doc.IDocReportService;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 项目名称：parent
 * 类 名 称：CjgMethodSyncListener
 * 类 描 述：TODO
 * 创建时间：2022-12-23 11:42
 * 创 建 人：wangxiaofei8
 */
public class CjgMethodSyncListener implements InterfaceChangeListener {

    @Autowired
    private MethodManageServiceImpl methodManageService;

    @Autowired
    private IDocReportService docReportService;

    @Autowired
    private CjgHelper cjgHelper;

    /*@PostConstruct
    public void init(){
        methodManageService.addListener(this);
        docReportService.addListener(this);
    }*/





    @Override
    public void onMethodAdd(InterfaceManage interfaceManage, List<MethodManage> methods) {
        for (MethodManage method : methods) {
            if(InterfaceTypeEnum.JSF.getCode().equals(interfaceManage.getType())
                    &&interfaceManage.getCjgAppId()!=null
              && InterfaceTypeEnum.JSF.getCode().equals(method.getType())
            ){
                cjgHelper.addMethod(interfaceManage,method, UserSessionLocal.getUser()!=null&&UserSessionLocal.getUser().getUserId()!=null
                        ?UserSessionLocal.getUser().getUserId():"systemReport");
            }
        }

    }


    @Override
    public void onMethodAfterUpdate(InterfaceManage interfaceManage, List<MethodManage> after,List<MethodManage> before){
        int i = 0;
        for (MethodManage method : after) {
            if(! InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
                continue;
            }
            MethodManage lastMethod = before.get(i);
            if(InterfaceTypeEnum.JSF.getCode().equals(interfaceManage.getType())
                    &&interfaceManage.getCjgAppId()!=null&&!lastMethod.getMethodCode().equals(method.getMethodCode()) ){
                //同步删除之前
                cjgHelper.removeMethod(interfaceManage,lastMethod,UserSessionLocal.getUser()!=null&&UserSessionLocal.getUser().getUserId()!=null
                        ?UserSessionLocal.getUser().getUserId():"systemReport");
                //同步新增修改的
                cjgHelper.addMethod(interfaceManage,method, UserSessionLocal.getUser()!=null&&UserSessionLocal.getUser().getUserId()!=null
                        ?UserSessionLocal.getUser().getUserId():"systemReport");
            }
            i++;
        }

    }


    @Override
    public void onMethodRemove(InterfaceManage interfaceManage, List<MethodManage> methods) {
        for (MethodManage method : methods) {
            if(! InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
                continue;
            }
            if(InterfaceTypeEnum.JSF.getCode().equals(interfaceManage.getType())
                    &&interfaceManage.getCjgAppId()!=null){
                cjgHelper.removeMethod(interfaceManage,method, UserSessionLocal.getUser()!=null&&UserSessionLocal.getUser().getUserId()!=null
                        ?UserSessionLocal.getUser().getUserId():"systemReport");
            }
        }
    }
}
