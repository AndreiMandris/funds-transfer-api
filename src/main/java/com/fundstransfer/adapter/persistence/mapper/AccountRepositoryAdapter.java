package com.fundstransfer.adapter.persistence.mapper;

import com.fundstransfer.adapter.persistence.entity.AccountEntity;
import com.fundstransfer.adapter.persistence.repository.AccountJpaRepository;
import com.fundstransfer.application.port.AccountRepository;
import com.fundstransfer.domain.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;
    private final AccountMapper accountMapper;

    @Override
    public Account save(Account account) {
        AccountEntity entity = accountMapper.toEntity(account);
        AccountEntity savedEntity = accountJpaRepository.save(entity);
        return accountMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Account> findById(String id) {
        return accountJpaRepository.findById(id)
                .map(accountMapper::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return accountJpaRepository.findAll().stream()
                .map(accountMapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findByOwnerId(Long ownerId) {
        return accountJpaRepository.findByOwnerId(ownerId).stream()
                .map(accountMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        accountJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return accountJpaRepository.existsById(id);
    }
} 
