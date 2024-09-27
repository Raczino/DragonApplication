package com.raczkowski.app.hashtags;

import com.raczkowski.app.article.Article;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    public List<Hashtag> parseHashtags(String hashtagString) {
        String[] hashtagNames = hashtagString
                .replaceFirst("^#", "")
                .split("#");

        List<Hashtag> hashtags = new ArrayList<>();
        for (String name : hashtagNames) {
            if (!name.trim().isEmpty()) {
                Hashtag hashtag = findOrCreateHashtag(name);
                hashtags.add(hashtag);
            }
        }
        return hashtags;
    }

    private Hashtag findOrCreateHashtag(String tag) {
        Hashtag hashtag = hashtagRepository.findByTag(tag);
        if (hashtag == null) {
            hashtag = new Hashtag(tag);
            hashtagRepository.save(hashtag);
        }
        return hashtag;
    }
}
