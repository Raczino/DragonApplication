package com.raczkowski.app.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublishArticlesListener {

    private final ArticleService articleService;

    @EventListener
    public void on(PublishArticleEvent e) {
        int changed = articleService.publishArticles();
        if (changed > 0)
            System.out.println("PublishArticlesListener: Published " + changed + "articles, from " + e.name() + " event");
    }
}
