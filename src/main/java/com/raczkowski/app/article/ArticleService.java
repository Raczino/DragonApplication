package com.raczkowski.app.article;

import com.raczkowski.app.accountPremium.FeatureKeys;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirm;
import com.raczkowski.app.admin.moderation.article.ArticleToConfirmRepository;
import com.raczkowski.app.common.pagination.GenericService;
import com.raczkowski.app.common.pagination.MetaData;
import com.raczkowski.app.common.pagination.PageMappers;
import com.raczkowski.app.common.pagination.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.hashtags.Hashtag;
import com.raczkowski.app.hashtags.HashtagService;
import com.raczkowski.app.likes.ArticleLike;
import com.raczkowski.app.likes.ArticleLikeRepository;
import com.raczkowski.app.limits.FeatureLimitHelperService;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final UserService userService;
    private final ArticleLikeRepository articleLikeRepository;
    private final DeletedArticleService deletedArticleService;
    private final HashtagService hashtagService;
    private final ArticleRequestValidator articleRequestValidator;
    private final ArticleToConfirmRepository articleToConfirmRepository;
    private final FeatureLimitHelperService featureLimitHelperService;
    private final ArticleDtoMapper articleDtoMapper;

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
            Set<Hashtag> hashtags = hashtagService.parseHashtags(request.getHashtags());
            articleToConfirm.setHashtags(hashtags);
        }

        articleToConfirmRepository.save(articleToConfirm);
        featureLimitHelperService.incrementFeatureUsage(user.getId(), FeatureKeys.ARTICLE_COUNT_PER_WEEK);

        return articleToConfirm;
    }

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public PageResponse<ArticleDto> getAllArticles(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        return paginateAndMapWithLikes(
                pageNumber,
                pageSize,
                sortBy,
                sortDirection,
                articleRepository::findAllWithPinnedFirst,
                userService.getLoggedUser()
        );
    }

    public PageResponse<ArticleDto> getArticlesFromUser(Long userId, int pageNumber, int pageSize, String sortBy, String sortDirection) {
        AppUser user = userService.getUserById(userId);

        return paginateAndMapWithLikes(
                pageNumber,
                pageSize,
                sortBy,
                sortDirection,
                pageable -> articleRepository.getArticleByAcceptedBy(user, pageable),
                user
        );
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
        AppUser user = userService.getLoggedUser();
        if (article == null) {
            throw new ResponseException(ErrorMessages.ARTICLE_ID_NOT_EXISTS);
        }
        article.setLiked(articleLikeRepository.existsArticleLikesByAppUserAndArticle(user, article));
        return article;
    }

    public void likeArticle(Long id) {
        AppUser user = userService.getLoggedUser();
        Article article = articleRepository.findArticleById(id);

        if (article == null) {
            throw new ResponseException(ErrorMessages.ARTICLE_ID_NOT_EXISTS);
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
            throw new ResponseException(ErrorMessages.ARTICLE_ID_NOT_EXISTS);
        } else if (!article.getAppUser().getId().equals(user.getId())) {
            throw new ResponseException(ErrorMessages.WRONG_PERMISSION);
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

    @Transactional
    public int publishArticles() {
        var nowMinute = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        return articleRepository.publishDueUpTo(nowMinute);
    }

    public PageResponse<ArticleDto> searchByQuery(String q, int page, int size, String sortBy, String sortDirection) {
        if (q == null || q.isBlank()) {
            return new PageResponse<>(List.of(), new MetaData(0, 0, page, size));
        }

        AppUser user = userService.getLoggedUser();
        Specification<Article> spec = Specification
                .where(ArticleSpecs.titleOrAuthorContainsIgnoreCase(q))
                .and(ArticleSpecs.statusEquals(ArticleStatus.APPROVED));

        return paginateAndMapWithLikes(
                page,
                size,
                sortBy,
                sortDirection,
                pageable -> articleRepository.findAll(spec, pageable),
                user
        );
    }

    private PageResponse<ArticleDto> paginateAndMapWithLikes(
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Function<Pageable, Page<Article>> pageSupplier,
            AppUser user
    ) {
        Page<Article> page = GenericService.paginate(pageNumber, pageSize, sortBy, sortDirection, pageSupplier);

        return PageMappers.mapPageAndEnrich(
                page,
                articleDtoMapper::toArticleDto,
                (dtos, entities) -> {
                    if (user == null || entities.isEmpty()) return;
                    var ids = entities.stream().map(Article::getId).toList();
                    var likedIds = articleLikeRepository.findLikedArticleIdsByUserAndArticleIds(user, ids);

                    for (ArticleDto dto : dtos) {
                        Long id = dto.getId();
                        if (id != null && likedIds.contains(id)) {
                            dto.setLiked(true);
                        }
                    }
                }
        );
    }

    public PageResponse<ArticleDto> getContentForUser(Long userId, int page, int size, String sortBy, String sortDirection) {
        return paginateAndMapWithLikes(
                page,
                size,
                sortBy,
                sortDirection,
                pageable -> articleRepository.findArticlesByAuthorsIFollow(userId, pageable),
                userService.getLoggedUser()
        );
    }
}
