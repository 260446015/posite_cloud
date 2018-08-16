package com.zkjl.posite_cloud.domain.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "user")
public class User {

    @Id
    private String id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 真实姓名
     */
    private String name;
    /**
     * 创建日期
     */
    private String creationTime;
    /**
     * 职位
     */
    private String jobLevel;
    /**
     * 拥有权限
     */
    private String permission;
    /**
     * 是否启用
     */
    private Boolean ifEnable;
    /**
     * 过期时间
     */
    private String expireTime;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 性别
     */
    private String sex;
    /**
     * 单位部门
     */
    private String department;
    /**
     * 岗位
     */
    private String job;
    /**
     * 所属领域
     */
    private String domain;
    /**
     * 手机号
     */
    private String mobile;
}
