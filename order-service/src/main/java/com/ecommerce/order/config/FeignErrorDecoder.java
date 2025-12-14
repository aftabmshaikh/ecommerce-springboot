package com.ecommerce.order.config;

import com.ecommerce.order.exception.ProductServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 499) {
            return new ProductServiceException(
                    "Client error " + response.status() + " occurred while calling Product Service: " + response.reason()
            );
        } else if (response.status() >= 500) {
            return new ProductServiceException(
                    "Server error " + response.status() + " occurred in Product Service: " + response.reason()
            );
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
