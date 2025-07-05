package com.fundstransfer.adapter.persistence.mapper;

import com.fundstransfer.adapter.persistence.entity.TransferEntity;
import com.fundstransfer.adapter.persistence.repository.TransferJpaRepository;
import com.fundstransfer.application.port.TransferRepository;
import com.fundstransfer.domain.model.Transfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransferRepositoryAdapter implements TransferRepository {

    private final TransferJpaRepository transferJpaRepository;
    private final TransferMapper transferMapper;

    @Override
    public Transfer save(Transfer transfer) {
        TransferEntity entity = transferMapper.toEntity(transfer);
        TransferEntity savedEntity = transferJpaRepository.save(entity);
        return transferMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Transfer> findById(String id) {
        return transferJpaRepository.findById(id)
                .map(transferMapper::toDomain);
    }

    @Override
    public List<Transfer> findAll() {
        return transferJpaRepository.findAll().stream()
                .map(transferMapper::toDomain)
                .toList();
    }

    @Override
    public List<Transfer> findByFromAccountId(String fromAccountId) {
        return transferJpaRepository.findByFromAccountId(fromAccountId).stream()
                .map(transferMapper::toDomain)
                .toList();
    }

    @Override
    public List<Transfer> findByToAccountId(String toAccountId) {
        return transferJpaRepository.findByToAccountId(toAccountId).stream()
                .map(transferMapper::toDomain)
                .toList();
    }

    @Override
    public List<Transfer> findByFromAccountIdOrToAccountId(String fromAccountId, String toAccountId) {
        return transferJpaRepository.findByFromAccountIdOrToAccountId(fromAccountId, toAccountId).stream()
                .map(transferMapper::toDomain)
                .toList();
    }
} 
