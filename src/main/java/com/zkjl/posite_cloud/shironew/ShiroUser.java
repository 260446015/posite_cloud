package com.zkjl.posite_cloud.shironew;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;

public class ShiroUser implements java.io.Serializable {

    private static final long serialVersionUID = -2649983064333269618L;

    private final String id;
    private final String loginName;
    private final String name;
    // 是否管理员
    private final String jobLevel;
    private final String ip;
    /**
     * 用户头像
     */
    private String image;
    /**
     * 查询数量
     */
    private Integer searchCount;
    /**
     * 查询总量
     */
    private Integer totalSerachCount;
    /**
     * 用户舆情关键词
     */
    @Getter
    private String sentiment;
    @Getter
    private String username;

    private ShiroUser(Builder builder) {
        id = builder.id;
        loginName = builder.loginName;
        name = builder.name;
        jobLevel = builder.jobLevel;
        ip = builder.ip;
        image = builder.image;
        searchCount = builder.searchCount;
        totalSerachCount = builder.totalSerachCount;
        sentiment = builder.sentiment;
        username = builder.username;
    }

    public static class Builder {
        private final String id;
        private final String loginName;
        private String name;
        // 是否管理员
        private String jobLevel;
        private String ip;
        /**
         * 用户头像
         */
        private String image;
        /**
         * 查询数量
         */
        private Integer searchCount;
        /**
         * 查询总量
         */
        private Integer totalSerachCount;
        /**
         * 用户舆情关键词
         */
        private String sentiment;
        private String username;

        public Builder(String id, String loginName) {
            this.id = id;
            this.loginName = loginName;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder jobLevel(String jobLevel) {
            this.jobLevel = jobLevel;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder image(String image){
            this.image = image;
            return this;
        }

        public Builder searchCount(Integer searchCount){
            this.searchCount = searchCount;
            return this;
        }

        public Builder totalSerachCount(Integer totalSerachCount){
            this.totalSerachCount = totalSerachCount;
            return this;
        }

        public Builder sentiment(String sentiment){
            this.sentiment = sentiment;
            return this;
        }

        public Builder username(String sentiment){
            this.sentiment = sentiment;
            return this;
        }

        public ShiroUser builder() {
            return new ShiroUser(this);
        }

    }

    public String getId() {
        return id;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getName() {
        return name;
    }

    public String getJobLevel() {
        return jobLevel;
    }

    public String getIp() {
        return ip;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
