package com.javatodev.finance.exception;

public class TransferLimitExceededException extends SimpleBankingGlobalException {
    public TransferLimitExceededException(String message, String code) {
        super(code, message);
    }
}
