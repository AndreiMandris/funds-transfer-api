package com.fundstransfer.adapter.persistence.repository;

import com.fundstransfer.adapter.persistence.entity.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferJpaRepository extends JpaRepository<TransferEntity, String> {

    List<TransferEntity> findByFromAccountId(String fromAccountId);

    List<TransferEntity> findByToAccountId(String toAccountId);

    List<TransferEntity> findByFromAccountIdOrToAccountId(String fromAccountId, String toAccountId);
} 