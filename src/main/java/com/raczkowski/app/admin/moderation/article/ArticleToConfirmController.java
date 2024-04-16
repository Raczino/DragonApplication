package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.NonConfirmedArticleDto;
import com.raczkowski.app.dto.RejectedArticleDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/webapi/v1/")
public class ArticleToConfirmController {

    ModerationService moderationService;


    @GetMapping("/article")
    public ResponseEntity<List<NonConfirmedArticleDto>> getArticlesToConfirm() {
        return ResponseEntity.ok(moderationService.getArticleToConfirm());
    }

    @PostMapping("/article/confirm")
    public ResponseEntity<ArticleDto> confirmArticle(@RequestParam Long articleId) {
        return ResponseEntity.ok(moderationService.confirmArticle(articleId));
    }

    @PostMapping("/article/reject")
    public ResponseEntity<RejectedArticleDto> rejectArticle(@RequestParam Long articleId) {
        return ResponseEntity.ok(moderationService.rejectArticle(articleId));
    }

    @GetMapping("/article/reject/get")
    public ResponseEntity<List<RejectedArticleDto>> rejectArticle() {
        return ResponseEntity.ok(moderationService.getRejectedArticles());
    }

    @GetMapping("/article/accepted/get")
    public ResponseEntity<List<ArticleDto>> getAcceptedArticles(@RequestParam Long id) {
        return ResponseEntity.ok(moderationService.getAcceptedArticlesByUser(id));
    }
}
