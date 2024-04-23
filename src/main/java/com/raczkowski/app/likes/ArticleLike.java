package com.raczkowski.app.likes;

import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.article.Article;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class ArticleLike {
    @SequenceGenerator(
            name = "like_type_sequence",
            sequenceName = "like_type_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "like_type_sequence"
    )
    private Long id;

    @OneToOne
    @JoinColumn(
            nullable = false
    )
    private AppUser appUser;

    @ManyToOne
    @JoinColumn(
            nullable = false
    )
    private Article article;

    @JoinColumn(
            nullable = false
    )
    private boolean isLiked;

    public ArticleLike(
            AppUser appUser,
            Article article,
            boolean isLiked
    ) {
        this.appUser = appUser;
        this.article = article;
        this.isLiked = isLiked;
    }
}
