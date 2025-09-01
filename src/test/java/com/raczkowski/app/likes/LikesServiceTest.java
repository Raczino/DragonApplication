package com.raczkowski.app.likes;


import com.raczkowski.app.article.Article;
import com.raczkowski.app.comment.Comment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikesServiceTest {

    @Mock
    private ArticleLikeRepository articleLikeRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @InjectMocks
    private LikesService likesService;

    @Test
    void shouldReturnSizeOfRepositoryResultWhenArticleLikeNonEmpty() {
        // given
        Article article = new Article();
        when(articleLikeRepository.findAllByArticle(article)).thenReturn(List.of(new ArticleLike(), new ArticleLike(), new ArticleLike()));

        // when
        int count = likesService.getLikesCountForArticle(article);

        // then
        assertEquals(3, count);
        verify(articleLikeRepository).findAllByArticle(article);
        verifyNoMoreInteractions(articleLikeRepository, commentLikeRepository);
    }

    @Test
    void shouldReturnZeroWhenRepositoryReturnsEmptyList() {
        // given
        Article article = new Article();
        when(articleLikeRepository.findAllByArticle(article)).thenReturn(List.of());

        // when
        int count = likesService.getLikesCountForArticle(article);

        // then
        assertEquals(0, count);
        verify(articleLikeRepository).findAllByArticle(article);
        verifyNoMoreInteractions(articleLikeRepository, commentLikeRepository);
    }

    @Test
    void shouldReturnSizeOfRepositoryResultWhenACommentLikeNonEmpty() {
        // given
        Comment comment = new Comment();
        when(commentLikeRepository.findAllByComment(comment)).thenReturn(List.of(new CommentLike(), new CommentLike()));

        // when
        int count = likesService.getLikesCountForComment(comment);

        // then
        assertEquals(2, count);
        verify(commentLikeRepository).findAllByComment(comment);
        verifyNoMoreInteractions(articleLikeRepository, commentLikeRepository);
    }

    @Test
    void getLikesCountForComment_shouldReturnZero_whenRepositoryReturnsEmptyList() {
        // given
        Comment comment = new Comment();
        when(commentLikeRepository.findAllByComment(comment)).thenReturn(List.of());

        // when
        int count = likesService.getLikesCountForComment(comment);

        // then
        assertEquals(0, count);
        verify(commentLikeRepository).findAllByComment(comment);
        verifyNoMoreInteractions(articleLikeRepository, commentLikeRepository);
    }
}
