package com.zkjl.posite_cloud.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author yindawei
 * @date 2018/8/14 16:25
 **/
public interface IReportService {

    JSONObject report(String mobile, String username);
}
