package com.zkjl.posite_cloud.shironew;

import com.zkjl.posite_cloud.shironew.redissession.listener.ShiroSessionListener;
import com.zkjl.posite_cloud.shironew.redissession.service.ShiroSessionRepository;
import com.zkjl.posite_cloud.shironew.redissession.service.ShiroSessionService;
import com.zkjl.posite_cloud.shironew.redissession.service.impl.ShiroSessionRepositoryImpl;
import com.zkjl.posite_cloud.shironew.redissession.session.CachingShiroSessionDao;
import com.zkjl.posite_cloud.shironew.redissession.session.ShiroSessionFactory;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * shiro的配置
 *
 * @author yindq
 * @date 2017年12月13日
 */
@Configuration
public class ShiroConfiguration {
	// 拦截器，必须保证有序
	private final static Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
	private final static Map<String, Filter> filters = new LinkedHashMap<String, Filter>();


	/**
	 * ShiroFilter
	 *
	 * @return
	 */
	@Bean(name = "shiroFilter")
	public ShiroFilterFactoryBean getShiroFilterFactoryBean() {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

		//配置访问权限 anon：表示全部放权的资源路径，authc：表示需要认证才可以访问
//		filterChainDefinitionMap.put("/logout", "logout");

		filterChainDefinitionMap.put("/login.html", "anon");
		filterChainDefinitionMap.put("/**/*.html", "anon");
		filterChainDefinitionMap.put("/api/*", "authc");

		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

//		filters.put("ajaxSessionFilter", getMyUserFilter());
		filters.put("authc", getMyFormAuthenticationFilter());

		shiroFilterFactoryBean.setSecurityManager(getDefaultWebSecurityManager());
		shiroFilterFactoryBean.setLoginUrl("/login");//测试使用
		shiroFilterFactoryBean.setSuccessUrl("/");
		shiroFilterFactoryBean.setUnauthorizedUrl("/unauthorized");
		shiroFilterFactoryBean.setFilters(filters);
		return shiroFilterFactoryBean;
	}

	@Bean(name = "lifecycleBeanPostProcessor")
	public LifecycleBeanPostProcessor getLifecycleBeanPostProcessor(){
		return new LifecycleBeanPostProcessor();
	}

	@Bean
	@DependsOn(value = "lifecycleBeanPostProcessor")
	public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator(){
		DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
		return defaultAdvisorAutoProxyCreator;
	}

	@Bean
	public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(){
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(getDefaultWebSecurityManager());
		return authorizationAttributeSourceAdvisor;
	}

	/**
	 * 配置核心安全事务管理器
	 *
	 * @return
	 */
	@Bean(name = "securityManager")
	public DefaultWebSecurityManager getDefaultWebSecurityManager() {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setSessionManager(getDefaultWebSessionManager());
		securityManager.setRealm(getShiroDbRealm());
		securityManager.setCacheManager(getEhCacheManager());
		return securityManager;
	}

	/**
	 * 配置自定义的权限登录器 (这个需要自己写，账号密码校验；权限等)
	 *
	 * @return
	 */
	@Bean(name = "shiroDbRealm")
	public ShiroDbRealm getShiroDbRealm() {
		ShiroDbRealm shiroRealm = new ShiroDbRealm();
		// 配置自定义的密码比较器
//		shiroRealm.setCredentialsMatcher(getCustomCredentialsMatcher());
		// 启用身份验证缓存，即缓存AuthenticationInfo信息，默认false
//		shiroRealm.setAuthenticationCachingEnabled(true);
//		shiroRealm.setCacheManager(getEhCacheManager());
//		shiroRealm.setCredentialsMatcher(getCustomCredentialsMatcher());
		return shiroRealm;
	}

	/**
	 * 配置EhCache 缓存
	 *
	 * @return
	 */
	@Bean
	public EhCacheManager getEhCacheManager() {
		EhCacheManager em = new EhCacheManager();
		em.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
		return em;
	}


