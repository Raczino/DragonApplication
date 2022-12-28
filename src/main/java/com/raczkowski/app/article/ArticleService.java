package com.raczkowski.app.article;


import com.raczkowski.app.User.AppUser;
import com.raczkowski.app.User.UserRepository;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.DtoMapper;
import com.raczkowski.app.exceptions.ArticleException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public String create(ArticleRequest request) {
        if(request.getTitle().equals("") || request.getContent().equals("")){
            throw  new ArticleException("Title or content can't be empty");
        }
        AppUser appUser = userRepository.findByEmail(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName());
        appUser.incrementArticlesCount();
        articleRepository.save(new Article(
                request.getTitle(),
                request.getContent(),
                ZonedDateTime.now(ZoneOffset.UTC),
                appUser
        ));
        return "saved";
    }

    public List<ArticleDto> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(DtoMapper::articleDtoMapper)
                .collect(Collectors.toList());
    }

    public List<ArticleDto> getArticlesFromUser(Long id) {
        return articleRepository.findAll().stream()
                .filter(article -> article.getAppUser().getId().equals(id))
                .map(DtoMapper::articleDtoMapper)
                .collect(Collectors.toList());
    }
}
