package com.jd.workflow.console.base;

import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import lombok.Data;

/**
 *
 * @date: 2022/5/11 18:19
 * @author wubaizhao1
 */
@Data
public class ServiceException extends RuntimeException {
	/**
	 *
	 */
	private Integer code;

	/**
	 *
	 * @param message
	 */
	public ServiceException(String message) {
		super(message);
	}

	/**
	 *
	 * @param code
	 * @param info
	 */
	public ServiceException(Integer code, String info) {
		super(info);
		this.code = code;
	}

	/**
	 *
	 * @param serviceErrorEnum
	 */
	public ServiceException(ServiceErrorEnum serviceErrorEnum) {
		super(serviceErrorEnum.getMsg());
		this.code = serviceErrorEnum.getCode();
	}

	/**
	 *
	 * @param cause
	 */
	public ServiceException(Throwable cause) {
		super(cause);
	}

	/**
	 *
	 * @param message
	 * @param cause
	 */
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 *
	 * @param code
	 * @param info
	 * @param cause
	 */
	public ServiceException(Integer code, String info, Throwable cause) {
		super(info, cause);
		this.code = code;
	}

	/**
	 *
	 *
	 * @param serviceErrorEnum
	 * @param info
	 */
	public ServiceException(ServiceErrorEnum serviceErrorEnum,String... info) {
		super(String.format(serviceErrorEnum.getMsg(),info));
		this.code = serviceErrorEnum.getCode();
	}

	public static ServiceException withCommon(String info){
		return new ServiceException(ServiceErrorEnum.COMMON_EXCEPTION,info);
	}
	public static ServiceException with(ServiceErrorEnum serviceErrorEnum,String info){
		return new ServiceException(serviceErrorEnum,info);
	}
	public static ServiceException with(ServiceErrorEnum serviceErrorEnum) {
		return new ServiceException(serviceErrorEnum);
	}
}
