package com.raczkowski.app.dto;

import com.raczkowski.app.article.Article;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    public static ArticleDto articleDtoMapper(Article article){
        return new ArticleDto(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getPostedDate(),
                new UserDto(
                        article.getAppUser().getId(),
                        article.getAppUser().getFirstName(),
                        article.getAppUser().getLastName(),
                        article.getAppUser().getEmail(),
                        article.getAppUser().getUserRole()
                ));
    }
}
