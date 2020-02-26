package io.cesarcneto.moneytransfer.account.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Account {

    private final UUID id;
    private final BigDecimal balance;

}
