package com.myself.novel.entity.novel;

import lombok.Data;

@Data
public class ChapterContent {

    /**
     * 章节名称
     */
    private String name;

    /**
     * 章节数
     */
    private int num;

    /**
     * 文本文件中 章节开始位置
     */
    private long txtBegin;

    /**
     * 文本文件中 该章节所占的长度
     */
    private long txtLength;

    /**
     *  章节内容
     */
    private String content;
}