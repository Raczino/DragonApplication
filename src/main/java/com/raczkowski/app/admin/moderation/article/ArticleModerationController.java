package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.article.DeletedArticle;
import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.DeletedArticleDto;
import com.raczkowski.app.dto.NonConfirmedArticleDto;
import com.raczkowski.app.dto.RejectedArticleDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/webapi/v1/article")
public class ArticleModerationController {

    ModerationArticleService moderationArticleService;


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

    @GetMapping("/reject/get")
    public ResponseEntity<PageResponse<RejectedArticleDto>> rejectArticle(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "rejectedAt") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(moderationArticleService.getRejectedArticles(page, size, sortBy, sortDirection));
    }

    @GetMapping("/accepted/get")
    public ResponseEntity<PageResponse<ArticleDto>> getAcceptedArticles(
            @RequestParam Long id,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "acceptedAt") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(moderationArticleService.getAcceptedArticlesByUser(id, page, size, sortBy, sortDirection));
    }

    @DeleteMapping("/delete")
    public void deleteArticle(@RequestParam Long id) {
        moderationArticleService.deleteArticle(id);
    }

    @GetMapping("/deleted/get")
    public ResponseEntity<PageResponse<DeletedArticleDto>> getDeletedArticlesByAdmins(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "deletedAt") String sortBy,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(moderationArticleService.getAllDeletedArticlesByAdmins(page, size, sortBy, sortDirection));
    }

    @PostMapping("/pin")
    public void getDeletedArticlesByAdmins(@RequestParam Long id) {
        moderationArticleService.pinArticle(id);
    }
}
