package com.payme.internal.security.model;

import java.util.Set;

public interface TokenSubject {
    String getUsernameOrId();
    Set<String> getRolesOrScope();
}
