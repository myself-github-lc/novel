package com.myself.novel.util;

import cn.hutool.core.io.IoUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicInteger;


public class IdGenerator {

    private static AtomicInteger idGenerator;

    private static AtomicInteger count;

    private static int MAX_GAPE = 30;

    static {
        try {
            int curValue = 1;

            File file = getIdFile();
            if(file.exists()){
                String read = IoUtil.read(new FileReader(file));
                curValue = StringUtils.isNotEmpty(read) ? Integer.parseInt(read) : 1;
            }

            idGenerator = new AtomicInteger(curValue);
            count = new AtomicInteger(MAX_GAPE);
            flushId(file, curValue + MAX_GAPE);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static int generateId(){
        int id = idGenerator.getAndIncrement();
        if (count.decrementAndGet() < 0){
            synchronized (IdGenerator.class){
                if(count.get() < 0){
                    count.set(MAX_GAPE);

                    flushId(getIdFile(), id + MAX_GAPE);
                }
            }
        }

        return id;
    }

    private static File getIdFile(){
        return new File(System.getProperty("user.dir") + "/warehouse/auto_increment.txt");
    }

    @SneakyThrows
    private static void flushId(File file, int nowValue){
        IoUtil.write(new FileOutputStream(file), true, String.valueOf(nowValue).getBytes());
    }
}