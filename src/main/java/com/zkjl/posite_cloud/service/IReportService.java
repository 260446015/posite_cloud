package com.zkjl.posite_cloud.service;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.domain.vo.RedistaskVO;
import com.zkjl.posite_cloud.exception.CustomerException;
import org.springframework.data.domain.PageImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yindawei
 * @date 2018/8/14 16:25
 **/
public interface IReportService {

    JSONObject report(String mobile, String username);

    PageImpl<RedistaskVO> findReportHistory(String taskid, Integer pageNum, Integer pageSize, String username);

    JSONObject reportById(String id, String username);

    JSONObject reportByMobileBatch(String[] ids, String username, Boolean ifSelectAll);

    JSONObject reportByTaskBatch(String[] taskid, String username, Boolean ifSelectAll);

    JSONObject reportByPlat(String[] taskid, String webtype, String username);

    void exportPosite(String taskid, String username, HttpServletResponse response, HttpServletRequest request) throws CustomerException;
}
