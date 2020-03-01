package io.cesarcneto.moneytransfer.account.service;

import io.cesarcneto.moneytransfer.account.model.Account;
import io.cesarcneto.moneytransfer.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepositoryMock;

    private AccountService underTest;

    @BeforeEach
    void setup() {
        initMocks(this);
        underTest = new AccountService(accountRepositoryMock);
    }

    @Test
    void getById_happyPath() {

        // given
        UUID someAccountId = UUID.randomUUID();
        UUID someAccountVersion = UUID.randomUUID();
        Account expectedAccount = Account.builder()
                .id(someAccountId)
                .balance(BigDecimal.TEN)
                .version(someAccountVersion)
                .build();

        given(accountRepositoryMock.findById(someAccountId)).willReturn(Optional.of(expectedAccount));

        // when
        Account actualAccount = underTest.getById(someAccountId);

        // then
        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    void getById_willThrowNoSuchElementIfIdNotFound() {

        // given
        UUID someAccountId = UUID.randomUUID();
        given(accountRepositoryMock.findById(someAccountId)).willReturn(Optional.empty());

        // when-then
        assertThrows(NoSuchElementException.class, () -> underTest.getById(someAccountId));
    }

    @Test
    void getAccountsById_happyPath() {

        // given
        UUID someAccountId1 = UUID.randomUUID();
        UUID someAccountVersion1 = UUID.randomUUID();
        Account expectedAccount1 = Account.builder()
                .id(someAccountId1)
                .balance(BigDecimal.TEN)
                .version(someAccountVersion1)
                .build();

        UUID someAccountId2 = UUID.randomUUID();
        UUID someAccountVersion2 = UUID.randomUUID();
        Account expectedAccount2 = Account.builder()
                .id(someAccountId2)
                .balance(BigDecimal.TEN)
                .version(someAccountVersion2)
                .build();

        Map<UUID, Account> expectedAccountsById = Map.of(
                someAccountId1, expectedAccount1,
                someAccountId2, expectedAccount2
        );

        // and
        List<UUID> accountIds = List.of(someAccountId1, someAccountId2);
        given(accountRepositoryMock.getAccountsByIdForUpdate(accountIds))
                .willReturn(List.of(expectedAccount1, expectedAccount2));

        // when
        Map<UUID, Account> actualAccountsById = underTest.getAccountsById(accountIds);

        // then
        assertEquals(expectedAccountsById, actualAccountsById);
    }

    @Test
    void getAccountsById_returnsAnEmptyMapIfIdsAreNotFound() {

        // given
        Map<UUID, Account> expectedAccountsById = Map.of();

        // and
        List<UUID> accountIds = List.of();
        given(accountRepositoryMock.getAccountsByIdForUpdate(accountIds)).willReturn(List.of());

        // when
        Map<UUID, Account> actualAccountsById = underTest.getAccountsById(accountIds);

        // then
        assertEquals(expectedAccountsById, actualAccountsById);
    }

    @Test
    void createAccount_happyPath() {

        // given
        UUID accountId = UUID.randomUUID();

        Account account = Account.builder().balance(BigDecimal.TEN).build();
        Account expectedAccount = Account.builder().id(accountId).balance(BigDecimal.TEN).build();

        given(accountRepositoryMock.save(account)).willReturn(accountId);

        // when
        Account actualAccount = underTest.createAccount(account);

        // then
        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    void createAccount_throwsExceptionInCaseRepositorySaveDoes() {

        // given
        Account account = Account.builder().balance(BigDecimal.TEN).build();
        given(accountRepositoryMock.save(account)).willThrow(new RuntimeException());

        // when-then
        assertThrows(RuntimeException.class, () -> underTest.createAccount(account));
    }

    @Test
    void updateAccountsBalances_happyPath() {
        // given
        UUID someAccountId1 = UUID.randomUUID();
        UUID someAccountVersion1 = UUID.randomUUID();
        Account someAccount1 = Account.builder()
                .id(someAccountId1)
                .balance(BigDecimal.TEN)
                .version(someAccountVersion1)
                .build();

        UUID someAccountId2 = UUID.randomUUID();
        UUID someAccountVersion2 = UUID.randomUUID();
        Account someAccount2 = Account.builder()
                .id(someAccountId2)
                .balance(BigDecimal.TEN)
                .version(someAccountVersion2)
                .build();

        List<Account> accountList = List.of(someAccount1, someAccount2);
        int expectedAmountOfUpdatedAccounts = accountList.size();

        // and
        given(accountRepositoryMock.updateAccountsBalances(accountList)).willReturn(new int[]{1, 1});

        // when
        int actualAmountOfUpdatedAccounts = underTest.updateAccountsBalances(accountList);

        // then
        assertEquals(expectedAmountOfUpdatedAccounts, actualAmountOfUpdatedAccounts);
    }

}
