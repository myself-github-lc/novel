package com.myself.novel.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class HttpUtil {

    public static String httpGet(String url){
        try (HttpResponse response = HttpRequest.get(url).execute()){
            if(response.getStatus() != 200){
                log.error("response status != 200, url:{}", url);
                System.out.println(response.body());
                throw new RuntimeException("response status != 200");
            }
            return response.body();
        }
    }

    public static String httpPostFormData(String url, Map<String, Object> formData){
        try (HttpResponse response = HttpRequest.post(url).form(formData).execute()){
            if(response.getStatus() != 200){
                log.error("response status != 200, url:{}", url);
                System.out.println(response.body());
                throw new RuntimeException("response status != 200");
            }
            return response.body();
        }
    }
}