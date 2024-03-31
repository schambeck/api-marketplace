package com.schambeck.marketplace.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.security.Principal;
import java.util.Map;

@Service
public class AuthenticationService {
    public String getLoggedUsername(Principal principal) {
        return getTokenAttribute(principal, "preferred_username");
    }
    
    public String getEmail(Principal principal) {
        return getTokenAttribute(principal, "email");
    }
    
    public String getName(Principal principal) {
        return getTokenAttribute(principal, "name");
    }
    
    private static String getTokenAttribute(Principal principal, String attribute) {
        if (!(principal instanceof JwtAuthenticationToken token)) {
            throw new InvalidParameterException("Expected JwtAuthenticationToken.");
        }
        Map<String, Object> attributes = token.getTokenAttributes();
        if (attributes == null || !attributes.containsKey(attribute)) {
            throw new IllegalStateException("'" + attribute + "' not found in token attributes.");
        }
        return (String) attributes.get(attribute);
    }
}
