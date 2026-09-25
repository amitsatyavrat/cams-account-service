package com.cams.account.service;

import com.cams.account.client.CustomerServiceClient;
import com.cams.account.dto.AccountRequest;
import com.cams.account.dto.AccountResponse;
import com.cams.account.dto.CustomerResponse;
import com.cams.account.entity.Account;
import com.cams.account.exception.AccountNotFoundException;
import com.cams.account.exception.DuplicateResourceException;
import com.cams.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AccountService {


    private static final Logger log =
            LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final CustomerServiceClient customerServiceClient;

    public AccountService(
            AccountRepository accountRepository,
            CustomerServiceClient customerServiceClient) {

        this.accountRepository = accountRepository;
        this.customerServiceClient =
                customerServiceClient;
    }

    public AccountResponse createAccount(
            AccountRequest request) {

        // 1. Verify customer exists
        CustomerResponse customer =
                customerServiceClient.getCustomer(
                        request.getCustomerId());

        // 2. Verify customer is active and not a duplicate request
        if (!"ACTIVE".equalsIgnoreCase(
                customer.getStatus())) {

            throw new IllegalArgumentException(
                    "Cannot create account for inactive customer");
        }

        // check if duplicate
        if (accountRepository
                .existsByCustomerIdAndAccountTypeAndCurrency(
                        request.getCustomerId(),
                        request.getAccountType(),
                        request.getCurrency())) {

            throw new DuplicateResourceException(
                    "Customer already has a "
                            + request.getAccountType()
                            + " account in "
                            + request.getCurrency());
        }

        log.info(
                "Customer validation successful for customerId={}",
                request.getCustomerId()
        );

        // 3. Generate account number
        String accountNumber =
                generateAccountNumber();

        // 4. Create account
        log.info(
                "Creating account for customerId={}, accountType={}, currency={}",
                request.getCustomerId(),
                request.getAccountType(),
                request.getCurrency()
        );
        Account account = new Account(
                accountNumber,
                request.getCustomerId(),
                request.getAccountType(),
                request.getBalance(),
                request.getCurrency(),
                "ACTIVE"
        );

        // 5. Persist
        Account savedAccount =
                accountRepository.save(account);

        log.info(
                "Account created successfully. accountNumber={}, customerId={}",
                savedAccount.getAccountNumber(),
                savedAccount.getCustomerId()
        );

        return mapToResponse(savedAccount);
    }

    public AccountResponse getAccount(Long id) {

        Account account =
                accountRepository.findById(id)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found: " + id));

        return mapToResponse(account);
    }

    public List<AccountResponse> getAllAccounts() {

        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AccountResponse> getAccountsByCustomer(
            Long customerId) {

        // Verify customer exists first
        customerServiceClient.getCustomer(customerId);

        return accountRepository
                .findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AccountResponse updateAccount(
            Long id,
            AccountRequest request) {

        Account account =
                accountRepository.findById(id)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found: " + id));

        account.setAccountType(
                request.getAccountType());

        account.setCurrency(
                request.getCurrency());

        account.setBalance(
                request.getBalance());

        Account updatedAccount =
                accountRepository.save(account);

        return mapToResponse(updatedAccount);
    }

    public void deleteAccount(Long id) {

        Account account =
                accountRepository.findById(id)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found: " + id));

        accountRepository.delete(account);
    }

    private String generateAccountNumber() {

        return "ACC-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }

    private AccountResponse mapToResponse(
            Account account) {

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}