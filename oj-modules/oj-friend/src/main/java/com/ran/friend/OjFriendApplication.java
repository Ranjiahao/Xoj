package com.ran.friend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ran.**.mapper")
public class OjFriendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OjFriendApplication.class, args);
    }
}