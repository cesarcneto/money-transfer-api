package io.cesarcneto.moneytransfer.transfer.service;

import io.cesarcneto.moneytransfer.account.model.Account;
import io.cesarcneto.moneytransfer.account.service.AccountService;
import io.cesarcneto.moneytransfer.transfer.exception.InsufficientBalanceInAccountException;
import io.cesarcneto.moneytransfer.transfer.exception.UpdateBalanceException;
import io.cesarcneto.moneytransfer.transfer.model.TransferRequest;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

class TransferServiceTest {

    @Mock
    private Jdbi jdbiMock;

    @Mock
    private AccountService accountServiceMock;

    private TransferService underTest;

    @BeforeEach
    void setup() {
        initMocks(this);
        underTest = new TransferService(jdbiMock, accountServiceMock);
    }

    @Test
    void executeTransferRequest_happyPath() {

        // given
        UUID fromAccountId = UUID.randomUUID();
        Account fromAccount = Account.builder().id(fromAccountId).balance(BigDecimal.TEN).build();

        UUID toAccountId = UUID.randomUUID();
        Account toAccount = Account.builder().id(toAccountId).balance(BigDecimal.ZERO).build();

        TransferRequest transferRequest = TransferRequest.builder()
                .from(fromAccountId)
                .to(toAccountId)
                .amount(BigDecimal.ONE)
                .occurredAt(Instant.now())
                .build();
        // and
        given(accountServiceMock.getAccountsById(List.of(fromAccountId, toAccountId)))
                .willReturn(Map.of(fromAccountId, fromAccount, toAccountId, toAccount));

        given(accountServiceMock.updateAccountsBalances(List.of(
                fromAccount.toBuilder().balance(BigDecimal.TEN.subtract(BigDecimal.ONE)).build(),
                toAccount.toBuilder().balance(BigDecimal.ZERO.add(BigDecimal.ONE)).build()))
        ).willReturn(2);

        // and
        int expectedNumberOfUpdatedBalances = 2;

        // when
        int actualNumberOfUpdatedBalances = underTest.executeTransferRequest(transferRequest);

        // then
        Assertions.assertEquals(expectedNumberOfUpdatedBalances, actualNumberOfUpdatedBalances);
    }

    @Test
    void executeTransferRequest_throwsInsufficientBalanceExceptionWhenNoBalanceAvailable() {

        // given
        UUID fromAccountId = UUID.randomUUID();
        Account fromAccount = Account.builder().id(fromAccountId).balance(BigDecimal.ONE).build();

        UUID toAccountId = UUID.randomUUID();
        Account toAccount = Account.builder().id(toAccountId).balance(BigDecimal.ZERO).build();

        TransferRequest transferRequest = TransferRequest.builder()
                .from(fromAccountId)
                .to(toAccountId)
                .amount(BigDecimal.TEN)
                .occurredAt(Instant.now())
                .build();
        // and
        given(accountServiceMock.getAccountsById(List.of(fromAccountId, toAccountId)))
                .willReturn(Map.of(fromAccountId, fromAccount, toAccountId, toAccount));

        // when-then
        assertThrows(InsufficientBalanceInAccountException.class,
                () -> underTest.executeTransferRequest(transferRequest));
    }

    @Test
    void executeTransferRequest_throwsUpdateBalanceExceptionInCaseTheAffectedAccountsDoesNotEqualToTwo() {

        // given
        UUID fromAccountId = UUID.randomUUID();
        Account fromAccount = Account.builder().id(fromAccountId).balance(BigDecimal.TEN).build();

        UUID toAccountId = UUID.randomUUID();
        Account toAccount = Account.builder().id(toAccountId).balance(BigDecimal.ZERO).build();

        TransferRequest transferRequest = TransferRequest.builder()
                .from(fromAccountId)
                .to(toAccountId)
                .amount(BigDecimal.ONE)
                .occurredAt(Instant.now())
                .build();
        // and
        given(accountServiceMock.getAccountsById(List.of(fromAccountId, toAccountId)))
                .willReturn(Map.of(fromAccountId, fromAccount, toAccountId, toAccount));

        given(accountServiceMock.updateAccountsBalances(List.of(
                fromAccount.toBuilder().balance(BigDecimal.TEN.subtract(BigDecimal.ONE)).build(),
                toAccount.toBuilder().balance(BigDecimal.ZERO.add(BigDecimal.ONE)).build()))
        ).willReturn(1);

        // when-then
        assertThrows(UpdateBalanceException.class, () -> underTest.executeTransferRequest(transferRequest));
    }

}
