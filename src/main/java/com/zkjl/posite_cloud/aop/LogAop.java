package com.zkjl.posite_cloud.aop;

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

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

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

    @Before(value = "logPoint()")
    public void saveUserOperation(JoinPoint joinPoint) {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method targetMethod = methodSignature.getMethod();
        System.out.println("当前session:"+request.getSession().getId());

    }
}
