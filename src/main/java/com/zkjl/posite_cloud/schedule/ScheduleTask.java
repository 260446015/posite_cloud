package com.zkjl.posite_cloud.schedule;

import com.zkjl.posite_cloud.dao.JobInfoRepository;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yindawei
 * @date 2018/8/21 15:31
 **/
//@Component
public class ScheduleTask {

    @Resource
    private JobInfoRepository jobInfoRepository;
    @Scheduled(cron = "* * * * * ?")
    public void timerToNow(){
        System.out.println("now time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }
}
