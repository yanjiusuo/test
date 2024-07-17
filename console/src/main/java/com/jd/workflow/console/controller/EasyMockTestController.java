package com.jd.workflow.console.controller;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.mock.UpdateDeliveryDto;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.remote.EasyMockRemoteService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.y.model.vo.JsfInterfaceOpenVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;

/**
 * 集成easymock
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/easyMock")
@Profile("erpLogin")
@UmpMonitor
@Api(tags = "集成easymock")
public class EasyMockTestController {

    @Autowired
    EasyMockRemoteService testEasyMockRemoteService;
    @Autowired
    EasyMockRemoteService onlineEasyMockRemoteService;
    @Autowired
    IInterfaceManageService interfaceManageService;

    @Autowired
    MethodManageServiceImpl methodManageService;

    public EasyMockRemoteService remoteService(String env){
        Guard.notEmpty(env,"环境不可为空");
        if("test".equals(env)){
            return testEasyMockRemoteService;
        }else{
            return onlineEasyMockRemoteService;
        }
    }

    /**
     * 更新easyMock接口透传信息
     * @param dto
     * @return
     */
    @PostMapping("/updateDelivery")
    public CommonResult<Boolean> updateDelivery(@RequestBody @Valid UpdateDeliveryDto dto,String env){
        InterfaceManage interfaceManage = interfaceManageService.getById(dto.getInterfaceId());
        Guard.notEmpty(interfaceManage,"无效的接口id");
        remoteService(env).updateDeliverInfo(interfaceManage,dto.getDeliverToken(),dto.getDeliverAlias());
        return CommonResult.buildSuccessResult(true);
    }
    /**
     * 更新easyMock接口透传信息
     * @param methodId
     * @param deliveryFlag true 开启透传
     * @return
     */
    @PostMapping("/switchMethod")
    public CommonResult<Boolean> switchMethod(String methodId,boolean deliveryFlag,String env){
        MethodManage methodManage = methodManageService.getById(methodId);
        Guard.notEmpty(methodManage,"无效的方法id");

        remoteService(env).methodSwitch(methodManage,deliveryFlag);
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/queryMockInterface")
    public CommonResult<JsfInterfaceOpenVO> queryMockInterface(String interfaceId){
        return CommonResult.buildSuccessResult(testEasyMockRemoteService.queryJsfMockInterfaceByInterfaceId(interfaceId));
    }
    /**
     * 更新easyMock接口透传信息
     * @param methodId
     *
     * @return
     */
    @GetMapping("/methodIsSwitch")
    public CommonResult<Boolean> easyMockMethodIsSwitch(String interfaceId,String methodId,String env){
        return CommonResult.buildSuccessResult(remoteService(env).methodIsSwitch(methodId));
    }

    /**
     * 校验mock方法是否有效
     * @param methodId
     * @return
     */
    @GetMapping("/validateMethodIsValid")
    public CommonResult<EasyMockRemoteService.SyncMockDataResult> validateMethodIsValid(String methodId,String env){

        MethodManage methodManage = methodManageService.getById(methodId);

        Guard.notEmpty(methodManage,"无效的方法id");
        InterfaceManage interfaceManage = interfaceManageService.getById(methodManage.getInterfaceId());
        if(interfaceManage.getAppId() == null){
            throw new BizException("接口未关联应用，无法生成mock前缀");
        }
        methodManageService.initMethodRefAndDelta(Collections.singletonList(methodManage),interfaceManage.getAppId());
        Guard.notEmpty(interfaceManage,"无效的接口id");


        final EasyMockRemoteService.SyncMockDataResult result = remoteService(env).syncMockData(interfaceManage, methodManage);
        /*if(result.getWarnInfo() != null){
            CommonResult ret = CommonResult.buildSuccessResult(false);
            ret.setMessage(result.getWarnInfo());
            return ret;
        }*/

        return CommonResult.buildSuccessResult(result);

    }
    public static boolean isValidRemoteAddress(InetSocketAddress remoteAddress,InetSocketAddress sourceAddress){
        InetAddress host = null;

        try {
            Socket socket = new Socket();

            try {
                socket.bind(sourceAddress);
                socket.connect(remoteAddress, 1000);
                host = socket.getLocalAddress();
            } finally {
                try {
                    socket.close();
                } catch (Throwable var10) {
                }

            }
        } catch (Exception var12) {
            log.error("Can not connect to host "+remoteAddress.toString()+", cause by :"+ var12.getMessage());
            return false;
        }

        return true;
    }
    @GetMapping("/checkNetworkValid")
    public CommonResult<Boolean> checkNetworkValid(String sourceIp,String targetIp,Integer sourcePort,Integer targetPort){
        final boolean validRemoteAddress = isValidRemoteAddress(new InetSocketAddress(targetIp, targetPort),new InetSocketAddress(sourceIp,sourcePort));
        return CommonResult.buildSuccessResult(validRemoteAddress);
    }

    @GetMapping("/moveMockDataToNewPlatform")
    public CommonResult<Boolean> moveMockDataToNewPlatform(){
        testEasyMockRemoteService.moveMockDataToNewPlatform();
        return CommonResult.buildSuccessResult(true);
    }
}
