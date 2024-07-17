package com.jd.workflow.console.fps.impl;

import com.jd.matrix.generic.annotation.GenericExtensionImpl;
import com.jd.workflow.console.fps.DataTypeCheckSPI;
import com.jd.workflow.console.fps.dto.DataType;
import com.jd.workflow.soap.common.exception.BizException;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.util.Objects;

/**
 * @author wufagang
 * @description 文件解析 解析校验实现类
 * @date 2023年04月21日 15:56
 */
@GenericExtensionImpl(bizCode = "parseFile", bizName = "文件解析校验",group ="file",index =1)
public class ParseFileDataTypeCheckImpl implements DataTypeCheckSPI {
    private final long maxFileSize = 10 *1024 *1024L;
    @Override
    public String checkData(DataType dataType) {
        MultipartFile file;
        if(Objects.isNull(file=dataType.getFile())){
            throw new BizException("文件不能为空");
        }
        if(file.getSize() > maxFileSize) {
            throw new BizException("上传的文件大小不能超过10M");
        }
        // 暂时不支持 csv文件
        if(file instanceof CommonsMultipartFile) {
            String name = ((CommonsMultipartFile) file).getFileItem().getName();
            if(name == null || (!name.endsWith(".json")&&!name.endsWith(".yml")&&!name.endsWith(".yaml"))) {
                throw new BizException("仅支持.json,.yml,yaml的扩展名文件");
            }
            try {
                return IOUtils.toString(file.getInputStream(), "utf-8");
            } catch (Exception e) {
                throw new BizException("文件转换异常");
            }
        }
        return null;
    }

}
