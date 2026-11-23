package com.javatodev.finance.exception;

public class InvalidTransferAmountException extends SimpleBankingGlobalException {
    public InvalidTransferAmountException(String message, String code) {
        super(code, message);
    }
}
