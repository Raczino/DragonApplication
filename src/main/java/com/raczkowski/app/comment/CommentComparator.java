package com.raczkowski.app.comment;

import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

@Component
public class CommentComparator implements Comparator<Comment> {
    @Override
    public int compare(Comment o1, Comment o2) {
        ZonedDateTime first = o1.getPostedDate().truncatedTo(ChronoUnit.MINUTES);
        ZonedDateTime second = o2.getPostedDate().truncatedTo(ChronoUnit.MINUTES);
        if (o1.getLikesNumber() > o2.getLikesNumber()) return -1;
        if (o1.getLikesNumber() < o2.getLikesNumber()) return 1;
        else if (first.isAfter(second)) return -1;
        if (first.isBefore(second)) return 1;
        else
            return 0;
    }
}
