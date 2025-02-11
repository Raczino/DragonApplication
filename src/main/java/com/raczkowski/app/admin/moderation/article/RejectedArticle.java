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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contentHtml;

    @Column(nullable = false)
    private ZonedDateTime postedDate;

    @OneToOne
    @JoinColumn(nullable = false)
    private AppUser appUser;

    @Enumerated(EnumType.STRING)
    private ArticleStatus status = ArticleStatus.REJECTED;

    private ZonedDateTime rejectedAt;

    @OneToOne
    private AppUser rejectedBy;

    public RejectedArticle(
            String title,
            String content,
            String contentHtml,
            ZonedDateTime postedDate,
            AppUser appUser,
            ZonedDateTime rejectedAt,
            AppUser rejectedBy
    ) {
        this.title = title;
        this.content = content;
        this.contentHtml = contentHtml;
        this.postedDate = postedDate;
        this.appUser = appUser;
        this.rejectedAt = rejectedAt;
        this.rejectedBy = rejectedBy;
    }
}
