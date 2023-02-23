package com.myself.novel.entity.novel;

import lombok.Data;

@Data
public class Novel {

    private int id;

    /**
     * 名称
     */
    private String name;

    /**
     *  作者
     */
    private String author;

    /**
     * 简述
     */
    private String desc;

    /**
     * 类型 eg:玄幻
     */
    private String type;

    /**
     * 状态：完结 or 连载中...
     */
    private String status;

    /**
     * 章节
     */
    private Chapter chapter;
}