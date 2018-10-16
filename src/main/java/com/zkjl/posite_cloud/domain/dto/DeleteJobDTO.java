package com.zkjl.posite_cloud.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteJobDTO implements Serializable {
    private static final long serialVersionUID = -956276294804116127L;
    private String userid;
    private String taskid;
}
