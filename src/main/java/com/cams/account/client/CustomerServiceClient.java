package com.cams.account.client;

import com.cams.account.dto.CustomerResponse;
import com.cams.account.exception.CustomerNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomerServiceClient {

    private final RestTemplate restTemplate;
    private final HttpServletRequest httpServletRequest;

    public CustomerServiceClient(
            RestTemplate restTemplate,
            HttpServletRequest httpServletRequest) {

        this.restTemplate = restTemplate;
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

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "Authorization",
                authorization);

        String correlationId =
                httpServletRequest.getHeader(
                        "X-Correlation-ID");

        if (correlationId != null &&
                !correlationId.isBlank()) {

            headers.set(
                    "X-Correlation-ID",
                    correlationId);
        }

        HttpEntity<Void> requestEntity =
                new HttpEntity<>(headers);

        try {

            ResponseEntity<CustomerResponse> response =
                    restTemplate.exchange(
                            "http://CAMS-CUSTOMER-SERVICE/customers/{id}",
                            HttpMethod.GET,
                            requestEntity,
                            CustomerResponse.class,
                            customerId);

            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {

            throw new CustomerNotFoundException(
                    "Customer not found: " + customerId);
        }
    }
}