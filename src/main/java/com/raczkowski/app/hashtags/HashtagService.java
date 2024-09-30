package com.raczkowski.app.hashtags;

import com.raczkowski.app.article.Article;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Service
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    public List<Hashtag> parseHashtags(String hashtagString) {
        Pattern pattern = Pattern.compile("#\\w+");
        Matcher matcher = pattern.matcher(hashtagString);

        List<Hashtag> hashtags = new ArrayList<>();

        while (matcher.find()) {
            String name = matcher.group();
            Hashtag hashtag = findOrCreateHashtag(name);
            hashtags.add(hashtag);
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
