package com.raczkowski.app.article;

import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dtoMappers.ArticleDtoMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/articles")
@AllArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleDtoMapper articleDtoMapper;

    @PostMapping("/add")
    ResponseEntity<ArticleDto> create(@RequestBody ArticleRequest request) {
        return ResponseEntity.ok(articleDtoMapper.toNonConfirmedArticleDto(articleService.create(request)));
    }

    @GetMapping("/get/all")
    ResponseEntity<PageResponse<ArticleDto>> getAllArticles(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "likesCount") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {

        return ResponseEntity.ok(articleService.getAllArticles(page, size, sortBy, sortDirection));
    }

    @GetMapping("/get/from")
    ResponseEntity<PageResponse<ArticleDto>> getAllArticlesFromUser(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "likesCount") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(articleService.getArticlesFromUser(userId, page, size, sortBy, sortDirection));
    }

    @GetMapping("/get")
    ResponseEntity<ArticleDto> getArticleByID(@RequestParam Long id) {
        Article article = articleService.getArticleByID(id);
        return ResponseEntity.ok(articleDtoMapper.toArticleDto(article));
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
