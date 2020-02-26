package io.cesarcneto.moneytransfer.account.mapper;

import io.cesarcneto.moneytransfer.account.dto.AccountDto;
import io.cesarcneto.moneytransfer.account.dto.AccountInputDto;
import io.cesarcneto.moneytransfer.account.model.Account;
import io.cesarcneto.moneytransfer.config.MapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface AccountMapper {

    AccountDto accountToDto(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", source = "initialBalance")
    Account accountInputDtoToAccount(AccountInputDto accountInputDto);

}
