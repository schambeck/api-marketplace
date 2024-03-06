package com.schambeck.marketplace.exception;

import com.schambeck.marketplace.usecase.user.User;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(Integer id) {
        super(User.class, id);
    }
    
    public UserNotFoundException(String username) {
        super(User.class + " username " + username + " not found.");
    }
}
