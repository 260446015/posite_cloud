package com.zkjl.posite_cloud.domain.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

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
    private Date creationTime;
    /**
     * 职位
     */
    private String jobLevel;
    /**
     * 拥有权限
     */
    private String permission;
}
