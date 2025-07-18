package com.raczkowski.app.article;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirm;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirmRepository;
import com.raczkowski.app.common.GenericService;
import com.raczkowski.app.common.MetaData;
import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.hashtags.Hashtag;
import com.raczkowski.app.hashtags.HashtagService;
import com.raczkowski.app.history.HistoryService;
import com.raczkowski.app.likes.ArticleLike;
import com.raczkowski.app.likes.ArticleLikeRepository;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ArticleLikeRepository articleLikeRepository;
    private final DeletedArticleService deletedArticleService;
    private final HashtagService hashtagService;
    private final ArticleRequestValidator articleRequestValidator;
    private final ArticleToConfirmRepository articleToConfirmRepository;
    private final FeatureLimitHelperService featureLimitHelperService;
    private final HistoryService historyService;

    public void saveArticle(Article article) {
        articleRepository.save(article);
    }

    public ArticleToConfirm create(ArticleRequest request) {
        AppUser user = userService.getLoggedUser();
        articleRequestValidator.validateArticleRequest(request, user);

        ArticleToConfirm articleToConfirm = new ArticleToConfirm(
                request.getTitle(),
                request.getContent(),
                request.getContentHtml(),
                ZonedDateTime.now(ZoneOffset.UTC),
                request.getScheduledForDate(),
                ArticleStatus.PENDING,
                user
        );

        if (request.getHashtags() != null) {
            List<Hashtag> hashtags = hashtagService.parseHashtags(request.getHashtags());
            articleToConfirm.setHashtags(hashtags);
        }

        articleToConfirmRepository.save(articleToConfirm);
        featureLimitHelperService.incrementFeatureUsage(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Przygotowanie danych do zapisu w JSON
            String payload = objectMapper.writeValueAsString(
                    Map.of("title", request.getTitle(), "content", request.getContent())
            );

            // Wywołanie HistoryService.create z nowym JSON payload
            historyService.create(
                    user,
                    articleToConfirm.getId(),
                    "ARTICLE",
                    payload // tutaj payload jest typu String, tak jak oczekuje HistoryService
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return articleToConfirm;
    }

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public PageResponse<ArticleDto> getAllPaginatedArticles(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        Page<Article> articlePage = GenericService.paginate(pageNumber, pageSize, sortBy, sortDirection, articleRepository::findAllWithPinnedFirst);

        AppUser user = userService.getLoggedUser();

        List<ArticleDto> articleDto = articlePage.getContent().stream()
                .filter(article -> article.getStatus() == ArticleStatus.APPROVED)
                .map(article -> ArticleDtoMapper.articleDtoMapperWithAdditionalFieldsMapper(
                        article,
                        isArticleLiked(article, user)
                ))
                .toList();

        return new PageResponse<>(
                articleDto,
                new MetaData(
                        articlePage.getTotalElements(),
                        articlePage.getTotalPages(),
                        articlePage.getNumber() + 1,
                        articlePage.getSize()
                )
        );
    }

    public List<Article> getArticlesFromUser(Long userID) {
        AppUser user = userRepository.getAppUserById(userID);
        if (user == null) {
            throw new ResponseException("There is no user");
        }
        return articleRepository.findAllByAppUser(user);
    }

    public Page<Article> getArticlesAcceptedByUser(AppUser user, Pageable pageable) {
        return articleRepository.getArticleByAcceptedBy(user, pageable);
    }

    @Transactional
    public void removeArticle(Long id) {
        deletedArticleService.deleteArticle(id, ArticleStatus.DELETED, null);
    }

    public Article getArticleByID(Long id) {
        Article article = articleRepository.findArticleById(id);
        if (article == null) {
            throw new ResponseException("There is no article with provided id");
        }
        return article;
    }

    public void likeArticle(Long id) {
        AppUser user = userService.getLoggedUser();
        Article article = articleRepository.findArticleById(id);

        if (article == null) {
            throw new ResponseException("Article doesnt exists");
        }

        if (!articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article)) {
            articleLikeRepository.save(new ArticleLike(user, article, true));
            if (articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article)) {
                articleRepository.updateArticleLikesCount(article.getId(), 1);
            }
        } else {
            articleLikeRepository.delete(articleLikeRepository.findByArticleAndAppUser(article, user));
            if (!articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article)) {
                articleRepository.updateArticleLikesCount(article.getId(), -1);
            }
        }
    }

    public void updateArticle(ArticleRequest articleRequest) {
        AppUser user = userService.getLoggedUser();
        articleRequestValidator.validateArticleRequest(articleRequest, user);
        Article article = articleRepository.findArticleById(articleRequest.getId());

        if (article == null) {
            throw new ResponseException("There is no article with provided id:" + articleRequest.getId());
        } else if (!article.getAppUser().getId().equals(user.getId())) {
            throw new ResponseException("User doesn't have permission to update this comment");
        }

        if (articleRequest.getTitle() == null) {
            articleRepository.updateArticle(
                    articleRequest.getId(),
                    article.getTitle(),
                    articleRequest.getContent(),
                    ZonedDateTime.now(ZoneOffset.UTC)
            );
        } else if (articleRequest.getContent() == null) {
            articleRepository.updateArticle(
                    articleRequest.getId(),
                    articleRequest.getTitle(),
                    article.getContent(),
                    ZonedDateTime.now(ZoneOffset.UTC)
            );
        } else {
            articleRepository.updateArticle(
                    articleRequest.getId(),
                    articleRequest.getTitle(),
                    articleRequest.getContent(),
                    ZonedDateTime.now(ZoneOffset.UTC));
        }
    }

    public boolean isArticleLiked(Article article, AppUser user) {
        return articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article);
    }

    public void pinArticle(Long id, AppUser user) {
        articleRepository.pinArticle(id, user);
    }

    public int getArticlesCountForUser(AppUser appUser) {
        return articleRepository.findAllByAppUser(appUser).size();
    }

    @Scheduled(fixedRate = 900000)
    @Transactional
    public void publishArticle() {
        List<Article> articlesToPublish = articleRepository.getAllByStatus(ArticleStatus.SCHEDULED);
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);

        for (Article article : articlesToPublish) {
            if (article.getScheduledForDate().truncatedTo(ChronoUnit.MINUTES).isBefore(currentTime) ||
                    article.getScheduledForDate().truncatedTo(ChronoUnit.MINUTES).equals(currentTime)) {
                articleRepository.updateArticleStatus(article.getId());
            }
        }
    }
}
