package io.cesarcneto.moneytransfer.transfer.exception;

import static java.lang.String.format;

public class InsufficientBalanceInAccountException extends RuntimeException {

    public InsufficientBalanceInAccountException(String accountId) {
        super(format("Insufficient balance available in account %s", accountId));
    }

}
