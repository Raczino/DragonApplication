package com.raczkowski.app.admin.operator.users;

import com.raczkowski.app.admin.common.PermissionValidator;
import com.raczkowski.app.common.pagination.GenericService;
import com.raczkowski.app.common.pagination.MetaData;
import com.raczkowski.app.common.pagination.PageResponse;
import com.raczkowski.app.dto.UserDto;
import com.raczkowski.app.dto.UserDtoAssembler;
import com.raczkowski.app.enums.UserRole;
import com.raczkowski.app.exceptions.ErrorMessages;
import com.raczkowski.app.exceptions.ResponseException;
import com.raczkowski.app.user.AppUser;
import com.raczkowski.app.user.UserRepository;
import com.raczkowski.app.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AdminUserService {
    private final PermissionValidator permissionValidator;
    private final ModerationStatisticService moderatorStatisticService;
    private final UserService userService;
    private final UserDtoAssembler userDtoAssembler;
    private final UserRepository userRepository;

    public void changeUserPermission(PermissionRequest permissionRequest) {
        boolean isUserAdmin = permissionValidator.validateAdmin();
        UserRole userRole = invokeUserRole(permissionRequest.getId());

        if (!isUserAdmin) {
            if (userRole.equals(UserRole.ADMIN) || userRole.equals(UserRole.MODERATOR)) {
                throw new ResponseException(ErrorMessages.WRONG_PERMISSION);
            } else {
                userService.updateAppUserByUserRole(permissionRequest.getId(), permissionRequest.getUserRole());
            }
        } else {
            userService.updateAppUserByUserRole(permissionRequest.getId(), permissionRequest.getUserRole());
        }
        if (permissionRequest.getUserRole().equals(UserRole.MODERATOR)) {
            moderatorStatisticService.createStatisticForUser(permissionRequest.getId());
        }
    }

    public void blockUser(Long id) {
        boolean isUserAdmin = permissionValidator.validateAdmin();
        UserRole userRole = invokeUserRole(id);
        if (userService.getUserById(id) == null) {
            throw new ResponseException(ErrorMessages.USER_NOT_EXITS);
        }
        if (!isUserAdmin) {
            if (userRole.equals(UserRole.ADMIN)) {
                throw new ResponseException(ErrorMessages.WRONG_PERMISSION);
            } else {
                userService.blockUser(id, ZonedDateTime.now(ZoneOffset.UTC));
            }
        } else {
            userService.blockUser(id, ZonedDateTime.now(ZoneOffset.UTC));
        }
    }

    public void unBlockUser(Long id) {
        permissionValidator.validateIfUserIsAdminOrModerator();
        if (userService.getUserById(id) == null) {
            throw new ResponseException(ErrorMessages.USER_NOT_EXITS);
        }
        userService.unblockUser(id);
    }

    private UserRole invokeUserRole(Long id) {
        AppUser user = userService.getUserById(id);
        if (user == null) {
            throw new ResponseException(ErrorMessages.USER_NOT_EXITS);
        }
        return user.getUserRole();
    }

    public PageResponse<UserDto> getAllUsers(
            int pageSize,
            int pageNumber
    ) {
        permissionValidator.validateOperatorOrAdmin();
        Page<AppUser> page = GenericService.paginate(pageNumber, pageSize, "id", "asc", userRepository::findAll);
        List<UserDto> users = page.getContent().stream().map(userDtoAssembler::assemble).toList();

        return new PageResponse<>(
                users,
                new MetaData(
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.getNumber() + 1,
                        page.getSize()
                )
        );
    }
}
