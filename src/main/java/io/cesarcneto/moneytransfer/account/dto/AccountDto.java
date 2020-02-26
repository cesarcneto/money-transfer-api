package io.cesarcneto.moneytransfer.account.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class AccountDto {

    private final UUID id;
    private final BigDecimal balance;

}
