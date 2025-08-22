package com.schambeck.marketplace.objectstorage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor
public class GenerateTokenMercadoPagoResponse {
    @JsonProperty("access_token")
    public String accessToken;
    @JsonProperty("token_type")
    public String tokenType;
    @JsonProperty("expires_in")
    public Long expiresIn;
    @JsonProperty("scope")
    public String scope;
    @JsonProperty("user_id")
    public Long userId;
    @JsonProperty("refresh_token")
    public String refreshToken;
    @JsonProperty("public_key")
    public String publicKey;
    @JsonProperty("live_mode")
    public Boolean liveMode;
    @JsonProperty("error")
    public String error;
}
