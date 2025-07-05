package com.fundstransfer.application.service;

import com.fundstransfer.domain.model.Transfer;

import java.util.List;
import java.util.Optional;

public interface TransferService {

    FundsTransferUseCase.TransferResult executeTransfer(FundsTransferUseCase.TransferRequest request);

    Transfer save(Transfer transfer);

    Optional<Transfer> findById(String id);

    List<Transfer> findAll();

    List<Transfer> findByFromAccountId(String fromAccountId);

    List<Transfer> findByToAccountId(String toAccountId);

    List<Transfer> findByFromAccountIdOrToAccountId(String fromAccountId, String toAccountId);
}
