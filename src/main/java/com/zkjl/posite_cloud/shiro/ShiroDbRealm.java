package com.zkjl.posite_cloud.shiro;

import com.zkjl.posite_cloud.common.MsgConstant;
import com.zkjl.posite_cloud.dao.RoleRepository;
import com.zkjl.posite_cloud.dao.UserRepository;
import com.zkjl.posite_cloud.domain.pojo.Role;
import com.zkjl.posite_cloud.domain.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.security.auth.login.AccountExpiredException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    @Resource
    private RoleRepository roleRepository;


    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        LOGGER.info("===============进行权限配置================");
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        User user = (User) principals.getPrimaryPrincipal();
        String jobLevel = user.getJobLevel();
        Role role = roleRepository.findByRolename(jobLevel);
        String[] split = role.getPermission().split(",");
        List<String> permissions = Arrays.asList(split);
        authorizationInfo.addStringPermissions(permissions);
        return authorizationInfo;
    }

    /**
     * 认证身份
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        LOGGER.info("===============进行登陆认证================");
        UsernamePasswordToken myToken = (UsernamePasswordToken) token;
        User user = userRepository.findByUsernameAndPassword(myToken.getUsername(), String.valueOf(((UsernamePasswordToken) token).getPassword()));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String today = format.format(new Date());
        if (user == null) {
            LOGGER.debug("user {} is not exist.", myToken.getUsername());
            throw new IncorrectCredentialsException();
        }
        /**
         * 启用状态
         */
        if(!user.getIfEnable()){
            LOGGER.debug("user {} is not enable.", myToken.getUsername());
            throw new DisabledAccountException(MsgConstant.UNENABLE);
        }
        /**
         * 过期策略
         */
        if(!StringUtils.isBlank(user.getExpireTime())){
            if (today.compareTo(user.getExpireTime()) > 0) {
                LOGGER.debug("user {} be overdue.", myToken.getUsername());
                throw new ExpiredCredentialsException(MsgConstant.ACCOUNTEXPIRED);
            }
        }
        Object principal = user;
        Object credentials = user.getPassword();
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                principal, credentials, getName());

        return authenticationInfo;
    }

    @Override
    public void onLogout(PrincipalCollection principals) {
        super.clearCachedAuthorizationInfo(principals);
        User user = (User) principals.getPrimaryPrincipal();
        removeUserCache(user);
    }

    /**
     * 清除用户缓存
     *
     * @param shiroUser
     */
    public void removeUserCache(User user) {
        removeUserCache(user.getUsername());
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

}
