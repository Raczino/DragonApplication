package com.raczkowski.app.admin;

import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.user.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "rejected_article")
@Getter
@Setter
@NoArgsConstructor
public class RejectedArticle {

    @SequenceGenerator(
            name = "rejected_article_sequence",
            sequenceName = "rejected_article_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "rejected_article_sequence"
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
    ArticleStatus status = ArticleStatus.REJECTED;

    public RejectedArticle(
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
