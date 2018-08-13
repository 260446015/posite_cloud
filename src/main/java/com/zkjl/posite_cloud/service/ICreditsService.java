package com.zkjl.posite_cloud.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/10 17:29
 **/
public interface ICreditsService {

    List<JSONObject> creditsWarining(String username) throws Exception;
}
