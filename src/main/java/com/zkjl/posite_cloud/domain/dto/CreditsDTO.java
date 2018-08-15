package com.zkjl.posite_cloud.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yindawei
 * @date 2018/8/14 15:07
 **/
@Data
public class CreditsDTO implements Serializable {
    private static final long serialVersionUID = -6620761284387311486L;

    private String username;
    private String mobile;
    private String webname;
    private String webtype;
    private Integer minSorce;
    private Integer maxSorce;
    private Integer pageNum;
    private Integer pageSize;
}
