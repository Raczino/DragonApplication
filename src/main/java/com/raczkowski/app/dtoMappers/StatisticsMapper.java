package com.raczkowski.app.dtoMappers;

import com.raczkowski.app.admin.users.ModeratorStatistic;
import com.raczkowski.app.dto.ModeratorStatisticDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class StatisticsMapper {
    private final AuthorDtoMapper authorDtoMapper;

    public ModeratorStatisticDto toModeratorStatisticDto(ModeratorStatistic moderatorStatistic) {
        return ModeratorStatisticDto.builder()
                .id(moderatorStatistic.getId())
                .approvedArticleCounter(moderatorStatistic.getApprovedArticleCounter())
                .rejectedArticleCounter(moderatorStatistic.getRejectedArticleCounter())
                .deletedArticleCounter(moderatorStatistic.getDeletedArticleCounter())
                .deletedCommentCounter(moderatorStatistic.getDeletedCommentCounter())
                .deletedSurveyCounter(moderatorStatistic.getDeletedSurveyCounter())
                .editedArticleCounter(moderatorStatistic.getEditedArticleCounter())
                .pinnedArticleCounter(moderatorStatistic.getPinnedArticleCounter())
                .moderator(authorDtoMapper.toAuthorDto(moderatorStatistic.getAppUser()))
                .build();
    }
}
