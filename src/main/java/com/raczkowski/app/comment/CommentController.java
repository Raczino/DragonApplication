package com.raczkowski.app.comment;

import com.raczkowski.app.dto.CommentDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/v1/api/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    ResponseEntity<String> addComment(@RequestBody CommentRequest commentRequest){
        return ResponseEntity.ok(commentService.addComment(commentRequest));
    }

    @GetMapping()
     ResponseEntity<List<CommentDto>> getAllCommentsByArticleId(@RequestParam Long id){
        return ResponseEntity.ok(commentService.getAllComments(id));
    }

    @PostMapping("/like")
    ResponseEntity<String> likeComment(@RequestParam Long id){
        return ResponseEntity.ok(commentService.likeComment(id));
    }
}
