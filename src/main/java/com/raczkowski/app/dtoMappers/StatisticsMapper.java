package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.admin.users.ModeratorStatistic;
import com.raczkowski.app.dto.AuthorDto;
import com.raczkowski.app.dto.ModeratorStatisticDto;

public class StatisticsMapper {
    public static ModeratorStatisticDto moderatorStatisticDto(ModeratorStatistic moderatorStatistic) {
        return new ModeratorStatisticDto(
                moderatorStatistic.getId(),
                moderatorStatistic.getApprovedArticleCounter(),
                moderatorStatistic.getRejectedArticleCounter(),
                moderatorStatistic.getDeletedArticleCounter(),
                moderatorStatistic.getDeletedCommentCounter(),
                moderatorStatistic.getDeletedSurveyCounter(),
                moderatorStatistic.getEditedArticleCounter(),
                moderatorStatistic.getPinnedArticleCounter(),
                new AuthorDto(
                        moderatorStatistic.getAppUser().getId(),
                        moderatorStatistic.getAppUser().getFirstName(),
                        moderatorStatistic.getAppUser().getLastName(),
                        moderatorStatistic.getAppUser().getEmail(),
                        moderatorStatistic.getAppUser().isAccountBlocked()
                )
        );
    }
}
