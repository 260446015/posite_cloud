package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.domain.pojo.Notice;
import com.zkjl.posite_cloud.service.INoticeService;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author yindawei
 * @date 2018/10/10 16:34
 **/
@RestController
@RequestMapping(value = "api/notice")
public class NoticeController extends BaseController{

    @Resource
    private INoticeService noticeService;

    /**
     * 增加公告信息
     * @param notice
     * @return
     */
    @PostMapping
    public ApiResult add(@RequestBody Notice notice){
        Notice result;
        try {
            result = noticeService.add(notice);
        }catch (Exception e){
            return error("增加公告信息出错:"+e.getMessage());
        }
        return success(result);
    }

    @GetMapping
    public ApiResult find(Integer pageNum,Integer pageSize,String startTime,String endTime){
        PageImpl<Notice> result;
        try{
            result = noticeService.find(pageNum,pageSize,startTime,endTime);
        }catch(Exception e){
            return error("分页查询公告信息出错:"+e.getMessage());
        }
        return successPages(result);
    }
}
