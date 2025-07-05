package com.fundstransfer.adapter.persistence.mapper;

import com.fundstransfer.adapter.persistence.entity.AccountEntity;
import com.fundstransfer.domain.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    AccountEntity toEntity(Account account);

    Account toDomain(AccountEntity entity);
}
