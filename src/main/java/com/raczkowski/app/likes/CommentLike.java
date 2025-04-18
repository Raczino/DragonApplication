package com.raczkowski.app.likes;

import com.raczkowski.app.comment.Comment;
import com.raczkowski.app.user.AppUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class CommentLike {
    @SequenceGenerator(
            name = "like_sequence",
            sequenceName = "like_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "like_sequence"
    )
    private Long id;

    @OneToOne
    @JoinColumn(
            nullable = false
    )
    private AppUser appUser;

    @OneToOne
    @JoinColumn(
            referencedColumnName = "id",
            nullable = false
    )
    private Comment comment;

    @JoinColumn(
            nullable = false
    )
    private boolean isLiked;


    public CommentLike(AppUser appUser, Comment comment, boolean isLiked) {
        this.appUser = appUser;
        this.comment = comment;
        this.isLiked = isLiked;
    }
}
