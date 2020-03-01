package io.cesarcneto.moneytransfer.transfer.mapper;

import io.cesarcneto.moneytransfer.config.MapperConfig;
import io.cesarcneto.moneytransfer.transfer.dto.TransferInputDto;
import io.cesarcneto.moneytransfer.transfer.model.TransferRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(config = MapperConfig.class)
public interface TransferMapper {

    @Mapping(target = "occurredAt", source = "occurredAt", qualifiedByName = "fromTimestampStringToInstance")
    TransferRequest transferInputDtoToTransferRequest(TransferInputDto transferInputDto);

    default Instant fromTimestampStringToInstance(String timestamp) {
        return Instant.parse(timestamp);
    }

}
