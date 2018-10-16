package com.zkjl.posite_cloud.shiro;

import com.zkjl.posite_cloud.dao.UserRepository;
import com.zkjl.posite_cloud.domain.pojo.User;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyFormAuthenticationFilter extends FormAuthenticationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyFormAuthenticationFilter.class);

    @Autowired
    private DefaultWebSecurityManager securityManager;
    @Autowired
    private UserRepository userBaseRepository;

    /**
     * 登陆验证
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        UsernamePasswordToken token = createToken(request, response);
        try {
            Subject subject = getSubject(request, response);
            /*if (true) {
                DefaultWebSessionManager sessionManager = (DefaultWebSessionManager) securityManager.getSessionManager();
                // 单点登录
                Collection<Session> sessions = sessionManager.getSessionDAO().getActiveSessions();
                for (Session session : sessions) {
                    if (token.getUsername()
                            .equals(((User)session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY)).getUsername())) {
                        sessionManager.getSessionDAO().delete(session);
                    }
                }
            }*/
            subject.login(token);
            return onLoginSuccess(token, subject, request, response);
        } catch (AuthenticationException e) {
            LOGGER.error("登录失败." + e);
            return onLoginFailure(token, e, request, response);
        }
    }

    /**
     * 登陆成功
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request,
                                     ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        httpServletRequest.setAttribute("success", true);
        return true;
    }

    /**
     * 创建token
     */
    @Override
    protected UsernamePasswordToken createToken(ServletRequest request, ServletResponse response) {
        String password = getPassword(request);
        User user=userBaseRepository.findById(password).get();
        String username = user.getUsername();
        String newpassword = user.getPassword();
        boolean rememberMe = isRememberMe(request);
        String host = getHost(request);
        return new UsernamePasswordToken(username, newpassword.toCharArray(), rememberMe, host);
    }

    /*@Override
    protected UsernamePasswordToken createToken(ServletRequest request, ServletResponse response) {
        String username = getUsername(request);
        String password = getPassword(request);
        boolean rememberMe = isRememberMe(request);
        String host = getHost(request);
        return new UsernamePasswordToken(username, password.toCharArray(), rememberMe, host);
    }*/

    /**
     * 未登录拦截处理
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response, Object mappedValue)
            throws Exception {
        if (isLoginRequest(request, response)) {
            if (isLoginSubmission(request, response)) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Login submission detected.  Attempting to execute login.");
                }
                return executeLogin(request, response);
            } else {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Login page view.");
                }
                return true;
            }
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Attempting to access a path which requires authentication.  Forwarding to the " +
                        "Authentication url [" + getLoginUrl() + "]");
            }
            if (isAjax(request)) {
                ShiroUtil.writeResponse((HttpServletResponse) response, "您的登录已失效，请重新登录本系统！");
            } else {
                this.saveRequestAndRedirectToLogin(request, response);
            }
            return false;
        }
    }

    private static boolean isAjax(ServletRequest request) {
        String header = ((HttpServletRequest) request).getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(header)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


}
