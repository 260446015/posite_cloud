package com.zkjl.posite_cloud.service.impl;

import com.zkjl.posite_cloud.common.util.DateUtils;
import com.zkjl.posite_cloud.common.util.PageUtil;
import com.zkjl.posite_cloud.dao.NoticeRepository;
import com.zkjl.posite_cloud.domain.pojo.Notice;
import com.zkjl.posite_cloud.service.INoticeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yindawei
 * @date 2018/10/10 16:40
 **/
@Service
public class NoticeService implements INoticeService {
    @Resource
    private NoticeRepository noticeRepository;

    @Override
    public Notice add(Notice notice) {
        notice.setCreationTime(DateUtils.getFormatString(Calendar.getInstance().getTime()));
        return noticeRepository.save(notice);
    }

    @Override
    public PageImpl<Notice> find(Integer pageNum, Integer pageSize, String startTime, String endTime) {
        if (StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            List<Notice> all = noticeRepository.findAll();
            all = all.stream().filter(action -> {
                if (StringUtils.isNotBlank(startTime)) {
                    return action.getCreationTime().compareTo(startTime) >= 0;
                }
                return true;
            }).filter(action -> {
                if (StringUtils.isNotBlank(endTime)) {
                    return action.getCreationTime().compareTo(startTime) <= 0;
                }
                return true;
            }).collect(Collectors.toList());
            return (PageImpl<Notice>) PageUtil.pageBeagin(all.size(),pageNum,pageSize,all);
        }
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, Sort.Direction.DESC, "creationTime");
        return (PageImpl<Notice>) noticeRepository.findAll(pageRequest);
    }
}
