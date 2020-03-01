package io.cesarcneto.moneytransfer;

import io.cesarcneto.moneytransfer.account.dto.AccountDto;
import io.cesarcneto.moneytransfer.account.dto.AccountInputDto;
import io.cesarcneto.moneytransfer.account.util.TestAccountFacade;
import io.cesarcneto.moneytransfer.transfer.dto.TransferInputDto;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static io.cesarcneto.moneytransfer.Application.*;
import static java.lang.String.format;
import static kong.unirest.Unirest.post;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationIT {

    private static final int NUMBER_OF_TRANSFERS = 1000;

    private String appHost;
    private TestAccountFacade testAccountFacade;

    private AccountDto fromAnAccount;
    private AccountDto toAnotherAccount;

    @BeforeEach
    void setup() {

        int httpPort = getRandomHttpPort();
        appHost = format("http://localhost:%s", httpPort);
        testAccountFacade = new TestAccountFacade(appHost);

        System.setProperty("server.port", String.valueOf(httpPort));
        main(new String[]{});

        fromAnAccount = testAccountFacade.createAccount(
                AccountInputDto.builder().initialBalance(BigDecimal.TEN).build()
        );

        toAnotherAccount = testAccountFacade.createAccount(
                AccountInputDto.builder().initialBalance(BigDecimal.ZERO).build()
        );
    }

    @Test
    void POST_accounts_happyPath() {

        // given
        BigDecimal expectedInitialBalance = new BigDecimal("100");
        AccountInputDto accountInputDto = AccountInputDto.builder()
                .initialBalance(expectedInitialBalance)
                .build();

        //when
        HttpResponse<AccountDto> actualResponse = post(format("%s/%s", appHost, ACCOUNTS_ENDPOINT))
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
        HttpResponse<String> actualResponse = post(format("%s/%s", appHost, ACCOUNTS_ENDPOINT))
                .body(inputBody)
                .asString();

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

        AccountDto expectedAccountDto = testAccountFacade.createAccount(accountInputDto);

        //when
        HttpResponse<AccountDto> actualResponse = Unirest.get(format("%s/accounts/%s", appHost, expectedAccountDto.getId()))
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

        post(format("%s/%s", appHost, ACCOUNTS_ENDPOINT))
                .body(accountInputDto)
                .asObject(AccountDto.class)
                .getBody();

        //when
        HttpResponse<AccountDto> actualResponse = Unirest
                .get(format("%s/%s/%s", appHost, ACCOUNTS_ENDPOINT, UUID.randomUUID()))
                .asObject(AccountDto.class);

        // then
        assertEquals(NOT_FOUND_404, actualResponse.getStatus());
    }

    @Test
    void POST_transfers_happyPath() {

        // given
        BigDecimal transferAmount = BigDecimal.ONE;
        TransferInputDto transferInputDto = TransferInputDto.builder()
                .from(fromAnAccount.getId())
                .to(toAnotherAccount.getId())
                .purpose("thanks for the coffee")
                .occurredAt("2020-03-01T13:00:00.000Z")
                .amount(transferAmount)
                .build();

        BigDecimal expectedFromBalance = fromAnAccount.getBalance().subtract(transferAmount);
        BigDecimal expectedToBalance = toAnotherAccount.getBalance().add(transferAmount);

        //when
        String transferPath = format("%s/%s", appHost, TRANSFERS_ENDPOINT);
        HttpResponse actualResponse = post(transferPath)
                .body(transferInputDto)
                .asEmpty();

        // then
        assertEquals(OK_200, actualResponse.getStatus());

        // and
        assertExpectedBalance(expectedFromBalance, fromAnAccount.getId());
        assertExpectedBalance(expectedToBalance, toAnotherAccount.getId());
    }

    @Test
    void POST_transfers_returnsBadRequestAsThereIsInsufficientBalanceInFromAccount() {

        // given
        BigDecimal transferAmount = new BigDecimal("10.01");
        TransferInputDto transferInputDto = TransferInputDto.builder()
                .from(fromAnAccount.getId())
                .to(toAnotherAccount.getId())
                .purpose("thanks for the coffee")
                .occurredAt("2020-03-01T13:00:00.000Z")
                .amount(transferAmount)
                .build();

        BigDecimal expectedFromBalance = fromAnAccount.getBalance();
        BigDecimal expectedToBalance = toAnotherAccount.getBalance();

        //when
        String transferPath = format("%s/%s", appHost, TRANSFERS_ENDPOINT);
        HttpResponse<String> actualResponse = post(transferPath)
                .body(transferInputDto)
                .asString();

        // then
        assertEquals(BAD_REQUEST_400, actualResponse.getStatus());
        assertNotNull(actualResponse.getBody(), "There should be an error message in there!");

        // and - accounts balances were not affected
        assertExpectedBalance(expectedFromBalance, fromAnAccount.getId());
        assertExpectedBalance(expectedToBalance, toAnotherAccount.getId());
    }

    @ParameterizedTest
    @MethodSource("getInvalidTransferRequests")
    void POST_transfers_returnsBadRequestForInvalidTransferInputDto(TransferInputDto transferInputDto) {

        // given the input
        // when
        String transferPath = format("%s/%s", appHost, TRANSFERS_ENDPOINT);
        HttpResponse<String> actualResponse = post(transferPath)
                .body(transferInputDto)
                .asString();

        // then
        assertEquals(BAD_REQUEST_400, actualResponse.getStatus());
        assertNotNull(actualResponse.getBody(), "There should be an error message in there!");
    }

    @Test
    void POST_transfers_handlesConcurrentTransferRequests() throws InterruptedException, ExecutionException {

        // given
        BigDecimal totalAmountToTransfer = BigDecimal.valueOf(NUMBER_OF_TRANSFERS);
        AccountDto johnsAccount = testAccountFacade.createAccount(
                AccountInputDto.builder().initialBalance(totalAmountToTransfer).build()
        );

        AccountDto annasAccount = testAccountFacade.createAccount(
                AccountInputDto.builder().initialBalance(BigDecimal.ZERO).build()
        );

        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(NUMBER_OF_TRANSFERS);

        Collection<Callable<HttpResponse>> transferRequests = new ArrayList<>();
        Instant baseInstant = Instant.parse("2020-03-01T13:00:00.000Z");
        for (int i = 0; i < NUMBER_OF_TRANSFERS; i++) {
            baseInstant = baseInstant.plus(1, ChronoUnit.MINUTES);
            Instant transferInstant = baseInstant;
            transferRequests.add(() -> {

                TransferInputDto transferInputDto = TransferInputDto.builder()
                        .from(johnsAccount.getId())
                        .to(annasAccount.getId())
                        .purpose("thanks for the coffee")
                        .occurredAt(transferInstant.toString())
                        .amount(BigDecimal.ONE)
                        .build();

                String transferPath = format("%s/%s", appHost, TRANSFERS_ENDPOINT);
                HttpResponse actualResponse = post(transferPath)
                        .body(transferInputDto)
                        .asEmpty();

                latch.countDown();

                return actualResponse;
            });
        }

        //when
        List<Future<HttpResponse>> futureList = executor.invokeAll(transferRequests);
        latch.await();

        // then
        int successes = 0;
        int failures = 0;
        for(Future<HttpResponse> future : futureList) {
            HttpResponse httpResponse = future.get();

            if (OK_200 == httpResponse.getStatus()) {
                successes++;
            } else {
                failures++;
            }
        }

        // and
        BigDecimal annasExpectedBalance = BigDecimal.ONE.multiply(BigDecimal.valueOf(successes));
        BigDecimal johnsExpectedBalance = totalAmountToTransfer.subtract(annasExpectedBalance);

        assertExpectedBalance(annasExpectedBalance, annasAccount.getId());
        assertExpectedBalance(johnsExpectedBalance, johnsAccount.getId());

        System.out.println(format("Number successes: %s", successes));
        System.out.println(format("Number failures: %s", failures));

        executor.shutdown();
    }

    private static Stream<TransferInputDto> getInvalidTransferRequests() {

        UUID randomId1 = UUID.randomUUID();
        UUID randomId2 = UUID.randomUUID();

        return Stream.of(
                TransferInputDto.builder()
                        .from(null)
                        .to(randomId2)
                        .purpose("some purpose")
                        .occurredAt("2020-03-01T13:00:00.000Z")
                        .amount(BigDecimal.ONE)
                        .build(),
                TransferInputDto.builder()
                        .from(randomId1)
                        .to(null)
                        .purpose("some purpose")
                        .occurredAt("2020-03-01T13:00:00.000Z")
                        .amount(BigDecimal.ONE)
                        .build(),
                TransferInputDto.builder()
                        .from(randomId1)
                        .to(randomId1)
                        .purpose("some purpose")
                        .occurredAt("2020-03-01T13:00:00.000Z")
                        .amount(BigDecimal.ONE)
                        .build(),
                TransferInputDto.builder()
                        .from(randomId1)
                        .to(randomId2)
                        .purpose("some purpose")
                        .occurredAt(null)
                        .amount(BigDecimal.ONE)
                        .build(),
                //wrong instant format
                TransferInputDto.builder()
                        .from(randomId1)
                        .to(randomId2)
                        .purpose("some purpose")
                        .occurredAt("2020-03-01T13:00:00")
                        .amount(BigDecimal.ONE)
                        .build(),
                TransferInputDto.builder()
                        .from(randomId1)
                        .to(randomId2)
                        .purpose("some purpose")
                        .occurredAt("2020-03-01T13:00:00.000Z")
                        .amount(null)
                        .build(),
                TransferInputDto.builder()
                        .from(randomId1)
                        .to(randomId2)
                        .purpose("some purpose")
                        .occurredAt("2020-03-01T13:00:00.000Z")
                        .amount(BigDecimal.valueOf(-1))
                        .build()
        );
    }

    private void assertExpectedBalance(BigDecimal expectedBalance, UUID accountId) {
        AccountDto actualAccount = testAccountFacade.getAccount(accountId);
        assertEquals(
                expectedBalance.setScale(2, RoundingMode.HALF_EVEN),
                actualAccount.getBalance().setScale(2, RoundingMode.HALF_EVEN)
        );
    }

    private static final Random RANDOM = new Random();

    private static int getRandomHttpPort() {
        return 8000 + RANDOM.nextInt((9999 - 8000) + 1);
    }

}
