package com.raczkowski.app.Reddit;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleService;
import com.raczkowski.app.comment.Comment;
import com.raczkowski.app.comment.CommentService;
import com.raczkowski.app.hashtags.Hashtag;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@Service
public class RedditPostService {

    private final CommentService commentService;
    private final ArticleService articleService;
    private final RedditClient redditClient;
    private final UserService userService;

    private static final Long REDDIT_USER_ID = 22L;

    public void getCommentsForArticle() throws IOException {
        List<Article> articles = articleService.getAllArticles();
        AppUser user = userService.getUserById(REDDIT_USER_ID);
        for (Article article : articles) {
            List<Hashtag> hashtags = article.getHashtags();

            for (Hashtag hashtag : hashtags) {
                String hashtagName = hashtag.getTag();
                List<RedditPost> redditPostsList = redditClient.searchPostsOnSubreddit(hashtagName);

                for(RedditPost post: redditPostsList){
                    Comment comment = new Comment(
                            post.getDescription(),
                            post.getCreatedDate(),
                            article,
                            post.getUrl(),
                            post.getAuthor(),
                            user
                    );
                    commentService.createComment(comment);
                }
            }
        }
    }
}
