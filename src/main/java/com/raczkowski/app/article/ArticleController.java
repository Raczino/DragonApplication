package com.raczkowski.app.article;

import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@AllArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleStatisticsService articleStatisticsService;

    @PostMapping("/add")
    ResponseEntity<ArticleDto> create(@RequestBody ArticleRequest request) {
        return ResponseEntity.ok(ArticleDtoMapper.nonConfirmedArticleMapper(articleService.create(request)));
    }

    @GetMapping("/get/all")
    ResponseEntity<PageResponse<ArticleDto>> getAllArticles(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "likesNumber") String sortBy, //TODO: need to change sort by Likes
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {

        return ResponseEntity.ok(articleService.getAllPaginatedArticles(page, size, sortBy, sortDirection));
    }

    @GetMapping("/get/from")
    ResponseEntity<List<ArticleDto>> getAllArticlesFromUser(@RequestParam Long id) {
        return ResponseEntity.ok(
                articleService.getArticlesFromUser(id).stream()
                        .map(article -> ArticleDtoMapper.articleDtoMapper(article,
                                articleStatisticsService.getLikesCountForArticle(article)))
                        .toList());
    }

    @GetMapping("/get")
    ResponseEntity<ArticleDto> getArticleByID(@RequestParam Long id) {
        Article article = articleService.getArticleByID(id);
        return ResponseEntity.ok(ArticleDtoMapper.articleDtoMapper(article, articleStatisticsService.getLikesCountForArticle(article)));
    }

    @DeleteMapping("/delete")
    public void removeArticle(@RequestParam Long id) {
        articleService.removeArticle(id);
    }

    @PostMapping("/like")
    void likeArticle(@RequestParam Long id) {
        articleService.likeArticle(id);
    }

    @PutMapping("/update")
    void updateArticle(@RequestBody ArticleRequest articleRequest) {
        articleService.updateArticle(articleRequest);
    }
}
