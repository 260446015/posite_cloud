package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.common.MsgConstant;
import com.zkjl.posite_cloud.exception.AccountStartException;
import com.zkjl.posite_cloud.service.IUserService;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.security.auth.login.AccountExpiredException;
import javax.servlet.http.HttpServletRequest;

@RestController
public class LoginController extends BaseController{

    private static Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Resource
    private IUserService userService;
    /**
     * 登录过滤器放行后进入此接口
     *
     * @param request
     *            携带成功或失败的request
     * @return 返回响应
     */
    @PostMapping(value = "/login")
    @ResponseBody
    public ApiResult loginAjax(HttpServletRequest request, String username, String type, String password) {
        if (request.getAttribute("success") != null && (boolean) request.getAttribute("success")) {
//            userLogoServiceImpl.addLoginLogo(getUserId());
            return success(MsgConstant.LOGIN_SUCCESS).setMessage(MsgConstant.LOGIN_SUCCESS);
        }
        // 登录失败从request中获取shiro处理的异常信息。
        String message = MsgConstant.LOGIN_ERRROR;
        String error = (String) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
        LOGGER.info("登陆失败的原因" + error);
        if (error != null) {
            if (error.equals(IncorrectCredentialsException.class.getName())) {
                message = MsgConstant.CREDENTIAL_ERROR;
            } else if (error.equals(AccountExpiredException.class.getName())) {
                message = MsgConstant.ACCOUNTEXPIRED;
            } else if (error.equals(AccountStartException.class.getName())) {
                message = MsgConstant.ACCOUNTSTART;
            } else if (error.equals(ExcessiveAttemptsException.class.getName())) {
                message = MsgConstant.LOCKING;
            }
        }
        return error(message);
    }
}
