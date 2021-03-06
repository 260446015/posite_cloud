package com.zkjl.posite_cloud.aop;

import com.zkjl.posite_cloud.common.SystemControllerLog;
import com.zkjl.posite_cloud.common.util.DateUtils;
import com.zkjl.posite_cloud.dao.LogRepository;
import com.zkjl.posite_cloud.domain.pojo.Log;
import com.zkjl.posite_cloud.domain.pojo.User;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Calendar;

/**
 * @author ydw
 * Created on 2018/6/23
 */
@Component
@Aspect
public class LogAop {

    @Pointcut(value = "execution(public * com.zkjl.posite_cloud.controller.*.*(..)) && !execution(public * com.zkjl.posite_cloud.controller.LoginController.loginAjax(..))")
    public void logPoint() {
    }

    @Resource
    private LogRepository logRepository;

    @Before(value = "logPoint()")
    public void saveUserOperation(JoinPoint joinPoint) {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method targetMethod = methodSignature.getMethod();
        SystemControllerLog annotation = targetMethod.getAnnotation(SystemControllerLog.class);
        try {
            if(annotation != null){
                User user = (User) SecurityUtils.getSubject().getPrincipal();
                Log log = new Log();
                log.setContent(annotation.description());
                log.setUsername(user.getUsername());
                log.setCretionTime(DateUtils.getFormatString(Calendar.getInstance().getTime()));
                logRepository.save(log);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
