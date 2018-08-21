package com.zkjl.posite_cloud;

import com.zkjl.posite_cloud.service.IFileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PositeCloudApplicationTests {

    @Resource
    private IFileService fileService;
    @Test
    public void contextLoads() {
    }

}
