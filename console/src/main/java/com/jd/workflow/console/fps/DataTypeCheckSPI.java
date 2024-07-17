package com.jd.workflow.console.fps;

import com.jd.matrix.generic.annotation.GenericExtension;
import com.jd.matrix.generic.annotation.GenericSPI;
import com.jd.workflow.console.fps.dto.DataType;

/**
 * @author wufagang
 * @description 根据不同数据类型校验数据格式
 * @date 2023年04月21日 15:43
 */
@GenericSPI(domainCode = "test")
public interface DataTypeCheckSPI {

    String SPI_CHECK_DATA_TYPE="spi_check_data_type";

    @GenericExtension(code = SPI_CHECK_DATA_TYPE,name = "解析文件检查")
    String checkData(DataType dataType);
}
