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
    ResponseEntity<String> addComment(@RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(commentService.addComment(commentRequest));
    }

    @GetMapping()
    ResponseEntity<PageResponse<CommentDto>> getAllCommentsByArticleId(
            @RequestParam(name = "id") Long id,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(commentService.getAllCommentsFromArticle(id, page, size, sortDirection));
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
    ResponseEntity<String> updateComment(@RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(commentService.updateComment(commentRequest));
    }
}
