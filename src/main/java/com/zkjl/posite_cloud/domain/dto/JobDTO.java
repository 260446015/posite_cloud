package com.zkjl.posite_cloud.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yindawei
 * @date 2018/8/9 11:11
 **/
@Data
public class JobDTO implements Serializable {
    private static final long serialVersionUID = 5326744538467644499L;

    private String username;
    private Integer level;
    private String status;
    private String datas;
    private String taskid;
    private String taskname;
}
