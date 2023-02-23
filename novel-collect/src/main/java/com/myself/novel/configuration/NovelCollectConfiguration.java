package com.myself.novel.configuration;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NovelCollectConfiguration {

    @Bean
    public List<String> myList(){
        return Lists.newArrayList("licong");
    }
}