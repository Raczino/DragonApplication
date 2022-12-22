package com.raczkowski.app.comment;

import com.raczkowski.app.User.AppUser;
import com.raczkowski.app.User.UserRepository;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.dto.CommentDto;
import com.raczkowski.app.dto.DtoMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    public List<CommentDto> getAllComments(Long id){
        return commentRepository.findAll().stream()
                .filter(comment -> comment.getArticleId().equals(id))
                .map(DtoMapper::commentDtoMapper)
                .collect(Collectors.toList());
    }

    public String addComment(CommentRequest commentRequest) {
        AppUser appUser = userRepository.findByEmail(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName());

        if(!userRepository.existsById(commentRequest.getIdOfArticle())){
            throw new IllegalArgumentException("Article with this id doesnt exists");
        }

        System.out.println(commentRequest.getIdOfArticle());
        commentRepository.save(new Comment(
                commentRequest.getContent(),
                ZonedDateTime.now(ZoneOffset.UTC),
                appUser,
                commentRequest.getIdOfArticle()
        ));
        return "Added";
    }
}
