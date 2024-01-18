package com.raczkowski.app.article;

import com.raczkowski.app.user.AppUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class Article {
    @SequenceGenerator(
            name = "article_sequence",
            sequenceName = "article_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "article_sequence"
    )
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String content;
    private ZonedDateTime postedDate;

    @OneToOne
    @JoinColumn(
            nullable = false,
            name = "app_user_id"
    )
    private AppUser appUser;

    private int likesNumber=0;

    public Article(
            String title,
            String content,
            ZonedDateTime postedDate,
            AppUser appUser
    ) {
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.appUser = appUser;
    }
}
