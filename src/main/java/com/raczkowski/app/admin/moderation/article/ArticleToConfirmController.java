package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.common.PageResponse;
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
    public ResponseEntity<PageResponse<NonConfirmedArticleDto>> getArticlesToConfirm(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "postedDate") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(moderationService.getArticleToConfirm(page, size, sortBy, sortDirection));
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
    public ResponseEntity<PageResponse<RejectedArticleDto>> rejectArticle(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "rejectedAt") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(moderationService.getRejectedArticles(page, size, sortBy, sortDirection));
    }

    @GetMapping("/article/accepted/get")
    public ResponseEntity<PageResponse<ArticleDto>> getAcceptedArticles(
            @RequestParam Long id,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "acceptedAt") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(moderationService.getAcceptedArticlesByUser(id, page, size, sortBy, sortDirection));
    }
}
