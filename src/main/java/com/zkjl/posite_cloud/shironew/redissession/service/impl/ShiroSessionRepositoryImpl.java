package com.zkjl.posite_cloud.shironew.redissession.service.impl;

import com.zkjl.posite_cloud.shironew.redissession.service.ShiroSessionRepository;
import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.session.Session;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Service
public class ShiroSessionRepositoryImpl implements ShiroSessionRepository {

    private static final String REDIS_SHIRO_SESSION = "shiro-session:";
    private static final int SESSION_VAL_TIME_SPAN = 1800;

    // 保存到Redis中key的前缀 prefix+sessionId
    @Setter
    private String redisShiroSessionPrefix = REDIS_SHIRO_SESSION;

    // 设置会话的过期时间
    @Setter
    private int redisShiroSessionTimeout = SESSION_VAL_TIME_SPAN;

    @Resource
    @Getter
    @Setter
    private RedisTemplate<String,Session> redisTemplate;

    @Override
    public void saveSession(Session session) {
        if (session == null || session.getId() == null)
            throw new NullPointerException("session is empty");
        try {
            redisTemplate.opsForValue().set(buildRedisSessionKey(session.getId()),session,
                    redisShiroSessionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("save session error");
        }
    }
    @Override
    public void updateSession(Session session) {
        if (session == null || session.getId() == null)
            throw new NullPointerException("session is empty");
        try {
            redisTemplate.boundValueOps(buildRedisSessionKey(session.getId())).set(session,
                    redisShiroSessionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("save session error");
        }
    }

    @Override
    public void refreshSession(Serializable sessionId) {
        redisTemplate.expire(buildRedisSessionKey(sessionId),redisShiroSessionTimeout, TimeUnit.SECONDS);
    }

    @Override
    public void deleteSession(Serializable id) {
        if (id == null) {
            throw new NullPointerException("session id is empty");
        }
        try {

            redisTemplate.delete(buildRedisSessionKey(id));

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("delete session error");
        }
    }

    @Override
    public Session getSession(Serializable id) {
        if (id == null)
            throw new NullPointerException("session id is empty");
        Session session = null;
        try {
            session = redisTemplate.boundValueOps(buildRedisSessionKey(id)).get();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("get session error");
        }
        return session;
    }

    @Override
    public Collection<Session> getAllSessions() {
        System.out.println("get all sessions");
        return null;
    }

    private String buildRedisSessionKey(Serializable sessionId) {
        return redisShiroSessionPrefix + sessionId;
    }


}