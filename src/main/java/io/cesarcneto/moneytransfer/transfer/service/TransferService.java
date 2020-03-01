package io.cesarcneto.moneytransfer.transfer.service;

import io.cesarcneto.moneytransfer.account.model.Account;
import io.cesarcneto.moneytransfer.account.service.AccountService;
import io.cesarcneto.moneytransfer.transfer.exception.InsufficientBalanceInAccountException;
import io.cesarcneto.moneytransfer.transfer.exception.UpdateBalanceException;
import io.cesarcneto.moneytransfer.transfer.model.TransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class TransferService {

    private final Jdbi jdbi;
    private final AccountService accountService;

    public void performTransferRequest(TransferRequest transferRequest) {
        jdbi.inTransaction(TransactionIsolationLevel.READ_COMMITTED, handle -> executeTransferRequest(transferRequest));
    }

    // Visible for unit testing
    int executeTransferRequest(TransferRequest transferRequest) {
        Map<UUID, Account> accountsById = accountService.getAccountsById(
                List.of(transferRequest.getFrom(), transferRequest.getTo())
        );

        Account fromAccount = accountsById.get(transferRequest.getFrom());
        Account toAccount = accountsById.get(transferRequest.getTo());

        BigDecimal transferAmount = transferRequest.getAmount();
        assertAccountHasBalance(fromAccount, transferAmount);

        Account newFromAccount = performDebit(fromAccount, transferAmount);
        Account newToAccount = performCredit(toAccount, transferAmount);
        // We could store such operations in a transfer facts table here

        int numberOfUpdates = accountService.updateAccountsBalances(List.of(newFromAccount, newToAccount));
        if(numberOfUpdates != 2) {
            throw new UpdateBalanceException(format(
                    "The transfer occurredAt %s with purpose '%s' from %s to %s in the amount of %s failed",
                    transferRequest.getOccurredAt(),
                    transferRequest.getPurpose(),
                    transferRequest.getFrom(),
                    transferRequest.getTo(),
                    transferRequest.getAmount()
            ));
        }

        return numberOfUpdates;
    }

    private Account performCredit(Account account, BigDecimal amount) {
        return account.toBuilder().balance(account.getBalance().add(amount)).build();
    }

    private Account performDebit(Account account, BigDecimal amount) {
        return account.toBuilder().balance(account.getBalance().subtract(amount)).build();
    }

    private void assertAccountHasBalance(Account account, BigDecimal amount) {
        if(account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceInAccountException(account.getId().toString());
        }
    }
}
