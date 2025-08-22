package com.schambeck.marketplace.objectstorage;

import feign.Param;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor
public class GenerateTokenMercadoPagoRequest {
    @Param("client_id")
    public String clientId;
    @Param("client_secret")
    public String clientSecret;
    @Param("grant_type")
    public String grantType;
    public String code;
    @Param("redirect_uri")
    public String redirectUri;
    public String state;
}
