package io.cesarcneto.moneytransfer.account.controller;

import io.cesarcneto.moneytransfer.account.dto.AccountDto;
import io.cesarcneto.moneytransfer.account.dto.AccountInputDto;
import io.cesarcneto.moneytransfer.account.mapper.AccountMapper;
import io.cesarcneto.moneytransfer.account.service.AccountService;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static java.util.Objects.nonNull;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;

@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public void getByAccountId(Context ctx) {
        UUID accountId = ctx.pathParam("accountId", UUID.class).get();
        ctx.json(accountMapper.accountToDto(accountService.getById(accountId)));
    }

    public void createAccount(Context ctx) {

        AccountInputDto accountInputDto = ctx.bodyValidator(AccountInputDto.class)
                .check(input -> nonNull(input.getInitialBalance()), "'initialBalance' must be specified")
                .get();

        AccountDto createdAccountDto = accountMapper.accountToDto(
                accountService.createAccount(accountMapper.accountInputDtoToAccount(accountInputDto))
        );

        ctx.json(createdAccountDto).status(CREATED_201);
    }
}
