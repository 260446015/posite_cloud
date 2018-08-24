package com.zkjl.posite_cloud.service;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.domain.pojo.UpdateTask;
import org.springframework.data.domain.PageImpl;

/**
 * @author yindawei
 * @date 2018/8/14 16:25
 **/
public interface IReportService {

    JSONObject report(String mobile, String username);

    PageImpl<UpdateTask> findReportHistory(String taskid, Integer pageNum, Integer pageSize);
}
