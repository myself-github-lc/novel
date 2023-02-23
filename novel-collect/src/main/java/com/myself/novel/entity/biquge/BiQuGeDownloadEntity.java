package com.myself.novel.entity.biquge;

import lombok.Data;

@Data
public class BiQuGeDownloadEntity {

    private String chapterPageUrl;

    private int chapterNum;

    private String articleId;

    private String chapterId;

    private int pageSize;

    private String chapterName;
}