package com.fundstransfer.adapter.persistence.repository;

import com.fundstransfer.adapter.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, String> {

    List<AccountEntity> findByOwnerId(Long ownerId);
} 