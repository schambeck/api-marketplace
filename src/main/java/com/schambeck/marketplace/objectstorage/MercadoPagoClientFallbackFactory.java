package com.schambeck.marketplace.objectstorage;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoClientFallbackFactory implements FallbackFactory<MercadoPagoClient> {
    @Override
    public MercadoPagoClient create(Throwable cause) {
        return request -> GenerateTokenMercadoPagoResponse.builder()
                .error("Resposta padrão: Serviço temporariamente indisponível")
                .build();
    }
}
