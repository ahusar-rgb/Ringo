package com.ringo.auth;

import com.ringo.model.security.User;

public interface IdProvider {
    User getUserFromToken(String token);
}
