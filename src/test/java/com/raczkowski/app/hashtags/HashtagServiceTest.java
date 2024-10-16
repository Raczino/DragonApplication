package com.raczkowski.app.hashtags;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HashtagServiceTest {

    @Mock
    private HashtagRepository hashtagRepository;

    @InjectMocks
    private HashtagService hashtagService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldParseSingleHashtag() {
        when(hashtagRepository.findByTag("#hashtag1")).thenReturn(null);

        String input = "#hashtag1";
        List<Hashtag> hashtags = hashtagService.parseHashtags(input);

        assertEquals(1, hashtags.size());
        assertEquals("#hashtag1", hashtags.get(0).getTag());

        verify(hashtagRepository, times(1)).save(any(Hashtag.class));
    }

    @Test
    void shouldParseMultipleHashtags() {
        when(hashtagRepository.findByTag("#hashtag1")).thenReturn(null);
        when(hashtagRepository.findByTag("#hashtag2")).thenReturn(null);

        String input = "#hashtag1#hashtag2";
        List<Hashtag> hashtags = hashtagService.parseHashtags(input);

        assertEquals(2, hashtags.size());
        assertEquals("#hashtag1", hashtags.get(0).getTag());
        assertEquals("#hashtag2", hashtags.get(1).getTag());

        verify(hashtagRepository, times(2)).save(any(Hashtag.class));
    }

    @Test
    void shouldParseHashtagsWithSpaces() {
        when(hashtagRepository.findByTag("#hashtag1")).thenReturn(null);
        when(hashtagRepository.findByTag("#hashtag2")).thenReturn(null);

        String input = "#hashtag1 #hashtag2";
        List<Hashtag> hashtags = hashtagService.parseHashtags(input);

        assertEquals(2, hashtags.size());
        assertEquals("#hashtag1", hashtags.get(0).getTag());
        assertEquals("#hashtag2", hashtags.get(1).getTag());

        verify(hashtagRepository, times(2)).save(any(Hashtag.class));
    }

    @Test
    void shouldParseHashtagsWithDuplicate() {
        when(hashtagRepository.findByTag("#hashtag1")).thenReturn(new Hashtag("#hashtag1"));
        when(hashtagRepository.findByTag("#hashtag2")).thenReturn(null);

        String input = "#hashtag1#hashtag2#hashtag1";
        List<Hashtag> hashtags = hashtagService.parseHashtags(input);

        assertEquals(3, hashtags.size());
        assertEquals("#hashtag1", hashtags.get(0).getTag());
        assertEquals("#hashtag2", hashtags.get(1).getTag());
        assertEquals("#hashtag1", hashtags.get(2).getTag());

        verify(hashtagRepository, times(1)).save(any(Hashtag.class));
        verify(hashtagRepository, times(2)).findByTag("#hashtag1");
        verify(hashtagRepository, times(1)).findByTag("#hashtag2");
    }

    @Test
    void shouldHandleEmptyString() {
        String input = "";
        List<Hashtag> hashtags = hashtagService.parseHashtags(input);

        assertEquals(0, hashtags.size());

        verify(hashtagRepository, never()).save(any(Hashtag.class));
    }

    @Test
    void shouldHandleStringWithoutHash() {
        when(hashtagRepository.findByTag("#hashtag2")).thenReturn(null);

        String input = "hashtag1 #hashtag2";
        List<Hashtag> hashtags = hashtagService.parseHashtags(input);

        assertEquals(1, hashtags.size(), "Powinien być tylko jeden hashtag");
        assertEquals("#hashtag2", hashtags.get(0).getTag(), "Powinien być zapisany tylko #hashtag2");

        verify(hashtagRepository, never()).findByTag("hashtag1");

        verify(hashtagRepository, times(1)).findByTag("#hashtag2");
        verify(hashtagRepository, times(1)).save(any(Hashtag.class));
    }
}
