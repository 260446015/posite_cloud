package com.zkjl.posite_cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class PositeCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(PositeCloudApplication.class, args);
    }


}
