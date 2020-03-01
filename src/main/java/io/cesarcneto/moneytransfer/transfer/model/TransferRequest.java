package io.cesarcneto.moneytransfer.transfer.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class TransferRequest {

    private final UUID from;
    private final UUID to;
    private final BigDecimal amount;
    private String purpose;
    private Instant occurredAt;

}
