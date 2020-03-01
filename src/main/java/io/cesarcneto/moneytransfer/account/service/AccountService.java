package io.cesarcneto.moneytransfer.account.service;

import io.cesarcneto.moneytransfer.account.model.Account;
import io.cesarcneto.moneytransfer.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account getById(UUID accountId) {
        return accountRepository.findById(accountId).orElseThrow();
    }

    public Map<UUID, Account> getAccountsById(List<UUID> accountIds) {
        return accountRepository.getAccountsByIdForUpdate(accountIds)
        .stream()
        .collect(Collectors.toMap(Account::getId, Function.identity()));
    }

    public Account createAccount(Account account) {
        UUID id = accountRepository.save(account);
        return Account.builder().id(id).balance(account.getBalance()).build();
    }

    public int updateAccountsBalances(List<Account> accountList) {
        return Arrays.stream(accountRepository.updateAccountsBalances(accountList)).sum();
    }
}
