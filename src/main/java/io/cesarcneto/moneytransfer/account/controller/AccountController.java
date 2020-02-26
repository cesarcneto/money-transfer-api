package io.cesarcneto.moneytransfer.account.controller;

import io.cesarcneto.moneytransfer.account.dto.AccountInputDto;
import io.cesarcneto.moneytransfer.account.mapper.AccountMapper;
import io.cesarcneto.moneytransfer.account.service.AccountService;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static org.eclipse.jetty.http.HttpStatus.CREATED_201;

@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public void getByAccountId(Context ctx) {
        UUID accountId = UUID.fromString(ctx.pathParam("accountId"));
        ctx.json(accountMapper.accountToDto(accountService.getById(accountId)));
    }

    public void createAccount(Context ctx) {
        ctx.json(
                accountMapper.accountToDto(
                        accountService.createAccount(
                                accountMapper.accountInputDtoToAccount(
                                        ctx.bodyAsClass(AccountInputDto.class)
                                )
                        )
                )
        ).status(CREATED_201);
    }
}
