package com.raczkowski.app.comment;

import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.CommentDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    ResponseEntity<CommentDto> addComment(@RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(commentService.addComment(commentRequest));
    }

    @GetMapping("/article")
    ResponseEntity<PageResponse<CommentDto>> getAllCommentsByArticleId(
            @RequestParam Long articleId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(commentService.getCommentsForArticle(articleId, page, size));
    }

    @GetMapping("/user")
    ResponseEntity<PageResponse<CommentDto>> getAllCommentsForUser(
            @RequestParam Long userId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(commentService.getCommentsForUser(userId, page, size));
    }

    @PostMapping("/like")
    void likeComment(@RequestParam Long id) {
        commentService.likeComment(id);
    }

    @DeleteMapping("/delete")
    ResponseEntity<String> removeComment(@RequestParam Long id) {
        return ResponseEntity.ok(commentService.removeComment(id));
    }

    @PutMapping("/update")
    void updateComment(@RequestBody CommentRequest commentRequest) {
        commentService.updateComment(commentRequest);
    }

    @PostMapping("/pin")
    void pinComment(@RequestParam Long id) {
        commentService.pinComment(id);
    }
}
