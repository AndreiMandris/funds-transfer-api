package com.fundstransfer.integration;

import com.fundstransfer.TestConfig;
import com.fundstransfer.adapter.web.GlobalExceptionHandler;
import com.fundstransfer.adapter.web.dto.AccountDto;
import com.fundstransfer.adapter.web.dto.CreateAccountRequestDto;
import com.fundstransfer.adapter.web.dto.TransferDto;
import com.fundstransfer.adapter.web.dto.TransferRequestDto;
import com.fundstransfer.application.service.AccountService;
import com.fundstransfer.application.service.TransferService;
import com.fundstransfer.application.service.FundsTransferUseCase;
import com.fundstransfer.domain.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
class FundsTransferIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferService transferService;

    private static final String API_PATH = "/api/v1";

    @BeforeEach
    void setUp() {
        // Clear data before each test
        transferService.findAll().forEach(transfer -> transferService.save(transfer));
        accountService.findAll().forEach(account -> accountService.save(account));
    }
    
    /**
     * Helper method to build API URLs
     */
    private String buildUrl(String path) {
        return "http://localhost:" + port + API_PATH + path;
    }

    @Test
    void shouldCreateAccountAndTransferFundsSuccessfully() {
        // Create source account
        CreateAccountRequestDto sourceAccountRequest = new CreateAccountRequestDto(1L, "USD", new BigDecimal("1000.00"));
        ResponseEntity<AccountDto> sourceAccountResponse = restTemplate.postForEntity(
                buildUrl("/accounts"), sourceAccountRequest, AccountDto.class);

        assertEquals(HttpStatus.CREATED, sourceAccountResponse.getStatusCode());
        assertNotNull(sourceAccountResponse.getBody());
        String sourceAccountId = sourceAccountResponse.getBody().id();

        // Create target account
        CreateAccountRequestDto targetAccountRequest = new CreateAccountRequestDto(2L, "EUR", new BigDecimal("500.00"));
        ResponseEntity<AccountDto> targetAccountResponse = restTemplate.postForEntity(
                buildUrl("/accounts"), targetAccountRequest, AccountDto.class);

        assertEquals(HttpStatus.CREATED, targetAccountResponse.getStatusCode());
        assertNotNull(targetAccountResponse.getBody());
        String targetAccountId = targetAccountResponse.getBody().id();

        // Perform transfer
        TransferRequestDto transferRequest = new TransferRequestDto(
                sourceAccountId, targetAccountId, new BigDecimal("100.00"), "Test transfer");
        ResponseEntity<TransferDto> transferResponse = restTemplate.postForEntity(
                buildUrl("/transfers"), transferRequest, TransferDto.class);

        assertEquals(HttpStatus.CREATED, transferResponse.getStatusCode());
        assertNotNull(transferResponse.getBody());

        // Verify transfer details
        TransferDto transfer = transferResponse.getBody();
        assertEquals(sourceAccountId, transfer.fromAccountId());
        assertEquals(targetAccountId, transfer.toAccountId());
        assertEquals(new BigDecimal("100.00"), transfer.amount());
        assertEquals("USD", transfer.sourceCurrency());
        assertEquals("EUR", transfer.targetCurrency());
        assertEquals(com.fundstransfer.domain.model.TransferStatus.COMPLETED, transfer.status());

        // Verify account balances
        var updatedSourceAccount = accountService.findById(sourceAccountId).orElseThrow();
        var updatedTargetAccount = accountService.findById(targetAccountId).orElseThrow();

        assertEquals(0, updatedSourceAccount.getBalance().compareTo(new BigDecimal("900.00")));
        assertTrue(updatedTargetAccount.getBalance().compareTo(new BigDecimal("500.00")) > 0); // Exchange rate applied
    }

    @Test
    void shouldFailTransferWhenInsufficientFunds() {
        // Create source account with low balance
        CreateAccountRequestDto sourceAccountRequest = new CreateAccountRequestDto(1L, "USD", new BigDecimal("50.00"));
        ResponseEntity<AccountDto> sourceAccountResponse = restTemplate.postForEntity(
                buildUrl("/accounts"), sourceAccountRequest, AccountDto.class);

        String sourceAccountId = sourceAccountResponse.getBody().id();

        // Create target account
        CreateAccountRequestDto targetAccountRequest = new CreateAccountRequestDto(2L, "EUR", new BigDecimal("500.00"));
        ResponseEntity<AccountDto> targetAccountResponse = restTemplate.postForEntity(
                buildUrl("/accounts"), targetAccountRequest, AccountDto.class);

        String targetAccountId = targetAccountResponse.getBody().id();

        // Attempt transfer with insufficient funds
        TransferRequestDto transferRequest = new TransferRequestDto(
                sourceAccountId, targetAccountId, new BigDecimal("100.00"), "Test transfer");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> transferResponse = restTemplate.postForEntity(
                buildUrl("/transfers"), transferRequest, GlobalExceptionHandler.ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, transferResponse.getStatusCode());
        assertNotNull(transferResponse.getBody());
        assertEquals("Insufficient Funds", transferResponse.getBody().error());
    }

    @Test
    void shouldFailTransferWhenAccountNotFound() {
        // Create only target account
        CreateAccountRequestDto targetAccountRequest = new CreateAccountRequestDto(2L, "EUR", new BigDecimal("500.00"));
        ResponseEntity<AccountDto> targetAccountResponse = restTemplate.postForEntity(
                buildUrl("/accounts"), targetAccountRequest, AccountDto.class);

        String targetAccountId = targetAccountResponse.getBody().id();

        // Attempt transfer with non-existent source account
        TransferRequestDto transferRequest = new TransferRequestDto(
                "non-existent-id", targetAccountId, new BigDecimal("100.00"), "Test transfer");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> transferResponse = restTemplate.postForEntity(
                buildUrl("/transfers"), transferRequest, GlobalExceptionHandler.ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, transferResponse.getStatusCode());
        assertNotNull(transferResponse.getBody());
        assertEquals("Account Not Found", transferResponse.getBody().error());
    }

    @Test
    void shouldHandleSameCurrencyTransfer() {
        // Create accounts with same currency
        CreateAccountRequestDto sourceAccountRequest = new CreateAccountRequestDto(1L, "USD", new BigDecimal("1000.00"));
        ResponseEntity<AccountDto> sourceAccountResponse = restTemplate.postForEntity(
                buildUrl("/accounts"), sourceAccountRequest, AccountDto.class);

        String sourceAccountId = sourceAccountResponse.getBody().id();

        CreateAccountRequestDto targetAccountRequest = new CreateAccountRequestDto(2L, "USD", new BigDecimal("500.00"));
        ResponseEntity<AccountDto> targetAccountResponse = restTemplate.postForEntity(
                buildUrl("/accounts"), targetAccountRequest, AccountDto.class);

        String targetAccountId = targetAccountResponse.getBody().id();

        // Perform transfer
        TransferRequestDto transferRequest = new TransferRequestDto(
                sourceAccountId, targetAccountId, new BigDecimal("100.00"), "Test transfer");
        ResponseEntity<TransferDto> transferResponse = restTemplate.postForEntity(
                buildUrl("/transfers"), transferRequest, TransferDto.class);

        assertEquals(HttpStatus.CREATED, transferResponse.getStatusCode());
        assertNotNull(transferResponse.getBody());

        TransferDto transfer = transferResponse.getBody();
        assertEquals(BigDecimal.ONE, transfer.exchangeRate());
        assertEquals(new BigDecimal("100.00"), transfer.convertedAmount());
    }
}