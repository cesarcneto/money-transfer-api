package io.cesarcneto.moneytransfer.account;

import io.cesarcneto.moneytransfer.Application;
import io.cesarcneto.moneytransfer.account.dto.AccountDto;
import io.cesarcneto.moneytransfer.account.dto.AccountInputDto;
import io.cesarcneto.moneytransfer.shared.dto.ApiErrorResponseDto;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

import static org.eclipse.jetty.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationIT {

    private String appHost;

    @BeforeEach
    void setup() {

        int httpPort = getRandomHttpPort();
        appHost = String.format("http://localhost:%s", httpPort);

        System.setProperty("server.port", String.valueOf(httpPort));
        Application.main(new String[]{});
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
    void POST_accounts_withNoInitialAccountBalanceDefined() {

        // given
        String inputBody = "{}";

        //when
        HttpResponse<ApiErrorResponseDto> actualResponse = Unirest.post(String.format("%s/accounts", appHost))
                .body(inputBody)
                .asObject(ApiErrorResponseDto.class);

        // then
        assertEquals(BAD_REQUEST_400, actualResponse.getStatus());
    }

    @Test
    void GET_accountsById_happyPath() {

        // given
        BigDecimal expectedInitialBalance = new BigDecimal("100.00");
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

    @Test
    void GET_accountsById_returnsNotFoundForUnknownId() {

        // given
        AccountInputDto accountInputDto = AccountInputDto.builder().build();

        Unirest.post(String.format("%s/accounts", appHost))
                .body(accountInputDto)
                .asObject(AccountDto.class)
                .getBody();

        //when
        HttpResponse<AccountDto> actualResponse = Unirest
                .get(String.format("%s/accounts/%s", appHost, UUID.randomUUID()))
                .asObject(AccountDto.class);

        // then
        assertEquals(NOT_FOUND_404, actualResponse.getStatus());
    }

    private static final Random RANDOM = new Random();
    private static int getRandomHttpPort() {
        return 8000 + RANDOM.nextInt((9999 - 8000) + 1);
    }

}
