package com.raczkowski.app.article;


import com.raczkowski.app.User.AppUser;
import com.raczkowski.app.User.UserRepository;
import lombok.AllArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public String create(ArticleRequest request) {
        AppUser appUser = userRepository
                .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        articleRepository.save(new Article(
                request.getTitle(),
                request.getContent(),
                LocalDate.now(),
                appUser
        ));
        return "saved";
    }

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }
}
