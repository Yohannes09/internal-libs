package com.payme.internal.security.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Builder
@RequiredArgsConstructor
public class UserTokenSubject implements TokenSubject {
    @NotNull
    private final String usernameOrId;
    @NotNull
    private final Set<String> roles;


    @Override
    public String getUsernameOrId() {
        return usernameOrId;
    }

    @Override
    public Set<String> getRolesOrScope() {
        return roles;
    }

}
