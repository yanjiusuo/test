package com.jd.workflow.console.controller.utils;

import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public class DtBeanUtils {

    /**
     * 创建并返回一个与目标类型相同的新对象，然后将源对象的属性值复制过去。
     * 需要目标类有一个无参数的构造函数。
     *
     * @param source 源对象，任意类型。
     * @param targetClass 目标对象的类，必须与源对象的类有相同的属性。
     * @return 返回一个Result对象，其中包含新创建的对象（如果成功）或错误信息（如果失败）。
     */
    public static <T> T getBean(Object source, Class<T> targetClass) {
        try {
            // 创建目标对象的实例
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T target = constructor.newInstance();
            // 复制属性
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error("Failed to copy properties and create a new instance: " + e.getMessage());
            return null;
        }
    }

    /**
     *
     * @param param
     * @param targetClass
     * @param <T>
     * @return
     */
    public static <T> T getCreatBean(Object param, Class<T> targetClass) {
        T target = getBean(param, targetClass);
        BaseEntityNoDelLogic baseEntity = new BaseEntityNoDelLogic();
        baseEntity.setCreator(getPin());
        BeanUtils.copyProperties(baseEntity, target);
        return target;
    }

    /**
     * 插件不走web登录态
     * @return
     */
    public static String getPin() {
        if(StringUtils.isBlank(UpLoginContextHelper.getUserPin())){
            return UserSessionLocal.getUser().getUserId();
        }else{
            return UpLoginContextHelper.getUserPin();
        }
    }

    /**
     *
     * @param param
     * @param targetClass
     * @param <T>
     * @return
     */
    public static <T> T getUpdateBean(Object param, Class<T> targetClass) {
        T target = getBean(param, targetClass);
        BaseEntityNoDelLogic baseEntity = new BaseEntityNoDelLogic();
        baseEntity.setModifier(getPin());
        BeanUtils.copyProperties(baseEntity, target);
        return target;
    }
}