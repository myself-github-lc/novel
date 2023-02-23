package com.myself.novel.util;

import org.apache.commons.lang3.StringUtils;

public class CommonItils {

    public static String buildKey(String... args){
        return StringUtils.join(args, "_");
    }
}