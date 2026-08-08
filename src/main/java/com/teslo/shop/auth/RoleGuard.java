package com.teslo.shop.auth;

import com.teslo.shop.auth.entity.User;
import com.teslo.shop.common.exception.ApiBadRequestException;
import com.teslo.shop.common.exception.ApiForbiddenException;
import java.util.Arrays;

public final class RoleGuard {

    private RoleGuard() {
    }

    public static void requireAny(User user, String... validRoles) {
        if (user == null) {
            throw new ApiBadRequestException("User not found");
        }
        if (user.getRoles() != null) {
            for (String role : user.getRoles()) {
                if (Arrays.asList(validRoles).contains(role)) {
                    return;
                }
            }
        }
        throw new ApiForbiddenException(
                "User " + user.getFullName() + " need a valid role: [" + String.join(", ", validRoles) + "]");
    }
}
