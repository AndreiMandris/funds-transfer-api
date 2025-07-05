package com.fundstransfer.application.service.impl;

import com.fundstransfer.application.port.TransferRepository;
import com.fundstransfer.application.service.FundsTransferUseCase;
import com.fundstransfer.application.service.TransferService;
import com.fundstransfer.domain.model.Transfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final FundsTransferUseCase fundsTransferUseCase;

    @Override
    public FundsTransferUseCase.TransferResult executeTransfer(FundsTransferUseCase.TransferRequest request) {
        return fundsTransferUseCase.handle(request);
    }

    @Override
    public Transfer save(Transfer transfer) {
        return transferRepository.save(transfer);
    }

    @Override
    public Optional<Transfer> findById(String id) {
        return transferRepository.findById(id);
    }

    @Override
    public List<Transfer> findAll() {
        return transferRepository.findAll();
    }

    @Override
    public List<Transfer> findByFromAccountId(String fromAccountId) {
        return transferRepository.findByFromAccountId(fromAccountId);
    }

    @Override
    public List<Transfer> findByToAccountId(String toAccountId) {
        return transferRepository.findByToAccountId(toAccountId);
    }

    @Override
    public List<Transfer> findByFromAccountIdOrToAccountId(String fromAccountId, String toAccountId) {
        return transferRepository.findByFromAccountIdOrToAccountId(fromAccountId, toAccountId);
    }
}
