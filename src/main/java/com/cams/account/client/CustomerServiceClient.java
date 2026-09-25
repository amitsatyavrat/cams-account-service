package com.cams.account.client;

import com.cams.account.dto.CustomerResponse;
import com.cams.account.exception.CustomerNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.slf4j.MDC;

@Component
public class CustomerServiceClient {

    private final RestClient restClient;
    private final HttpServletRequest httpServletRequest;
    private static final String CORRELATION_ID =
            "X-Correlation-ID";
    String correlationId =
            MDC.get("correlationId");

    public CustomerServiceClient(
            @Value("${customer.service.url}") String customerServiceUrl,
            HttpServletRequest httpServletRequest) {

        this.restClient = RestClient.builder()
                .baseUrl(customerServiceUrl)
                .build();

        this.httpServletRequest = httpServletRequest;
    }

    public CustomerResponse getCustomer(Long customerId) {

        String authorization =
                httpServletRequest.getHeader("Authorization");

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            throw new IllegalStateException(
                    "Authorization header is missing");
        }

        /*String correlationId =
                httpServletRequest.getHeader(CORRELATION_ID);*/

        try {

            return restClient.get()
                    .uri("/customers/{id}", customerId)
                    .header("Authorization", authorization)
                    .header(CORRELATION_ID, correlationId)
                    .retrieve()
                    .body(CustomerResponse.class);

        } catch (HttpClientErrorException.NotFound e) {

            throw new CustomerNotFoundException(
                    "Customer not found: " + customerId);
        }
    }
}