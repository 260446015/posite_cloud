package com.zkjl.posite_cloud.domain.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "user")
public class User implements Serializable{

    private static final long serialVersionUID = 6074570833026582055L;
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
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 创建人
     */
    private String creator;
    /**
     * 用户舆情关键词
     */
    private String sentiment;
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
}
