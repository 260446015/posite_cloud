package com.zkjl.posite_cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableAspectJAutoProxy
//@EnableAspectJAutoProxy
public class PositeCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(PositeCloudApplication.class, args);
    }


}
