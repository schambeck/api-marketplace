package com.schambeck.marketplace.objectstorage;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Erro na comunicação com o Mercado Pago: {}", response.toString());
        return new FeignClientException("Erro na comunicação com o Mercado Pago", response.status());
    }
}
