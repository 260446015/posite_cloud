package com.zkjl.posite_cloud.service;

import com.zkjl.posite_cloud.domain.pojo.Notice;
import org.springframework.data.domain.PageImpl;

/**
 * @author yindawei
 * @date 2018/10/10 16:37
 **/
public interface INoticeService {

    Notice add(Notice notice);

    PageImpl<Notice> find(Integer pageNum, Integer pageSize, String startTime, String endTime);
}
