package com.schambeck.marketplace.usecase.mercadopago.preference;

public class AccessTokenHolder {
    private static String accessToken;
    
    public static String getAccessToken() {
        return accessToken;
    }
    
    public static void setAccessToken(String accessToken) {
        AccessTokenHolder.accessToken = accessToken;
    }
}
