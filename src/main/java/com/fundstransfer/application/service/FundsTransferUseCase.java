package com.fundstransfer.application.service;

import com.fundstransfer.application.port.AccountRepository;
import com.fundstransfer.application.port.ExchangeRateService;
import com.fundstransfer.application.port.TransferRepository;
import com.fundstransfer.domain.model.*;
import com.fundstransfer.domain.model.exception.AccountNotFoundException;
import com.fundstransfer.domain.model.exception.InsufficientFundsException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FundsTransferUseCase {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final ExchangeRateService exchangeRateService;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransferResult handle(TransferRequest request) {
        var fromAccount = findAccount(request.fromAccountId(), "Source account not found");
        var toAccount = findAccount(request.toAccountId(), "Target account not found");

        validateSufficientFunds(fromAccount, request.amount());

        var currencyConversion = calculateCurrencyConversion(fromAccount, toAccount, request.amount());

        var transfer = createTransferRecord(request, fromAccount, toAccount,
                currencyConversion.exchangeRate(), currencyConversion.convertedAmount());

        try {
            return executeTransfer(fromAccount, toAccount, request.amount(), currencyConversion.convertedAmount(), transfer);
        } catch (OptimisticLockingFailureException e) {
            // Handle concurrent modification
            transfer = transfer.withStatus(TransferStatus.FAILED);
            transfer = transferRepository.save(transfer);
            return new TransferResult(transfer, TransferStatus.FAILED, "Concurrent modification detected. Please try again.");
        } catch (Exception e) {
            return handleTransferError(transfer, e);
        }
    }

    private Account findAccount(String accountId, String errorPrefix) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(errorPrefix + ": " + accountId));
    }

    private void validateSufficientFunds(Account account, BigDecimal amount) {
        if (!account.hasSufficientFunds(amount)) {
            throw new InsufficientFundsException("Insufficient funds in account " + account.getId());
        }
    }

    private record CurrencyConversion(BigDecimal exchangeRate, BigDecimal convertedAmount) {
    }

    private CurrencyConversion calculateCurrencyConversion(Account fromAccount, Account toAccount, BigDecimal amount) {
        var exchangeRate = BigDecimal.ONE;
        var convertedAmount = amount;

        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            exchangeRate = exchangeRateService.getExchangeRate(fromAccount.getCurrency(), toAccount.getCurrency());
            convertedAmount = amount.multiply(exchangeRate).setScale(4, RoundingMode.HALF_UP);
        }

        return new CurrencyConversion(exchangeRate, convertedAmount);
    }

    private Transfer createTransferRecord(TransferRequest request, Account fromAccount, Account toAccount,
                                          BigDecimal exchangeRate, BigDecimal convertedAmount) {
        return Transfer.builder()
                .fromAccountId(request.fromAccountId())
                .toAccountId(request.toAccountId())
                .amount(request.amount())
                .sourceCurrency(fromAccount.getCurrency())
                .targetCurrency(toAccount.getCurrency())
                .exchangeRate(exchangeRate)
                .convertedAmount(convertedAmount)
                .description(request.description())
                .createdAt(LocalDateTime.now())
                .status(TransferStatus.PENDING)
                .build();
    }

    private TransferResult executeTransfer(Account fromAccount, Account toAccount,
                                           BigDecimal amount, BigDecimal convertedAmount,
                                           Transfer transfer) {
        fromAccount.debit(amount);
        accountRepository.save(fromAccount);

        toAccount.credit(convertedAmount);
        accountRepository.save(toAccount);

        transfer = transfer.withStatus(TransferStatus.COMPLETED);
        transfer = transferRepository.save(transfer);

        return new TransferResult(transfer, TransferStatus.COMPLETED, null);
    }

    private TransferResult handleTransferError(Transfer transfer, Exception e) {
        transfer = transfer.withStatus(TransferStatus.FAILED);
        transfer = transferRepository.save(transfer);

        return new TransferResult(transfer, TransferStatus.FAILED, e.getMessage());
    }

    public record TransferRequest(
            String fromAccountId,
            String toAccountId,
            BigDecimal amount,
            String description
    ) {
    }

    public record TransferResult(
            Transfer transfer,
            TransferStatus status,
            String errorMessage
    ) {
    }
} 
