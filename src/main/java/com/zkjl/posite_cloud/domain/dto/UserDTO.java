package com.zkjl.posite_cloud.domain.dto;

import com.zkjl.posite_cloud.domain.pojo.User;
import lombok.Data;

/**
 * @author yindawei
 * @date 2018/8/16 19:57
 **/
@Data
public class UserDTO extends User {

    private String beginDate;
    private String endDate;
    private Integer pageNum;
    private Integer pageSize;
}
