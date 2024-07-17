package com.jd.workflow.console.config;



import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.jd.jsf.gd.error.ClientTimeoutException;
import com.jd.jsf.gd.error.NoAliveProviderException;
import com.jd.jsf.gd.error.RpcException;

import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dto.constant.SystemConstants;
import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.ServiceException;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.MDC;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常统一处理
 * <p>业务内部可直接抛出异常, 不用关注异常处理。异常统一由此处异常处理Handler进行处理吐给前端</p>
 *
 * @author xieguangyao3
 * @date 2021/12/6
 */
@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler implements ResponseBodyAdvice {


    @ExceptionHandler(value = AsyncRequestTimeoutException.class)
    public String handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
        log.error("AsyncRequestTimeoutException");
        return SseEmitter.event().data("timeout!!").build().stream()
                .map(d -> d.getData().toString())
                .collect(Collectors.joining());
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public CommonResult<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return CommonResult.error(e.getMessage());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public CommonResult<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException", e);
        return CommonResult.error(null);
    }
    @ExceptionHandler(value = BizException.class)
    public CommonResult<?> handleIllegalArgumentException(BizException e) {


        Integer code = e.getCode();
        if(code == null){
            code  = ServiceErrorEnum.COMMON_EXCEPTION.getCode();
        }
        CommonResult result = new CommonResult(code, e.getMsg(), e.getData());
        log.error("BizException:trace={}",result.getTraceId(), e);
        return result;
    }
    @ExceptionHandler(value = StdException.class)
    public CommonResult<?> handleIllegalArgumentException(StdException e) {
        log.error("StdException", e);
        String desc = ErrorMessageFormatter.formatMsg(e);
        return CommonResult.error(desc);
    }
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public CommonResult<?> handleIllegalArgumentException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException", e);
        return CommonResult.error("http请求方法错误，只支持："+ Arrays.toString(e.getSupportedMethods()));
    }

    @ExceptionHandler(value = NullPointerException.class)
    public CommonResult<?> handlerBaseException(NullPointerException e) {
        log.error("NullPointerException: ", e);
        return CommonResult.error("系统错误，请重试.");
    }



    @ExceptionHandler(value = {MyBatisSystemException.class, MybatisPlusException.class, PersistenceException.class})
    public CommonResult<?> handlerSqlException(RuntimeException e) {
        log.error("sql exception: ", e);
        return CommonResult.error("系统错误，请重试.");
    }


    @ExceptionHandler(BindException.class)
    public CommonResult<?> bindException(BindException exception) {
        log.error("BindException Exception.", exception);
        //配置的默认的错误信息
        String defaultMessage = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return CommonResult.error(defaultMessage);
    }

    @ExceptionHandler({NoAliveProviderException.class, ClientTimeoutException.class})
    public CommonResult<?> jsfException(RpcException exception) {
        log.error("jsfException Exception.", exception);
        return CommonResult.error("外部服务调用异常："+exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResult<?>> validException(MethodArgumentNotValidException exception){
        log.error("ErrorHandler MethodArgumentNotValidException.", exception);
        List<ObjectError> errors = exception.getBindingResult().getAllErrors();
        StringBuilder builder = new StringBuilder();
        if(!errors.isEmpty()){
            for(ObjectError objectError: errors){
                if(builder.length() > 0){
                    builder.append("; ");
                }
                Object[] arguments = objectError.getArguments();
                if(arguments.length>0){
                    Object o = arguments[0];
                    if(o instanceof DefaultMessageSourceResolvable){
                        builder.append(((DefaultMessageSourceResolvable) o).getDefaultMessage()).append(":");
                    }
                }
                builder.append(objectError.getDefaultMessage());
            }
        }
        String message;
        if(builder.length() > 0){
            message = builder.toString();
        } else {
            message = exception.getMessage();
        }
        return ResponseEntity.ok().body(CommonResult.error(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResult<?>> validException(ConstraintViolationException exception){
        log.error("ErrorHandler ConstraintViolationException.", exception);
        Set<ConstraintViolation<?>> errors = exception.getConstraintViolations();
        StringBuilder builder = new StringBuilder();
        if(!errors.isEmpty()){
            for(ConstraintViolation<?> objectError: errors){
                if(builder.length() > 0){
                    builder.append("; ");
                }
                String properties = objectError.getPropertyPath().toString();
                builder.append(properties).append(":");
                builder.append(objectError.getMessage());
            }
        }
        String message;
        if(builder.length() > 0){
            message = builder.toString();
        } else {
            message = exception.getMessage();
        }

        return ResponseEntity.ok().body(CommonResult.error(message));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<CommonResult<?>> validException(ValidationException exception){
        log.error("ErrorHandler ValidationException.", exception);
        return ResponseEntity.ok().body(CommonResult.error("校验失败"));
    }

    @ExceptionHandler(value = ServiceException.class)
    public CommonResult<?> handleServiceException(HttpServletRequest req,ServiceException e) {
        String uri = req.getRequestURI().replaceAll(req.getContextPath(), "");
        log.error("ServiceException->business error url={},params={}",uri, JsonUtils.toJSONString(req.getParameterMap()), e);
        return CommonResult.buildErrorCodeMsg(e.getCode(), e.getMessage());
    }


    /**
     * 全局异常捕捉处理,捕获异常类型:Exception
     *
     * @param req
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object defaultErrorHandler(HttpServletRequest req, Exception e) {
        String uri = req.getRequestURI().replaceAll(req.getContextPath(), "");
        log.error("请求接口:{}异常, 参数:{}, 异常信息:", uri, JsonUtils.toJSONString(req.getParameterMap()), e);
        return CommonResult.error("未知错误");
    }

    /**
     * 对返回的数据进行处理
     */
    @Override
    public Object beforeBodyWrite(Object retObj, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (retObj instanceof CommonResult) {
            CommonResult resultModel = (CommonResult) retObj;
            resultModel.setTraceId(MDC.get(SystemConstants.TRACE_ID_KEY));
            return retObj;
        }
        return retObj;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }
}
