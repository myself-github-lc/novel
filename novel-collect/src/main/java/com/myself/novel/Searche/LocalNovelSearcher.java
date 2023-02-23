package com.myself.novel.Searche;

import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;
import com.myself.novel.entity.novel.Novel;
import com.myself.novel.util.CommonItils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class LocalNovelSearcher {

    private Map<String, Novel> novelMap = new HashMap<>();

    @SneakyThrows
    private void init(){
        File warehouseFile = new File(System.getProperty("user.dir") + "/warehouse");
        File[] files = warehouseFile.listFiles();
        if(!warehouseFile.exists() || ArrayUtils.isEmpty(files)){
            return;
        }

        for (File file : files) {
            File[] novelFiles = file.listFiles();
            if(ArrayUtils.isNotEmpty(novelFiles)){
                for (File novelFile : novelFiles) {
                    if("novel.txt".equals(novelFile.getName())){
                        Novel novel = JSONUtil.toBean(IoUtil.read(new FileReader(novelFile)), Novel.class);
                        novelMap.put(CommonItils.buildKey(novel.getName(), novel.getAuthor()), novel);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        LocalNovelSearcher searcher = new LocalNovelSearcher();
        searcher.init();
        System.out.println(searcher.novelMap);
    }

    public Novel search(String name, String author){
        return novelMap.get(CommonItils.buildKey(name, author));
    }

    @SneakyThrows
    public void save(Novel novel){
        IoUtil.write(
                new FileOutputStream(new File(System.getProperty("user.dir") + "/warehouse" + "/" + novel.getName() + "/novel.txt")),
                true,
                JSONUtil.toJsonStr(novel).getBytes());

        novelMap.put(CommonItils.buildKey(novel.getName(), novel.getAuthor()), novel);
    }
}