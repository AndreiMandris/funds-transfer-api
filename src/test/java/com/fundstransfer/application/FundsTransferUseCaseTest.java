package com.fundstransfer.application;

import com.fundstransfer.application.port.AccountRepository;
import com.fundstransfer.application.port.ExchangeRateService;
import com.fundstransfer.application.port.TransferRepository;
import com.fundstransfer.application.service.FundsTransferUseCase;
import com.fundstransfer.domain.model.*;
import com.fundstransfer.domain.model.exception.AccountNotFoundException;
import com.fundstransfer.domain.model.exception.ExchangeRateException;
import com.fundstransfer.domain.model.exception.InsufficientFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundsTransferUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    private FundsTransferUseCase fundsTransferUseCase;

    private Account sourceAccount;
    private Account targetAccount;

    @BeforeEach
    void setUp() {
        fundsTransferUseCase = new FundsTransferUseCase(accountRepository, transferRepository, exchangeRateService);

        sourceAccount = Account.builder()
                .ownerId(1L)
                .currency("USD")
                .balance(new BigDecimal("1000.00"))
                .build();
        targetAccount = Account.builder()
                .ownerId(2L)
                .currency("EUR")
                .balance(new BigDecimal("500.00"))
                .build();
    }

    @Test
    void shouldHandleTransferSuccessfully() {
        // Given
        FundsTransferUseCase.TransferRequest request = new FundsTransferUseCase.TransferRequest("acc-1", "acc-2", new BigDecimal("100.00"), "Test transfer");
        Transfer savedTransfer = Transfer.builder()
                .fromAccountId("acc-1")
                .toAccountId("acc-2")
                .amount(new BigDecimal("100.00"))
                .sourceCurrency("USD")
                .targetCurrency("EUR")
                .exchangeRate(new BigDecimal("0.85"))
                .convertedAmount(new BigDecimal("85.00"))
                .description("Test transfer")
                .build();

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("acc-2")).thenReturn(Optional.of(targetAccount));
        when(exchangeRateService.getExchangeRate("USD", "EUR")).thenReturn(new BigDecimal("0.85"));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferRepository.save(any(Transfer.class))).thenReturn(savedTransfer);

        // When
        FundsTransferUseCase.TransferResult result = fundsTransferUseCase.handle(request);

        // Then
        assertNotNull(result);
        assertEquals(TransferStatus.COMPLETED, result.status());
        assertNull(result.errorMessage());

        verify(accountRepository).findById("acc-1");
        verify(accountRepository).findById("acc-2");
        verify(exchangeRateService).getExchangeRate("USD", "EUR");
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void shouldHandleTransferWithSameCurrency() {
        // Given
        Account targetAccountUSD = Account.builder()
                .ownerId(2L)
                .currency("USD")
                .balance(new BigDecimal("500.00"))
                .build();
        FundsTransferUseCase.TransferRequest request = new FundsTransferUseCase.TransferRequest("acc-1", "acc-2", new BigDecimal("100.00"), "Test transfer");
        Transfer savedTransfer = Transfer.builder()
                .fromAccountId("acc-1")
                .toAccountId("acc-2")
                .amount(new BigDecimal("100.00"))
                .sourceCurrency("USD")
                .targetCurrency("USD")
                .exchangeRate(BigDecimal.ONE)
                .convertedAmount(new BigDecimal("100.00"))
                .description("Test transfer")
                .build();

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("acc-2")).thenReturn(Optional.of(targetAccountUSD));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferRepository.save(any(Transfer.class))).thenReturn(savedTransfer);

        // When
        FundsTransferUseCase.TransferResult result = fundsTransferUseCase.handle(request);

        // Then
        assertNotNull(result);
        assertEquals(TransferStatus.COMPLETED, result.status());

        verify(exchangeRateService, never()).getExchangeRate(any(), any());
    }

    @Test
    void shouldFailWhenSourceAccountNotFound() {
        // Given
        FundsTransferUseCase.TransferRequest request = new FundsTransferUseCase.TransferRequest("acc-999", "acc-2", new BigDecimal("100.00"), "Test transfer");

        when(accountRepository.findById("acc-999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AccountNotFoundException.class, () -> fundsTransferUseCase.handle(request));

        verify(accountRepository).findById("acc-999");
        verify(accountRepository, never()).findById("acc-2");
        verify(exchangeRateService, never()).getExchangeRate(any(), any());
    }

    @Test
    void shouldFailWhenTargetAccountNotFound() {
        // Given
        FundsTransferUseCase.TransferRequest request = new FundsTransferUseCase.TransferRequest("acc-1", "acc-999", new BigDecimal("100.00"), "Test transfer");

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("acc-999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AccountNotFoundException.class, () -> fundsTransferUseCase.handle(request));

        verify(accountRepository).findById("acc-1");
        verify(accountRepository).findById("acc-999");
        verify(exchangeRateService, never()).getExchangeRate(any(), any());
    }

    @Test
    void shouldFailWhenInsufficientFunds() {
        // Given
        FundsTransferUseCase.TransferRequest request = new FundsTransferUseCase.TransferRequest("acc-1", "acc-2", new BigDecimal("1500.00"), "Test transfer");

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("acc-2")).thenReturn(Optional.of(targetAccount));

        // When & Then
        assertThrows(InsufficientFundsException.class, () -> fundsTransferUseCase.handle(request));

        verify(accountRepository).findById("acc-1");
        verify(accountRepository).findById("acc-2");
        verify(exchangeRateService, never()).getExchangeRate(any(), any());
    }

    @Test
    void shouldFailWhenExchangeRateServiceFails() {
        // Given
        FundsTransferUseCase.TransferRequest request = new FundsTransferUseCase.TransferRequest("acc-1", "acc-2", new BigDecimal("100.00"), "Test transfer");

        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("acc-2")).thenReturn(Optional.of(targetAccount));
        when(exchangeRateService.getExchangeRate("USD", "EUR"))
                .thenThrow(new ExchangeRateException("Service unavailable"));

        // When & Then
        assertThrows(ExchangeRateException.class, () -> fundsTransferUseCase.handle(request));

        verify(accountRepository).findById("acc-1");
        verify(accountRepository).findById("acc-2");
        verify(exchangeRateService).getExchangeRate("USD", "EUR");
    }
} 
