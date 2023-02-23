package com.myself.novel.entity.novel;

import lombok.Data;

import java.util.List;

@Data
public class Chapter {

    /**
     * 最新章节数
     */
    private int latestChapterNum;

    /**
     * 文本文件中 最新章节开始位置
     */
    private long latestChapterTxtBegin;

    /**
     * 文本文件中 最新章节所占的长度
     */
    private long latestChapterTxtLength;

    /**
     * 所有章节
     */
    private List<ChapterContent> contents;
}