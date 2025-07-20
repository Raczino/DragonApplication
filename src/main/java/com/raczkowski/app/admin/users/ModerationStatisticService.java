package com.raczkowski.app.admin.users;

import com.raczkowski.app.dto.ModeratorStatisticDto;
import com.raczkowski.app.dtoMappers.StatisticsMapper;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ModerationStatisticService {
    private final ModeratorStatisticRepository moderatorStatisticRepository;
    private final UserService userService;
    private final StatisticsMapper statisticsMapper;

    public void createStatisticForUser(Long userId) {
        AppUser user = userService.getUserById(userId);
        System.out.println(moderatorStatisticRepository.existsByAppUser(user));
        if (!moderatorStatisticRepository.existsByAppUser(user)) {
            moderatorStatisticRepository.save(new ModeratorStatistic(user));
        }
    }

    public ModeratorStatisticDto getStatisticsForUser(Long id) {
        return statisticsMapper.toModeratorStatisticDto(moderatorStatisticRepository.getModeratorStatisticByAppUser(userService.getUserById(id)));
    }

    public void articleApprovedCounterIncrease(Long userId) {
        moderatorStatisticRepository.increaseApprovedArticleCount(userId);
    }

    public void articleRejectedCounterIncrease(Long userId) {
        moderatorStatisticRepository.increaseRejectedArticleCount(userId);
    }

    public void articleDeletedCounterIncrease(Long userId) {
        moderatorStatisticRepository.increaseDeletedArticleCount(userId);
    }

    public void articlePinnedCounterIncrease(Long userId) {
        moderatorStatisticRepository.increasePinnedArticleCount(userId);
    }

    public void commentDeletedCounterIncrease(Long userId) {
        moderatorStatisticRepository.increaseDeletedCommentCount(userId);
    }

    public void surveyDeletedCounterIncrease(Long userId) {
        moderatorStatisticRepository.increaseDeletedSurveyCount(userId);
    }
}
