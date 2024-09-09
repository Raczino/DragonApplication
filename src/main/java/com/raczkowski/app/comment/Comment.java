package com.raczkowski.app.comment;

import com.raczkowski.app.article.Article;
import com.raczkowski.app.likes.CommentLike;
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
public class Comment {
    @SequenceGenerator(
            name = "comment_sequence",
            sequenceName = "comment_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "comment_sequence"
    )
    private Long id;
    @Column(nullable = false)
    private String content;

    private ZonedDateTime postedDate;

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

    private int likesNumber = 0;

    private ZonedDateTime updatedAt;

    private boolean isUpdated;

    @OneToOne(mappedBy = "comment", cascade = CascadeType.REMOVE)
    private CommentLike commentLike;

    @Column(columnDefinition = "boolean default false")
    private boolean isPinned;

    public Comment(
            String content,
            ZonedDateTime postedDate,
            AppUser appUser,
            Article article
    ) {
        this.content = content;
        this.postedDate = postedDate;
        this.appUser = appUser;
        this.article = article;
    }
}
