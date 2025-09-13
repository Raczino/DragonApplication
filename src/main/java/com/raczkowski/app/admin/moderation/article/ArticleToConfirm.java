package com.raczkowski.app.admin.moderation.article;

import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.hashtags.Hashtag;
import com.raczkowski.app.user.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ArticleToConfirm {
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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contentHtml;

    @Column(nullable = false)
    private ZonedDateTime postedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private AppUser appUser;

    @Enumerated(EnumType.STRING)
    private ArticleStatus status;

    @Column
    ZonedDateTime scheduledForDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "article_to_confirm_hashtag",
            joinColumns = @JoinColumn(name = "article_to_confirm_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<Hashtag> hashtags = new HashSet<>();

    public ArticleToConfirm(
            String title,
            String content,
            String contentHtml,
            ZonedDateTime postedDate,
            ZonedDateTime scheduledForDate,
            ArticleStatus articleStatus,
            AppUser appUser
    ) {
        this.title = title;
        this.content = content;
        this.contentHtml = contentHtml;
        this.postedDate = postedDate;
        this.scheduledForDate = scheduledForDate;
        this.status = articleStatus;
        this.appUser = appUser;
    }
}
