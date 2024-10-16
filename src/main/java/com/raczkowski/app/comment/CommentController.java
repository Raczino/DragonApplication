package com.raczkowski.app.comment;

import com.raczkowski.app.common.PageResponse;
import com.raczkowski.app.dto.CommentDto;
import com.raczkowski.app.hashtags.Hashtag;
import com.raczkowski.app.hashtags.HashtagService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    ResponseEntity<List<CommentDto>> getAllCommentsByArticleId(@RequestParam Long id) {
        return ResponseEntity.ok(commentService.getAllCommentsFromArticle(id));
    }

    @GetMapping("/user")
    ResponseEntity<List<CommentDto>> getAllCommentsForUser(Long id) {
        return ResponseEntity.ok(commentService.getAllCommentsForUser(id));
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

    @PostMapping("/pin")
    void pinComment(@RequestParam Long id) {
        commentService.pinComment(id);
    }
}
