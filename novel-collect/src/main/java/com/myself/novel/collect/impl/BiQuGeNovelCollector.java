package com.myself.novel.collect.impl;

import cn.hutool.core.io.IoUtil;
import com.myself.novel.Searche.LocalNovelSearcher;
import com.myself.novel.collect.NovelCollector;
import com.myself.novel.constant.CollectorSource;
import com.myself.novel.entity.biquge.BiQuGeDownloadEntity;
import com.myself.novel.entity.novel.Novel;
import com.myself.novel.util.CommonItils;
import com.myself.novel.util.HttpUtil;
import com.myself.novel.util.IdGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 *
 */
@Slf4j
@Component
public class BiQuGeNovelCollector implements NovelCollector {

    @Resource
    private LocalNovelSearcher localNovelSearcher = new LocalNovelSearcher();

    @Override
    public CollectorSource getSource() {
        return CollectorSource.BI_QU_GE;
    }

    @Override
    @SneakyThrows
    public void collect(String novelName, String novelAuthor) {
        //String chapterUrl = "https://www.biquzge.com/info/37814.html";
        //String chapterBody = HttpUtil.httpGet(chapterUrl);
        Novel novel = searchNovelOrElseCollect(novelName, novelAuthor);

        String chapterBody = IoUtil.read(new BufferedReader(new FileReader(new File(System.getProperty("user.dir") + "/temp/chapter.text"))));
        Elements elements = Jsoup.parse(chapterBody)
                .getElementsByAttributeValue("class", "container border3-2 mt8 mb20")
                .get(0)
                .getElementsByAttributeValue("class", "info-chapters flex flex-wrap")
                .get(0)
                .children();

        CompletableFuture[] completableFutures = IntStream.range(0, elements.size())
                .parallel()
                .mapToObj(i -> {
                    int chapterNum = i + 1;
                    File chapterContentFile = new File(System.getProperty("user.dir") + "/warehouse/武动乾坤/temp/" + chapterNum);
                    if(chapterContentFile.exists()){
                        return null;
                    }

                    Element element = elements.get(i);
                    // /info/37814/29433146.html
                    String href = element.attr("href");
                    String chapterPageUrl = "https://www.biquzge.com" + href;
                    String[] art = StringUtils.substringBetween(href, "/info/", ".html").split("/");

                    BiQuGeDownloadEntity downloadEntity = new BiQuGeDownloadEntity();
                    downloadEntity.setChapterNum(chapterNum);
                    downloadEntity.setChapterPageUrl(chapterPageUrl);
                    downloadEntity.setArticleId(art[0].trim());
                    downloadEntity.setChapterId(art[1].trim());
                    downloadEntity.setChapterName(element.text());

                    return CompletableFuture
                            .supplyAsync(parseChapterPage(downloadEntity))
                            .thenAcceptAsync(downloadChapter())
                            .exceptionally(chapterDownloadExceptionally());
                })
                .filter(Objects::nonNull)
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(completableFutures)
                .thenAccept(v -> {
                    System.out.println("小说下载完成...");
                    String contentFilePath = System.getProperty("user.dir") + "/warehouse/武动乾坤/chapter-content.txt";
                    String chapterFilePath = System.getProperty("user.dir") + "/warehouse/武动乾坤/chapter.txt";
                    String chapterContentFilePath = System.getProperty("user.dir") + "/warehouse/武动乾坤/temp";
                    try {
                        AtomicInteger offset = new AtomicInteger(0);
                        PrintWriter contentPrintWriter = new PrintWriter(contentFilePath);
                        PrintWriter chapterPrintWriter = new PrintWriter(chapterFilePath);
                        File contentFile = new File(chapterContentFilePath);
                        Arrays.stream(contentFile.listFiles())
                                .sorted(Comparator.comparingInt(f -> Integer.parseInt(f.getName())))
                                .peek(f -> System.out.println(f.getName()))
                                .forEach(file -> {
                                    try {
                                        String chapterContent = IoUtil.read(new FileReader(file));
                                        String chapterInfo = CommonItils.buildKey(file.getName() + "", offset.get() + "", chapterContent.length() + "");
                                        chapterPrintWriter.println(chapterInfo);
                                        contentPrintWriter.println(chapterContent);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                });

                        chapterPrintWriter.close();
                        contentPrintWriter.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                })
                .join();

    }

    private Function<Throwable, Void> chapterDownloadExceptionally(){
        return t -> {
            Optional.ofNullable(t).ifPresent(th -> log.error("", th));
            return null;
        };
    }

    /**
     * 解析章节页面信息
     * @return
     */
    private Consumer<BiQuGeDownloadEntity> downloadChapter(){
        return downloadEntity -> {
            if(downloadEntity == null){
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(downloadEntity.getChapterName()).append("\r\n");

            Map<String, Object> form = new HashMap<>();
            form.put("articleid", downloadEntity.getArticleId());
            form.put("chapterid", downloadEntity.getChapterId());

            String url = "https://www.biquzge.com/api/reader_js.php";
            for (int j = 1; j <= downloadEntity.getPageSize(); j++) {
                form.put("pid", j);
                String chapterContent = HttpUtil.httpPostFormData(url, form);
                sb.append(chapterContent.replaceAll("</p><p>", "\r\n").replaceAll("</p>", "").replaceAll("<p>", ""));
            }
            sb.append("\n");

            try {
                String filePath = System.getProperty("user.dir") + "/warehouse/武动乾坤/temp/" + downloadEntity.getChapterNum();
                IoUtil.write(new FileOutputStream(filePath), true, sb.toString().getBytes());
            }catch (Exception e){
                e.printStackTrace();
            }

            log.info("第{}章下载完成", downloadEntity.getChapterNum());
        };
    }

    /**
     * 解析章节页面信息
     * @return
     */
    private Supplier<BiQuGeDownloadEntity> parseChapterPage(BiQuGeDownloadEntity downloadEntity){
        String h1 = Jsoup.parse(HttpUtil.httpGet(downloadEntity.getChapterPageUrl()))
                .getElementsByClass("reader-main")
                .get(0)
                .getElementsByTag("h1")
                .text();

        downloadEntity.setPageSize(Integer.parseInt(StringUtils.substringBetween(h1, "（", "）").split("/")[1].trim()));

        return () -> downloadEntity;
    }

    @SneakyThrows
    private Novel searchNovelOrElseCollect(String novelName, String novelAuthor) {
        Novel novel = localNovelSearcher.search(novelName, novelAuthor);
        if(novel != null){
            return novel;
        }

        //主页
        String bookHomeUrl = "https://m.biquzge.com/book/37814.html";
        //String homeBody = HttpUtil.httpGet(bookHomeUrl);
        String homeBody = IoUtil.read(new BufferedReader(new FileReader(new File(System.getProperty("user.dir") + "/temp/home.text"))));
        Document homeDocument = Jsoup.parse(homeBody);

        novel = new Novel();

        novel.setId(IdGenerator.generateId());
        novel.setName(StringUtils.trimToEmpty(homeDocument.getElementsByAttributeValue("property", "og:novel:book_name").attr("content")));
        novel.setAuthor(StringUtils.trimToEmpty(homeDocument.getElementsByAttributeValue("property", "og:novel:author").attr("content").trim()));
        novel.setDesc(StringUtils.trimToEmpty(homeDocument.getElementsByAttributeValue("property", "og:description").attr("content").trim()));
        novel.setType(StringUtils.trimToEmpty(homeDocument.getElementsByAttributeValue("property", "og:novel:category").attr("content").trim()));
        novel.setStatus(StringUtils.trimToEmpty(homeDocument.getElementsByAttributeValue("property", "og:novel:status").attr("content").trim()));
        novel.setName(StringUtils.trimToEmpty(homeDocument.getElementsByAttributeValue("property", "og:novel:book_name").attr("content").trim()));

        localNovelSearcher.save(novel);

        return novel;
    }

    public static void main(String[] args) {
        BiQuGeNovelCollector collector = new BiQuGeNovelCollector();
        collector.collect("武动乾坤", "天蚕土豆");
    }

}