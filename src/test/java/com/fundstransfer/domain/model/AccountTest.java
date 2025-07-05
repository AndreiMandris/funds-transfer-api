package com.fundstransfer.domain.model;

import com.fundstransfer.domain.model.exception.InsufficientFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .ownerId(1L)
                .currency("USD")
                .balance(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    void shouldCreateAccountWithValidData() {
        assertNotNull(account);
        assertEquals(1L, account.getOwnerId());
        assertEquals("USD", account.getCurrency());
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
    }

    @Test
    void shouldDebitAmountSuccessfully() {
        BigDecimal amount = new BigDecimal("500.00");
        account.debit(amount);

        assertEquals(new BigDecimal("500.00"), account.getBalance());
    }

    @Test
    void shouldCreditAmountSuccessfully() {
        BigDecimal amount = new BigDecimal("500.00");
        account.credit(amount);

        assertEquals(new BigDecimal("1500.00"), account.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenDebitingMoreThanBalance() {
        BigDecimal amount = new BigDecimal("1500.00");

        assertThrows(InsufficientFundsException.class, () -> account.debit(amount));
        assertEquals(new BigDecimal("1000.00"), account.getBalance()); // Balance should remain unchanged
    }

    @Test
    void shouldReturnTrueWhenHasSufficientFunds() {
        assertTrue(account.hasSufficientFunds(new BigDecimal("500.00")));
        assertTrue(account.hasSufficientFunds(new BigDecimal("1000.00")));
    }

    @Test
    void shouldReturnFalseWhenHasInsufficientFunds() {
        assertFalse(account.hasSufficientFunds(new BigDecimal("1500.00")));
    }

    @Test
    void shouldHandleExactBalanceDebit() {
        account.debit(new BigDecimal("1000.00"));
        assertEquals(new BigDecimal("0.00"), account.getBalance());
    }

    @Test
    void shouldHandleZeroAmountOperations() {
        account.debit(BigDecimal.ZERO);
        account.credit(BigDecimal.ZERO);

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
    }
} 