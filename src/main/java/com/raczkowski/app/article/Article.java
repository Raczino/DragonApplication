package com.raczkowski.app.article;

import com.raczkowski.app.comment.Comment;
import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.hashtags.Hashtag;
import com.raczkowski.app.likes.ArticleLike;
import com.raczkowski.app.user.AppUser;
import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contentHtml;

    private ZonedDateTime postedDate;

    private ZonedDateTime scheduledForDate;

    @OneToOne
    @JoinColumn(
            nullable = false
    )
    private AppUser appUser;

    @Enumerated(EnumType.STRING)
    private ArticleStatus status;

    private ZonedDateTime updatedAt;

    private boolean isUpdated = false;

    @OneToMany(
            mappedBy = "article",
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY
    )
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(
            mappedBy = "article",
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY
    )
    private List<ArticleLike> articleLikes = new ArrayList<>();

    private ZonedDateTime acceptedAt;

    @OneToOne
    private AppUser acceptedBy;

    @Column(columnDefinition = "boolean default false")
    private boolean isPinned;

    @OneToOne
    @JoinColumn(name = "pinned_by")
    private AppUser pinnedBy;

    @ManyToMany(cascade = {CascadeType.ALL, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "article_hashtag",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private List<Hashtag> hashtags = new ArrayList<>();

    private int likesCount;

    private int commentsCount;

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

    public Article(
            String title,
            String content,
            String contentHtml,
            ZonedDateTime postedDate,
            AppUser appUser,
            ZonedDateTime acceptedAt,
            ZonedDateTime scheduledForDate,
            AppUser acceptedBy
    ) {
        this.title = title;
        this.content = content;
        this.contentHtml = contentHtml;
        this.postedDate = postedDate;
        this.appUser = appUser;
        this.acceptedAt = acceptedAt;
        this.scheduledForDate = scheduledForDate;
        this.acceptedBy = acceptedBy;
    }
}
