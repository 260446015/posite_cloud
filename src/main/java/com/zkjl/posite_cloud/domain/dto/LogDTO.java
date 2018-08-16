package com.zkjl.posite_cloud.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yindawei
 * @date 2018/8/16 16:30
 **/
@Data
public class LogDTO implements Serializable {
    private static final long serialVersionUID = 5874612378100224795L;
    private String username;
    private String beginDate;
    private String endDate;
    private Integer pageNum;
    private Integer pageSize;
}
