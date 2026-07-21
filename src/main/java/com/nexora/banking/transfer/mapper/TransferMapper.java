package com.nexora.banking.transfer.mapper;

import com.nexora.banking.transfer.dto.response.TransferResponse;
import com.nexora.banking.transfer.entity.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    @Mapping(
            target = "transferId",
            source = "id"
    )
    @Mapping(
            target = "senderId",
            source = "sender.id"
    )
    @Mapping(
            target = "receiverId",
            source = "receiver.id"
    )
    TransferResponse toResponse(Transfer transfer);

}