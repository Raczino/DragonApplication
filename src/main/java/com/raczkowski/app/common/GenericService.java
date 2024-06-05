package com.raczkowski.app.common;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.article.ArticleRepository;
import com.raczkowski.app.article.DeletedArticle;
import com.raczkowski.app.article.DeletedArticleRepository;
import com.raczkowski.app.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class GenericService {
    public static <T> Page<T> pagination(JpaRepository<T, Long> repository, int pageNumber, int pageSize, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest
                .of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return repository.findAll(pageable);
    }

    public static Page<DeletedArticle> paginationOfDeletedArticles(DeletedArticleRepository deletedArticleRepository, int pageNumber, int pageSize, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest
                .of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return deletedArticleRepository.findAll(pageable);
    }

    public static Page<Article> paginationOfElementsAcceptedByUser(Optional<AppUser> user, ArticleRepository articleRepository, int pageNumber, int pageSize, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest
                .of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return articleRepository.getArticleByAcceptedBy(user, pageable);
    }
}
