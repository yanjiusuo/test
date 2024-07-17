package com.jd.workflow.console.fps.impl;

import com.jd.matrix.generic.annotation.GenericExtensionImpl;
import com.jd.workflow.console.fps.DataTypeCheckSPI;
import com.jd.workflow.console.fps.dto.DataType;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.exception.BizException;
import org.apache.commons.lang.StringUtils;

/**
 * @author wufagang
 * @description url 解析校验实现类
 * @date 2023年04月21日 15:56
 */
@GenericExtensionImpl(bizCode = "parseUrl", bizName = "URL解析校验",group ="url",index =1)
public class ParseUrlDataTypeCheckImpl implements DataTypeCheckSPI {

    static final String pattern = "(http|https)://.*";
    @Override
    public String checkData(DataType dataType) {
        String url;
        if(StringUtils.isEmpty((url = dataType.getUrl()))){
            throw new BizException("Url 不能为空");
        }
        if(!checkUrl(url)) {
            throw new BizException("填写Url格式不正确！");
        }
        RequestClient client = new RequestClient();
        final String result = client.get(url, null);

        return result;
    }

    public boolean checkUrl(String url){
        return url.matches(pattern);
    }
}
