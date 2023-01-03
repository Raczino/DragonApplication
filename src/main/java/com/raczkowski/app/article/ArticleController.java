package com.raczkowski.app.article;

import com.raczkowski.app.dto.ArticleDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@AllArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("/add")
    ResponseEntity<String> create(@RequestBody ArticleRequest request) {
        return ResponseEntity.ok(articleService.create(request));
    }

    @GetMapping("/get/all")
    ResponseEntity<List<ArticleDto>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    @GetMapping("/get")
    ResponseEntity<List<ArticleDto>> getAllArticlesFromUser(@RequestParam Long id) {
        return ResponseEntity.ok(articleService.getArticlesFromUser(id));
    }

    @DeleteMapping("/delete")
    ResponseEntity<String> removeArticle(@RequestParam Long id) {
        return ResponseEntity.ok(articleService.removeArticle(id));
    }
}
