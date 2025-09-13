package com.raczkowski.app.article;

import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.user.AppUser;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

@NoArgsConstructor
public class ArticleSpecs {
    public static Specification<Article> titleOrAuthorContainsIgnoreCase(String q) {
        if (q == null || q.isBlank()) return null;
        final String like = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> {
            var titleLike = cb.like(cb.lower(root.get("title")), like);
            Join<Article, AppUser> author = root.join("appUser", JoinType.INNER);
            var authorNameLike = cb.like(cb.lower(author.get("firstName")), like);
            var authorLastNameLike = cb.like(cb.lower(author.get("lastName")), like);
            return cb.or(titleLike, authorNameLike, authorLastNameLike);
        };
    }

    public static Specification<Article> statusEquals(ArticleStatus status) {
        if (status == null) return null;
        return (root, cq, cb) -> cb.equal(root.get("status"), status);
    }
}
