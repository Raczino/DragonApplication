package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.user.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ArticleToConfirm { //TODO: sprawdzić czy mozna dziedziczyć po article

    @SequenceGenerator(
            name = "article_to_confirm_sequence",
            sequenceName = "article_to_confirm_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "article_to_confirm_sequence"
    )
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private ZonedDateTime postedDate;

    @OneToOne
    @JoinColumn(nullable = false)
    private AppUser appUser;

    @Enumerated(EnumType.STRING)
    private ArticleStatus status = ArticleStatus.PENDING;

    public ArticleToConfirm(
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
