package com.raczkowski.app.hashtags;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Service
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    public Set<Hashtag> parseHashtags(String hashtagString) {
        Pattern pattern = Pattern.compile("#\\w+");
        Matcher matcher = pattern.matcher(hashtagString);

        Set<Hashtag> hashtags = new HashSet<>();

        while (matcher.find()) {
            String name = matcher.group();
            Hashtag hashtag = findOrCreateHashtag(name);
            hashtags.add(hashtag);
        }

        return hashtags;
    }

    public Hashtag findOrCreateHashtag(String tag) {
        Hashtag hashtag = hashtagRepository.findByTag(tag);
        if (hashtag == null) {
            hashtag = new Hashtag(tag);
            hashtagRepository.save(hashtag);
        }
        return hashtag;
    }
}
