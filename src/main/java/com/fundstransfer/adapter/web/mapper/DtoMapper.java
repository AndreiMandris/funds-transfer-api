package com.fundstransfer.adapter.web.mapper;

import com.fundstransfer.adapter.web.dto.AccountDto;
import com.fundstransfer.adapter.web.dto.CreateAccountRequestDto;
import com.fundstransfer.adapter.web.dto.TransferDto;
import com.fundstransfer.domain.model.Account;
import com.fundstransfer.domain.model.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DtoMapper {

    AccountDto toDto(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", source = "initialBalance")
    Account toDomain(CreateAccountRequestDto dto);

    TransferDto toDto(Transfer transfer);
}
