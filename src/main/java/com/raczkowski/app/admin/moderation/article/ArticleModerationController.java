package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.common.pagination.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.DeletedArticleDto;
import com.raczkowski.app.dto.NonConfirmedArticleDto;
import com.raczkowski.app.dto.RejectedArticleDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/moderator/article")
public class ArticleModerationController {

    private final ModerationArticleService moderationArticleService;

    @GetMapping("/toConfirm/get")
    public ResponseEntity<PageResponse<NonConfirmedArticleDto>> getArticlesToConfirm(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "postedDate") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(moderationArticleService.getArticleToConfirm(page, size, sortBy, sortDirection));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ArticleDto> confirmArticle(@RequestParam Long articleId) {
        return ResponseEntity.ok(moderationArticleService.confirmArticle(articleId));
    }

    @PostMapping("/reject")
    public ResponseEntity<RejectedArticleDto> rejectArticle(@RequestParam Long articleId) {
        return ResponseEntity.ok(moderationArticleService.rejectArticle(articleId));
    }

    @GetMapping("/get/rejected")
    public ResponseEntity<PageResponse<RejectedArticleDto>> rejectArticle(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "rejectedAt") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(moderationArticleService.getRejectedArticles(page, size, sortBy, sortDirection));
    }

    @GetMapping("/get/accepted")
    public ResponseEntity<PageResponse<ArticleDto>> getAcceptedArticles(
            @RequestParam Long id,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "acceptedAt") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(moderationArticleService.getAcceptedArticlesByUser(id, page, size, sortBy, sortDirection));
    }

    @DeleteMapping("/article/delete")
    public void deleteArticle(@RequestParam Long id) {
        moderationArticleService.deleteArticle(id);
    }

    @GetMapping("/get/deleted")
    public ResponseEntity<PageResponse<DeletedArticleDto>> getDeletedArticlesByAdmins(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "deletedAt") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(moderationArticleService.getAllDeletedArticlesByAdmins(page, size, sortBy, sortDirection));
    }

    @PostMapping("/pin/article")
    public void getDeletedArticlesByAdmins(@RequestParam Long id) {
        moderationArticleService.pinArticle(id);
    }

    @GetMapping("/get/article/from/user")
    public ResponseEntity<PageResponse<NonConfirmedArticleDto>> getPendingArticlesForUser(
            @RequestParam Long id, @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "postedDate") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(moderationArticleService.getPendingArticlesForUser(id, page, size, sortBy, sortDirection));
    }
}
