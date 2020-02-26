package io.cesarcneto.moneytransfer.account;

import io.cesarcneto.moneytransfer.Application;
import io.cesarcneto.moneytransfer.account.controller.AccountController;
import io.cesarcneto.moneytransfer.account.dto.AccountDto;
import io.cesarcneto.moneytransfer.account.dto.AccountInputDto;
import io.cesarcneto.moneytransfer.account.mapper.AccountMapper;
import io.cesarcneto.moneytransfer.account.repository.AccountRepository;
import io.cesarcneto.moneytransfer.account.service.AccountService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.Random;

import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationIT {

    private Application appUnderTest;
    private String appHost;

    @BeforeEach
    void setup() {

        AccountRepository accountRepository = new AccountRepository();
        AccountService accountService = new AccountService(accountRepository);
        AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);
        AccountController accountController = new AccountController(accountService, accountMapper);

        int httpPort = getRandomHttpPort();
        appHost = String.format("http://localhost:%s", httpPort);

        appUnderTest = new Application(accountController);
        appUnderTest.start(httpPort);
    }

    @AfterEach
    void tearDown() {
        appUnderTest.stop();
    }

    @Test
    void POST_accounts_happyPath() {

        // given
        BigDecimal expectedInitialBalance = new BigDecimal("100");
        AccountInputDto accountInputDto = AccountInputDto.builder()
                .initialBalance(expectedInitialBalance)
                .build();

        //when
        HttpResponse<AccountDto> actualResponse = Unirest.post(String.format("%s/accounts", appHost))
                .body(accountInputDto)
                .asObject(AccountDto.class);

        // then
        assertEquals(CREATED_201, actualResponse.getStatus());

        AccountDto actualAccountDto = actualResponse.getBody();
        assertNotNull(actualAccountDto.getId());
        assertEquals(expectedInitialBalance, actualAccountDto.getBalance());
    }

    @Test
    void GET_accountsById_happyPath() {

        // given
        BigDecimal expectedInitialBalance = new BigDecimal("100");
        AccountInputDto accountInputDto = AccountInputDto.builder()
                .initialBalance(expectedInitialBalance)
                .build();

        AccountDto expectedAccountDto = Unirest.post(String.format("%s/accounts", appHost))
                .body(accountInputDto)
                .asObject(AccountDto.class)
                .getBody();

        //when
        HttpResponse<AccountDto> actualResponse = Unirest.get(String.format("%s/accounts/%s", appHost, expectedAccountDto.getId()))
                .asObject(AccountDto.class);

        // then
        assertEquals(OK_200, actualResponse.getStatus());

        AccountDto actualAccountDto = actualResponse.getBody();
        assertEquals(expectedAccountDto.getId(), actualAccountDto.getId());
        assertEquals(expectedAccountDto.getBalance(), actualAccountDto.getBalance());
    }

    private static final Random RANDOM = new Random();
    private static int getRandomHttpPort() {
        return 8000 + RANDOM.nextInt((9999 - 8000) + 1);
    }

}
