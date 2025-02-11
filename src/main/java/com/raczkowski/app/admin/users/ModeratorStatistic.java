package com.raczkowski.app.admin.users;

import com.raczkowski.app.user.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "moderators_statistics")
@Getter
@Setter
@NoArgsConstructor
public class ModeratorStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int approvedArticleCounter;
    private int rejectedArticleCounter;
    private int deletedArticleCounter;
    private int deletedCommentCounter;
    private int deletedSurveyCounter;
    private int editedArticleCounter;
    private int pinnedArticleCounter;

    @OneToOne
    @JoinColumn(
            nullable = false
    )
    private AppUser appUser;

    public ModeratorStatistic(AppUser appUser) {
        this.appUser = appUser;
    }
}
