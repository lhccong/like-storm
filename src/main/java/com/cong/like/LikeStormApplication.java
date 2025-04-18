package com.cong.like;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author cong
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.cong.like.mapper")
public class LikeStormApplication {

    public static void main(String[] args) {
        SpringApplication.run(LikeStormApplication.class, args);
    }

}
