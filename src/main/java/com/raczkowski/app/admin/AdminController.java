package com.raczkowski.app.admin;

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
public class AdminController {

    AdminService adminService;


    @GetMapping("/article")
    public ResponseEntity<List<NonConfirmedArticleDto>> getArticlesToConfirm() {
        return ResponseEntity.ok(adminService.getArticleToConfirm());
    }

    @PostMapping("/article/confirm")
    public ResponseEntity<ArticleDto> confirmArticle(@RequestParam Long articleId) {
        return ResponseEntity.ok(adminService.confirmArticle(articleId));
    }

    @PostMapping("/article/reject")
    public ResponseEntity<RejectedArticleDto> rejectArticle(@RequestParam Long articleId) {
        return ResponseEntity.ok(adminService.rejectArticle(articleId));
    }

    @GetMapping("/article/reject/get")
    public ResponseEntity<List<RejectedArticleDto>> rejectArticle() {
        return ResponseEntity.ok(adminService.getRejectedArticles());
    }
}
