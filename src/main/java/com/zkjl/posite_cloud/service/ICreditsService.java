package com.zkjl.posite_cloud.service;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.domain.dto.CreditsDTO;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.domain.pojo.User;
import org.springframework.data.domain.PageImpl;

/**
 * @author yindawei
 * @date 2018/8/10 17:29
 **/
public interface ICreditsService {

    PageImpl<JSONObject> creditsWarining(CreditsDTO creditsDTO) throws Exception;

    boolean sendEmail(JSONObject data, User user) throws Exception;

    CreditsWarn findCreditsWarnConf(String username);
}
