package io.cesarcneto.moneytransfer.account.service;

import io.cesarcneto.moneytransfer.account.model.Account;
import io.cesarcneto.moneytransfer.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account getById(UUID accountId) {
        return accountRepository.findById(accountId).orElseThrow();
    }

    public Account createAccount(Account account) {
        UUID id = accountRepository.save(account);
        return Account.builder().id(id).balance(account.getBalance()).build();
    }
}
