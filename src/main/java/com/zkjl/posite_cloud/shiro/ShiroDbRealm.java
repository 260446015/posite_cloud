package com.zkjl.posite_cloud.shiro;

import com.zkjl.posite_cloud.dao.UserRepository;
import com.zkjl.posite_cloud.domain.pojo.Role;
import com.zkjl.posite_cloud.domain.pojo.User;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * 身份校验核心类
 * 
 * @author yindawei
 * @date 2017年12月13日
 */
public class ShiroDbRealm extends AuthorizingRealm {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShiroDbRealm.class);

	@Autowired
	private UserRepository userRepository;
	
	
	/**
	 * 授权
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		LOGGER.info("===============进行权限配置================");
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		
		return authorizationInfo;
	}

	/**
	 * 认证身份
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		LOGGER.info("===============进行登陆认证================");
		UsernamePasswordToken myToken = (UsernamePasswordToken) token;
		User user = userRepository.findByUsernameAndPassword(myToken.getUsername(),String.valueOf(((UsernamePasswordToken) token).getPassword()));
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String today = format.format(new Date());
		if (user == null) {
			LOGGER.debug("user {} is not exist.", myToken.getUsername());
			throw new IncorrectCredentialsException();
		}
		/**
		 * 过期策略
		 */
		Object principal = null;
		Object credentials = null;
		if (user != null) {
			//以下信息是从数据库中获取的.
			//1). principal: 认证的实体信息. 可以是 username, 也可以是数据表对应的用户的实体类对象.
			principal = user;
			credentials = user.getPassword();
		}
		ShiroUser shiroUser = new ShiroUser(user.getId(), user.getUsername(), user.getName(),user.getRole());
		SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
				principal ,credentials, getName());

		return authenticationInfo;
	}

	@Override
	public void onLogout(PrincipalCollection principals) {
		super.clearCachedAuthorizationInfo(principals);
		ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
		removeUserCache(shiroUser);
	}

	/**
	 * 清除用户缓存
	 * 
	 * @param shiroUser
	 */
	public void removeUserCache(ShiroUser shiroUser) {
		removeUserCache(shiroUser.getLoginName());
	}
	
	/**
	 * 清除用户缓存
	 * 
	 * @param loginName
	 */
	public void removeUserCache(String loginName) {
		SimplePrincipalCollection principals = new SimplePrincipalCollection();
		principals.add(loginName, super.getName());
		super.clearCachedAuthenticationInfo(principals);
	}

	/**
	 * 自定义Authentication对象，使得Subject除了携带用户的登录名外还可以携带更多信息.
	 */
	public static class ShiroUser implements Serializable {
		private static final long serialVersionUID = -5479583557463219088L;
		private String id;
		private String loginName;
		private String name;
		private Role role;

		public ShiroUser(String id, String loginName, String name,Role role) {
			super();
			this.id = id;
			this.loginName = loginName;
			this.name = name;
			this.role = role;
		}


		public String getLoginName() {
			return loginName;
		}

		public void setLoginName(String loginName) {
			this.loginName = loginName;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Role getRole() {
			return role;
		}

		public void setRole(Role role) {
			this.role = role;
		}

		/**
		 * 本函数输出将作为默认的<shiro:principal/>输出.
		 */
		@Override
		public String toString() {
			return loginName;
		}

		/**
		 * 重载hashCode,只计算loginName;
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(loginName);
		}

		/**
		 * 重载equals,只计算loginName;
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ShiroUser other = (ShiroUser) obj;
			if (loginName == null) {
				if (other.loginName != null) {
					return false;
				}
			} else if (!loginName.equals(other.loginName)) {
				return false;
			}
			return true;
		}
	}

}
