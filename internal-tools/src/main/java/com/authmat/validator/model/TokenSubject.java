package com.authmat.validator.model;

import java.util.Set;

public interface TokenSubject {
    String getUsernameOrId();
    Set<String> getRolesOrScope();
}
