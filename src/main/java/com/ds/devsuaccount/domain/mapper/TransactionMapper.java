package com.ds.devsuaccount.domain.mapper;

import com.ds.devsuaccount.domain.entity.Transfer;
import com.ds.devsuaccount.infraestructure.database.entity.TransactionDbEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transfer entityToDTO(TransactionDbEntity entity);

    @Mapping(source = "paymentDate", target = "paymentDate")
    @Mapping(source = "type", target = "paymentType")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "origin.account.id", target = "originAccount")
    @Mapping(source = "origin.account.type", target = "originAccountType")
    @Mapping(source = "origin.client.name", target = "originClientName")
    @Mapping(source = "origin.client.id", target = "originClientId")
    @Mapping(source = "destination.account.id", target = "destinationAccount")
    @Mapping(source = "destination.account.type", target = "destinationAccountType")
    @Mapping(source = "destination.client.name", target = "destinationClientName")
    @Mapping(source = "destination.client.id", target = "destinationClientId")
    @Mapping(source = "statusDetail", target = "statusDetail")
    TransactionDbEntity dtoToEntity(Transfer transfer);

    @AfterMapping
    default void addTimeStamp(@MappingTarget TransactionDbEntity transactionDbEntity) {
        transactionDbEntity.setTimeStamp(LocalDateTime.now());
    }
}
