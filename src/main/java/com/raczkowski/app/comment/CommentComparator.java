package com.raczkowski.app.comment;

import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class CommentComparator implements Comparator<Comment> {
    @Override
    public int compare(Comment o1, Comment o2) {
        if (o1.getLikesNumber() > o2.getLikesNumber()) return -1;
        if (o1.getLikesNumber() < o2.getLikesNumber()) return 1;
        return 0; //TODO: Add second condition, if comments have the same likes number, check which one was added earlier
    }
}
