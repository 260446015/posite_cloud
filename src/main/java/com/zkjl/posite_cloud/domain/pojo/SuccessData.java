package com.zkjl.posite_cloud.domain.pojo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author yindawei
 * @date 2018/8/9 14:31
 **/
@Data
public class SuccessData {

    private String webname;
    private Boolean success;
    private String webtype;
    private Boolean isregister;

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
