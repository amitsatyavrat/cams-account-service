package com.cams.account.service;

import com.cams.account.dto.AccountRequest;
import com.cams.account.dto.AccountResponse;
import com.cams.account.dto.CustomerResponse;
import com.cams.account.entity.Account;
import com.cams.account.exception.CustomerNotFoundException;
import com.cams.account.repository.AccountRepository;
import com.cams.account.client.CustomerServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerServiceClient customerServiceClient;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_shouldCreateAccountSuccessfully() {

        AccountRequest request = new AccountRequest();
        request.setCustomerId(1L);
        request.setAccountType("SAVINGS");
        request.setBalance(new BigDecimal("5000.00"));
        request.setCurrency("SGD");

        CustomerResponse customer = new CustomerResponse(
                1L,
                "CUST001",
                "Amit",
                "Kumar",
                "amit@test.com",
                "9999999999",
                "ACTIVE"
        );

        when(customerServiceClient.getCustomer(1L))
                .thenReturn(customer);

        Account savedAccount = new Account();
        //savedAccount.setId(100L);
        savedAccount.setAccountNumber("ACC-12345678");
        savedAccount.setCustomerId(1L);
        savedAccount.setAccountType("SAVINGS");
        savedAccount.setBalance(new BigDecimal("5000.00"));
        savedAccount.setCurrency("SGD");
        savedAccount.setStatus("ACTIVE");

        when(accountRepository.save(any(Account.class)))
                .thenReturn(savedAccount);

        AccountResponse response =
                accountService.createAccount(request);

        assertNotNull(response);
        //assertEquals(100L, response.getId());
        assertEquals("ACC-12345678", response.getAccountNumber());
        assertEquals(1L, response.getCustomerId());
        assertEquals("SAVINGS", response.getAccountType());
        assertEquals(new BigDecimal("5000.00"), response.getBalance());
        assertEquals("SGD", response.getCurrency());

        verify(customerServiceClient)
                .getCustomer(1L);

        verify(accountRepository)
                .save(any(Account.class));
    }

    @Test
    void createAccount_shouldRejectInactiveCustomer() {

        AccountRequest request = new AccountRequest();
        request.setCustomerId(1L);
        request.setAccountType("SAVINGS");
        request.setBalance(new BigDecimal("5000.00"));
        request.setCurrency("SGD");

        CustomerResponse customer = new CustomerResponse(
                1L,
                "CUST001",
                "Amit",
                "Kumar",
                "amit@test.com",
                "9999999999",
                "INACTIVE"
        );
        when(customerServiceClient.getCustomer(1L))
                .thenReturn(customer);

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(request)
        );

        verify(customerServiceClient)
                .getCustomer(1L);

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    @Test
    void createAccount_shouldFail_whenCustomerDoesNotExist() {

        AccountRequest request = new AccountRequest();
        request.setCustomerId(999L);
        request.setAccountType("SAVINGS");
        request.setBalance(new BigDecimal("5000.00"));
        request.setCurrency("SGD");

        when(customerServiceClient.getCustomer(999L))
                .thenThrow(
                        new CustomerNotFoundException(
                                "Customer not found: 999"));

        assertThrows(
                CustomerNotFoundException.class,
                () -> accountService.createAccount(request)
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }




}