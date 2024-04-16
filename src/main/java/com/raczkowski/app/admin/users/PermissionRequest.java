package com.raczkowski.app.admin.users;

import com.raczkowski.app.enums.UserRole;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PermissionRequest {
    private Long id;

    UserRole userRole;
}
