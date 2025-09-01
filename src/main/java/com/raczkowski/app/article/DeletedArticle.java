package com.raczkowski.app.article;

import com.raczkowski.app.comment.Comment;
import com.raczkowski.app.enums.ArticleStatus;
import com.raczkowski.app.user.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class DeletedArticle {
    @SequenceGenerator(
            name = "deleted_article_sequence",
            sequenceName = "deleted_article_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "deleted_article_sequence"
    )
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String contentHtml;

    private ZonedDateTime postedDate;

    @OneToOne
    @JoinColumn(
            nullable = false
    )
    private AppUser appUser;

    @Enumerated(EnumType.STRING)
    private ArticleStatus status;

    private int likesNumber;

    private ZonedDateTime updatedAt;

    private boolean isUpdated;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    private ZonedDateTime acceptedAt;

    @OneToOne
    private AppUser acceptedBy;

    private ZonedDateTime deletedAt;

    @OneToOne
    private AppUser deletedBy;

    private int commentsCount;

    public DeletedArticle(
            String title,
            String content,
            String contentHtml,
            ZonedDateTime postedDate,
            AppUser appUser,
            ArticleStatus status,
            int likesNumber,
            ZonedDateTime updatedAt,
            boolean isUpdated,
            ZonedDateTime acceptedAt,
            AppUser acceptedBy,
            ZonedDateTime deletedAt,
            AppUser deletedBy,
            int commentsCount
    ) {
        this.title = title;
        this.content = content;
        this.contentHtml = contentHtml;
        this.postedDate = postedDate;
        this.appUser = appUser;
        this.status = status;
        this.likesNumber = likesNumber;
        this.updatedAt = updatedAt;
        this.isUpdated = isUpdated;
        this.acceptedAt = acceptedAt;
        this.acceptedBy = acceptedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
        this.commentsCount = commentsCount;
    }
}
