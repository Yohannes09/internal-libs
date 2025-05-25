package com.payme.internal.security.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Builder
@RequiredArgsConstructor
public class ServiceTokenSubject implements TokenSubject{
    @NotNull
    private final String serviceNameOrId;
    @NotNull
    private final Set<String> scope;

    @Override
    public String getUsernameOrId() {
        return serviceNameOrId;
    }

    @Override
    public Set<String> getRolesOrScope() {
        return scope;
    }

}
