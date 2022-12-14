package com.raczkowski.app.article;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface ArticleRepository extends JpaRepository<Article,Long> {
    List<Article> findAll();
}
