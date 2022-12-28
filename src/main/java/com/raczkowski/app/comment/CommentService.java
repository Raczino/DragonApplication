package com.raczkowski.app.comment;

import com.raczkowski.app.User.AppUser;
import com.raczkowski.app.User.UserRepository;
import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.dto.CommentDto;
import com.raczkowski.app.dto.DtoMapper;
import com.raczkowski.app.exceptions.ArticleException;
import com.sun.xml.bind.v2.model.core.ID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    public List<CommentDto> getAllComments(Long id){
        return commentRepository.findAll().stream()
                .filter(comment -> comment.getAppUser().getId().equals(id))
                .map(DtoMapper::commentDtoMapper)
                .collect(Collectors.toList());
    }

    public String addComment(CommentRequest commentRequest) {
        AppUser appUser = userRepository.findByEmail(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName());
        appUser.incrementCommentsCount();
        if(!userRepository.existsById(commentRequest.getId())){
            throw new ArticleException("Article with this id doesnt exists");
        }else {
            Article article = articleRepository.findArticleById(commentRequest.getId());

            commentRepository.save(new Comment(
                    commentRequest.getContent(),
                    ZonedDateTime.now(ZoneOffset.UTC),
                    appUser,
                    article
            ));
        }
        return "Added";
    }

    public String likeComment(Long id) {
        commentRepository.getById(id).likesIncrement();  //TODO: Implement comment liking method
        return "liked";
    }
}
