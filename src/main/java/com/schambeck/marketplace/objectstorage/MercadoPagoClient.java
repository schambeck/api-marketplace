package com.schambeck.marketplace.objectstorage;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@FeignClient(
        name = "mercado-pago-client",
        url = "https://api.mercadopago.com/oauth/token",
        configuration = FeignConfig.class,
        fallbackFactory = MercadoPagoClientFallbackFactory.class
)
public interface MercadoPagoClient {
    @Headers({"Accept: application/json", "Content-Type: application/x-www-form-urlencoded"})
    @PostMapping
    GenerateTokenMercadoPagoResponse generateToken(Map<String, Object> request);
}

