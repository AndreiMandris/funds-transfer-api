package com.fundstransfer.adapter.persistence.mapper;

import com.fundstransfer.adapter.persistence.entity.TransferEntity;
import com.fundstransfer.domain.model.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransferMapper {
    TransferEntity toEntity(Transfer transfer);

    Transfer toDomain(TransferEntity entity);
}
