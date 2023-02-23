package com.myself.novel.collect;

import com.myself.novel.constant.CollectorSource;

public interface NovelCollector {

    CollectorSource getSource();

    void collect(String novelName, String novelAuthor);
}