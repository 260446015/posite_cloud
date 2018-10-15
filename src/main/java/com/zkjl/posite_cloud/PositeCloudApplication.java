package com.zkjl.posite_cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableAspectJAutoProxy
@EnableScheduling
@EnableCaching
public class PositeCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(PositeCloudApplication.class, args);
    }

}
