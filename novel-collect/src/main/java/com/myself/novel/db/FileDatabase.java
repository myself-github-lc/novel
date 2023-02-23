package com.myself.novel.db;

import cn.hutool.core.util.ArrayUtil;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileDatabase {

    private void init() throws Exception {
        String basePath = System.getProperty("user.dir") + "/warehouse";
        File baseFile = new File(basePath);
        if(!baseFile.exists()){
            baseFile.mkdir();
            return;
        }

        File[] files = baseFile.listFiles();
        if(ArrayUtil.isEmpty(files)){
            return;
        }

        for (File novelFile : files) {
            System.out.println(novelFile.getName());
        }
    }

    public static void main(String[] args) throws Exception{
        new FileDatabase().init();
    }
}