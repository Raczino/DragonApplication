package com.raczkowski.app.article;


import com.raczkowski.app.User.AppUser;
import com.raczkowski.app.User.UserRepository;
import com.raczkowski.app.dto.ArticleDto;
import com.raczkowski.app.dto.DtoMapper;
import lombok.AllArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public String create(ArticleRequest request) {
        AppUser appUser = userRepository.findByEmail(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName());
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
