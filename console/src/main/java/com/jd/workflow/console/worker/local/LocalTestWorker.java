package com.jd.workflow.console.worker.local;

import cn.hutool.http.HttpRequest;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author sunchao81
 * @date 2021/8/11 16:56
 * @description
 */
@Service
@Slf4j
public class LocalTestWorker {

    /**
     *
     */
    final String url = "http://api-gateway.jd.com/jacp/api/rest/v2/repo/card/query";
    /**
     *
     */
    final String appCode = "cjg";
    /**
     *
     */
    final String appSecret = "36836bb0-6ae0-4b8d-ae0e-89725137d041";





    /**
     *
     */
    @Resource
    RequirementInfoService requirementInfoService;



    /**
     * 每次处理个数
     */
    private static final Integer PER_NUMBER = 1;


    /**
     * 需求空间刷新行云编码
     */
    @XxlJob("relatedRequirementCode")
    public ReturnT<String> relatedRequirementCode() {
        try{
            List<RequirementInfo> list = requirementInfoService.lambdaQuery().eq(RequirementInfo::getYn, DataYnEnum.VALID.getCode()).isNotNull(RequirementInfo::getGitUrl)
                    .isNotNull(RequirementInfo::getGitBranch).isNull(RequirementInfo::getRelatedRequirementCode)
                    .orderBy(true,false,RequirementInfo::getId).last("limit " + PER_NUMBER).list();
            for (RequirementInfo requirementInfo : list) {
                if(requirementInfo.getGitUrl().startsWith("git@coding.jd.com:") && requirementInfo.getGitUrl().endsWith(".git")
                    && StringUtils.isNotBlank(requirementInfo.getCreator())){
                    Long timestamp = System.currentTimeMillis();
                    String plainText = appCode + timestamp + appSecret;
                    String signature = DigestUtils.md5DigestAsHex(plainText.getBytes());
                    HttpRequest withHeader = HttpRequest.post(url).header("appId", appCode).header("timestamp", timestamp + "")
                            .header("signature", signature).header("optErp", "org.lht");

                    String repTemp = "https://coding.jd.com/%s";
                    String bodyTemp ="{\"repUrl\":\"%s\",\"branchName\":\"%s\"}";
                    String key = requirementInfo.getGitUrl().replace("git@coding.jd.com:", "").replace(".git", "");
                    String result = withHeader.body(String.format(bodyTemp,String.format(repTemp,"cjg_fellow_man/cjg_business_identity"),"master")).execute().body();
                    String result1 = withHeader.body(String.format(bodyTemp,String.format(repTemp,"cjg_fellow_man/cjg_business_identity"),"feat-xxj-20240524-hotfix")).execute().body();
                    String body = withHeader.body(String.format(bodyTemp, String.format(repTemp, key), requirementInfo.getGitBranch())).execute().body();
                    log.error("###"+result);
                }else {
                    requirementInfo.setRelatedRequirementCode("-");
                }
            }
            requirementInfoService.updateBatchById(list);
            return  ReturnT.SUCCESS;
        }catch (Exception e){
            e.printStackTrace();
            return  ReturnT.FAIL;
        }

    }

}
