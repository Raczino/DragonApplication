package com.raczkowski.app.article;

import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@AllArgsConstructor
@CrossOrigin
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("/add")
    ResponseEntity<String> create(@RequestBody ArticleRequest request) {
        return ResponseEntity.ok(articleService.create(request));
    }

    @GetMapping("/get/all")
    ResponseEntity<PageResponse<ArticleDto>> getAllArticles(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "likesNumber") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(articleService.getAllArticles(page, size, sortBy, sortDirection));
    }

    @GetMapping("/get/from")
    ResponseEntity<List<ArticleDto>> getAllArticlesFromUser(@RequestParam Long id) {
        return ResponseEntity.ok(articleService.getArticlesFromUser(id));
    }

    @GetMapping("/get")
    ResponseEntity<ArticleDto> getArticleByID(@RequestParam Long id) {
        return ResponseEntity.ok(articleService.getArticleByID(id));
    }

    @DeleteMapping("/delete")
    ResponseEntity<String> removeArticle(@RequestParam Long id) {
        return ResponseEntity.ok(articleService.removeArticle(id));
    }

    @PostMapping("/like")
    void likeArticle(@RequestParam Long id) {
        articleService.likeArticle(id);
    }

    @PutMapping("/update")
    void updateArticle(@RequestBody ArticleRequest articleRequest) {
        articleService.updateArticle(articleRequest);
    }

    @GetMapping("/most")
    ResponseEntity<ArticleDto> mostLikeArticle() {
        return ResponseEntity.ok(articleService.getMostLikableArticle());
    }
}
