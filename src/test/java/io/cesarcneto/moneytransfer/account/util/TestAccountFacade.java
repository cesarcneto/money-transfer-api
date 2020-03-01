package io.cesarcneto.moneytransfer.account.util;

import io.cesarcneto.moneytransfer.account.dto.AccountDto;
import io.cesarcneto.moneytransfer.account.dto.AccountInputDto;
import kong.unirest.HttpResponse;

import java.util.UUID;

import static io.cesarcneto.moneytransfer.Application.ACCOUNTS_ENDPOINT;
import static java.lang.String.format;
import static kong.unirest.Unirest.get;
import static kong.unirest.Unirest.post;

public class TestAccountFacade {

    private final String appHost;

    public TestAccountFacade(String appHost) {
        this.appHost = appHost;
    }

    public AccountDto createAccount(AccountInputDto accountInputDto) {
        String path = format("%s/%s", appHost, ACCOUNTS_ENDPOINT);
        HttpResponse<AccountDto> actualResponse = post(path)
                .body(accountInputDto)
                .asObject(AccountDto.class);

        return actualResponse.getBody();
    }

    public AccountDto getAccount(UUID accountId) {
        String path = format("%s/%s/%s", appHost, ACCOUNTS_ENDPOINT, accountId);
        HttpResponse<AccountDto> actualResponse = get(path)
                .asObject(AccountDto.class);

        return actualResponse.getBody();
    }

}
