package com.raczkowski.app.article;

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
    ResponseEntity<String> create(@RequestBody ArticleRequest request){
        return ResponseEntity.ok(articleService.create(request));
    }

    @GetMapping("get/all")
    ResponseEntity<List<Article>> getAllArticles(){
        return ResponseEntity.ok(articleService.getAllArticles());
    }
}
