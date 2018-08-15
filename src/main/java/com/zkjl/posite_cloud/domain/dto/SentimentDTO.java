package com.zkjl.posite_cloud.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yindawei
 * @date 2018/8/14 11:39
 **/
@Data
public class SentimentDTO implements Serializable {
    private static final long serialVersionUID = -4865984292354479975L;

    private Integer pageNum;
    private Integer pageSize;
    private String[] msg;
    private String beginDate;
    private String endDate;

}