	/**
	 * FormAuthenticationFilter
	 *
	 * @return
	 */
	@Bean(name = "myAuthenticationFilter")
	public MyAuthenticationFilter getMyFormAuthenticationFilter() {
		return new MyAuthenticationFilter();
	}

	/**
	 * DefaultWebSessionManager
	 *
	 * @return
	 */
	@Bean(name = "defaultWebSessionManager")
	public DefaultWebSessionManager getDefaultWebSessionManager() {
		DefaultWebSessionManager manager = new DefaultWebSessionManager();
		// 会话超时时间，单位：毫秒
		manager.setGlobalSessionTimeout(1800000);
		// 是否在会话过期后会调用SessionDAO的delete方法删除会话 默认true
		manager.setDeleteInvalidSessions(true);
		// 是否开启会话验证器任务 默认true
		manager.setSessionValidationSchedulerEnabled(false);
		// 定时清理失效会话, 清理用户直接关闭浏览器造成的孤立会话
		manager.setSessionValidationInterval(1800000);
		manager.setSessionFactory(getShiroSessionFactory());
		manager.setSessionDAO(getCachingShiroSessionDao());
		//默认JSESSIONID，同tomcat/jetty在cookie中缓存标识相同，修改用于防止访问404页面时，容器生成的标识把shiro的覆盖掉
		manager.setSessionValidationSchedulerEnabled(true);
		SimpleCookie simpleCookie = new SimpleCookie("SHRIOSESSIONID");
		manager.setSessionIdCookie(simpleCookie);
		List<SessionListener> listeners = new ArrayList<>();
		listeners.add(getShiroSessionListener());
		manager.setSessionListeners(listeners);
		return manager;
	}

	@Bean
	public ShiroSessionFactory getShiroSessionFactory(){
		return new ShiroSessionFactory();
	}
	@Bean
	public ShiroSessionListener getShiroSessionListener(){
		ShiroSessionListener shiroSessionListener = new ShiroSessionListener();
		shiroSessionListener.setSessionDao(getCachingShiroSessionDao());
		shiroSessionListener.setShiroSessionService(getShiroSessionService());
		return shiroSessionListener;
	}

	@Bean
	public ShiroSessionService getShiroSessionService(){
		ShiroSessionService shiroSessionService = new ShiroSessionService();
		shiroSessionService.setRedisTemplate(getRedisTemplate());
		shiroSessionService.setSessionDao(getCachingShiroSessionDao());
		return shiroSessionService;
	}

	/**
	 * SessionDAO
	 *
	 * @return
	 */
	@Bean(name = "cachingShiroSessionDao")
	public CachingShiroSessionDao getCachingShiroSessionDao() {
		CachingShiroSessionDao dao = new CachingShiroSessionDao();
		dao.setSessionRepository(getShiroSessionRepository());
		return dao;
	}

	@Bean("redisSessionTemplate")
	public RedisTemplate getRedisTemplate(){
		RedisTemplate redisTemplate = new RedisTemplate();
		redisTemplate.setConnectionFactory(getJedisConnectionFactory());
		JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
		redisTemplate.setValueSerializer(jdkSerializationRedisSerializer);
		return redisTemplate;
	}

	@Bean
	public JedisConnectionFactory getJedisConnectionFactory(){
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
		jedisConnectionFactory.setPort(44334);
		jedisConnectionFactory.setHostName("112.244.72.30");
		jedisConnectionFactory.setPoolConfig(getJedisPoolConfig());
		return jedisConnectionFactory;
	}

	@Bean
	public JedisPoolConfig getJedisPoolConfig(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxIdle(300);
		config.setTestOnBorrow(true);
		return config;
	}

	@Bean
	public ShiroSessionRepository getShiroSessionRepository(){
		ShiroSessionRepositoryImpl shiroSessionRepository = new ShiroSessionRepositoryImpl();
		shiroSessionRepository.setRedisTemplate(getRedisTemplate());
		return shiroSessionRepository;
	}

}