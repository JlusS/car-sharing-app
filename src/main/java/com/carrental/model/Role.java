package com.carrental.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    MANAGER,
    CUSTOMER;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
