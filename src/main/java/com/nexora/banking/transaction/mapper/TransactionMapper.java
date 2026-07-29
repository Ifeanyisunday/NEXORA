package com.nexora.banking.transaction.mapper;

import com.nexora.banking.transaction.dto.response.TransactionResponse;
import com.nexora.banking.transaction.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionResponse toResponse(Transaction transaction);

}