package com.zkjl.posite_cloud.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

public class MySessionManager extends DefaultWebSessionManager {
	/**
	 * 屏蔽no session的错误
	 */
	@Override
	protected Session retrieveSession(SessionKey sessionKey) throws UnknownSessionException {

		try {
			return super.retrieveSession(sessionKey);
		} catch (Exception e) {
			// 获取不到SESSION不报错
			return null;
		}

	}
}
