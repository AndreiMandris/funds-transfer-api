package com.fundstransfer.application.service;

import com.fundstransfer.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountService {

    Account save(Account account);

    Optional<Account> findById(String id);

    List<Account> findByOwnerId(Long ownerId);

    List<Account> findAll();

    void deleteById(String id);

    boolean existsById(String id);
}
