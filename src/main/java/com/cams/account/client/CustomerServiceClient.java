package com.cams.account.client;

import com.cams.account.dto.CustomerResponse;
import com.cams.account.exception.CustomerNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class CustomerServiceClient {

    private final RestClient restClient;
    private final HttpServletRequest httpServletRequest;

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

        try {

            return restClient.get()
                    .uri("/customers/{id}", customerId)
                    .header("Authorization", authorization)
                    .retrieve()
                    .body(CustomerResponse.class);

        } catch (HttpClientErrorException.NotFound e) {

            throw new CustomerNotFoundException(
                    "Customer not found: " + customerId);
        }
    }
}