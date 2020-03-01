package io.cesarcneto.moneytransfer.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class Account {
    private UUID id;
    private UUID version;
    private BigDecimal balance;
}
