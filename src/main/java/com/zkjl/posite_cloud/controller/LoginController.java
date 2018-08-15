package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.common.MsgConstant;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.exception.AccountStartException;
import com.zkjl.posite_cloud.service.IUserService;
import com.zkjl.posite_cloud.shiro.ShiroUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.security.auth.login.AccountExpiredException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public ApiResult loginAjax(HttpServletRequest request, String username, String type, String password) {
        if (request.getAttribute("success") != null && (boolean) request.getAttribute("success")) {
//            userLogoServiceImpl.addLoginLogo(getUserId());
            User login = userService.selectByUsernameAndPassword(username, password);
            return success(login);
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

    @GetMapping(value = "login")
    private ModelAndView login(ModelAndView modelAndView){
        modelAndView.setViewName("login");
        return modelAndView;
    }

    /**
     * 退出系统
     *
     * @return
     * @throws Exception
     */
    @GetMapping("/logout")
    @ResponseBody
    @ApiOperation(value = "用户退出", httpMethod = "GET")
    public String logout() throws Exception {
        Subject subject = SecurityUtils.getSubject();
       /* if (subject.isAuthenticated()) {
            subject.logout();
            return "logout->success";
        }*/
        try {
            subject.logout();
        } catch (Exception e) {
            return "logout->error";
        }
        return "logout->success";
    }

    /**
     * 没有权限
     * @param response
     */
    @RequestMapping(value = "/unauthorized")
    public void unauthorized(HttpServletResponse response) {
        ShiroUtil.writeResponse(response, "对不起,您没有访问权限！");
    }
}
