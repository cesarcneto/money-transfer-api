package io.cesarcneto.moneytransfer.transfer.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class TransferInputDto {

    private final UUID from;
    private final UUID to;
    private final BigDecimal amount;
    private final String purpose;
    private final String occurredAt;

}
