package io.cesarcneto.moneytransfer.account.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class AccountInputDto {

    private final BigDecimal initialBalance;

}
