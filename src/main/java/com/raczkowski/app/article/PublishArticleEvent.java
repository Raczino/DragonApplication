package com.raczkowski.app.article;

import com.raczkowski.app.events.DomainEvent;

public record PublishArticleEvent() implements DomainEvent {

    @Override
    public String name() {
        return "publishArticlesEvent";
    }
}
