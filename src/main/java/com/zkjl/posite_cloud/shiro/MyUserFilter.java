package com.zkjl.posite_cloud.shiro;

import org.apache.shiro.web.filter.authc.UserFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyUserFilter extends UserFilter {

	@Override
	protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if ("XMLHttpRequest".equals(httpRequest.getHeader("X-Requested-With"))) {
			// ajax 请求直接返回403 Access Denied
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.sendError(403, "Access Denied");
		} else {
			// 普通请求返回登录页面
			String loginUrl = getLoginUrl();
			WebUtils.issueRedirect(request, response, loginUrl);
		}
	}

	/**
	 * ajax shiro session超时统一处理
	 */
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest req = WebUtils.toHttp(request);
		String xmlHttpRequest = req.getHeader("X-Requested-With");
		if (!StringUtils.hasText(xmlHttpRequest)) {
			if (xmlHttpRequest.equalsIgnoreCase("XMLHttpRequest")) {
				HttpServletResponse res = WebUtils.toHttp(response);
				res.sendError(401, "oauthstatus");
				// res.setHeader("oauthstatus", "401");
				return false;
			}
		}
		return super.onAccessDenied(request, response);
	}

}
