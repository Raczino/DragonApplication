package com.raczkowski.app.article;

import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class ArticleComparator implements Comparator<Article> {
    @Override
    public int compare(Article o1, Article o2) {
        return o2.getId().compareTo(o1.getId());
    }
}
