package com.myself.novel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class BootStrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(BootStrap.class, args);
        System.out.println(run.getBean("myList"));
    }
}