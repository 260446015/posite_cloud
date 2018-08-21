package com.zkjl.posite_cloud.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yindawei
 * @date 2018/8/21 15:31
 **/
@Component
public class ScheduleTask {

    @Scheduled(cron = "0 0 9 * * ?")
    public void timerToNow(){
        System.out.println("now time:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }
}
