package com.zkjl.posite_cloud.domain.vo;

import lombok.Data;

/**
 * @author yindawei
 * @date 2018/8/22 10:35
 **/
@Data
public class UserVo {
    private String area;
    private String id;
    /**
     * 用户名
     */
    private String username;
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
}
