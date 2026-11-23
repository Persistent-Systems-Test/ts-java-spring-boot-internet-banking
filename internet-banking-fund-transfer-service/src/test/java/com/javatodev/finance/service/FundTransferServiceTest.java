package com.javatodev.finance.service;

import com.javatodev.finance.exception.GlobalErrorCode;
import com.javatodev.finance.exception.InvalidTransferAmountException;
import com.javatodev.finance.exception.TransferLimitExceededException;
import com.javatodev.finance.model.TransactionStatus;
import com.javatodev.finance.model.dto.request.FundTransferRequest;
import com.javatodev.finance.model.dto.response.FundTransferResponse;
import com.javatodev.finance.model.entity.FundTransferEntity;
import com.javatodev.finance.model.repository.FundTransferRepository;
import com.javatodev.finance.service.rest.client.BankingCoreFeignClient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundTransferServiceTest {

    @Mock
    private FundTransferRepository fundTransferRepository;

    @Mock
    private BankingCoreFeignClient bankingCoreFeignClient;

    @InjectMocks
    private FundTransferService fundTransferService;

    private FundTransferRequest request(String amount) {
        FundTransferRequest request = new FundTransferRequest();
        request.setFromAccount("100015003000");
        request.setToAccount("100015003001");
        request.setAmount(amount == null ? null : new BigDecimal(amount));
        return request;
    }

    private List<TransactionStatus> stubSuccessfulTransfer() {
        List<TransactionStatus> savedStatuses = new ArrayList<>();
        when(fundTransferRepository.save(any(FundTransferEntity.class))).thenAnswer(invocation -> {
            FundTransferEntity entity = invocation.getArgument(0);
            savedStatuses.add(entity.getStatus());
            return entity;
        });
        FundTransferResponse coreResponse = new FundTransferResponse();
        coreResponse.setTransactionId("TXN-1");
        when(bankingCoreFeignClient.fundTransfer(any(FundTransferRequest.class))).thenReturn(coreResponse);
        return savedStatuses;
    }

    private void assertNothingPersistedOrSent() {
        verify(fundTransferRepository, never()).save(any());
        verifyNoInteractions(bankingCoreFeignClient);
    }

    @Test
    void fundTransfer_amountEqualToLimit_transferCompleted() {
        List<TransactionStatus> savedStatuses = stubSuccessfulTransfer();

        FundTransferResponse response = fundTransferService.fundTransfer(request("100000"));

        assertThat(response.getTransactionId()).isEqualTo("TXN-1");
        assertThat(response.getMessage()).isEqualTo("Fund Transfer Successfully Completed");
        assertThat(savedStatuses).containsExactly(TransactionStatus.PENDING, TransactionStatus.SUCCESS);
        verify(bankingCoreFeignClient).fundTransfer(any(FundTransferRequest.class));
    }

    @Test
    void fundTransfer_amountEqualToLimitWithScale_transferCompleted() {
        stubSuccessfulTransfer();

        FundTransferResponse response = fundTransferService.fundTransfer(request("100000.00"));

        assertThat(response.getTransactionId()).isEqualTo("TXN-1");
    }

    @Test
    void fundTransfer_amountJustBelowLimit_transferCompleted() {
        List<TransactionStatus> savedStatuses = stubSuccessfulTransfer();

        fundTransferService.fundTransfer(request("99999.99"));

        assertThat(savedStatuses).containsExactly(TransactionStatus.PENDING, TransactionStatus.SUCCESS);
    }

    @Test
    void fundTransfer_amountJustAboveLimit_throwsTransferLimitExceededAndSavesNothing() {
        assertThatThrownBy(() -> fundTransferService.fundTransfer(request("100000.01")))
            .isInstanceOf(TransferLimitExceededException.class)
            .hasMessage("Transfer amount exceeds the per-transaction limit of 100000")
            .extracting("code").isEqualTo(GlobalErrorCode.TRANSFER_LIMIT_EXCEEDED);

        assertNothingPersistedOrSent();
    }

    @Test
    void fundTransfer_amountFarAboveLimit_throwsTransferLimitExceededAndSavesNothing() {
        assertThatThrownBy(() -> fundTransferService.fundTransfer(request("250000")))
            .isInstanceOf(TransferLimitExceededException.class);

        assertNothingPersistedOrSent();
    }

    @Test
    void fundTransfer_nullAmount_throwsInvalidTransferAmountAndSavesNothing() {
        assertThatThrownBy(() -> fundTransferService.fundTransfer(request(null)))
            .isInstanceOf(InvalidTransferAmountException.class)
            .extracting("code").isEqualTo(GlobalErrorCode.INVALID_TRANSFER_AMOUNT);

        assertNothingPersistedOrSent();
    }

    @Test
    void fundTransfer_zeroAmount_throwsInvalidTransferAmountAndSavesNothing() {
        assertThatThrownBy(() -> fundTransferService.fundTransfer(request("0")))
            .isInstanceOf(InvalidTransferAmountException.class);

        assertNothingPersistedOrSent();
    }

    @Test
    void fundTransfer_negativeAmount_throwsInvalidTransferAmountAndSavesNothing() {
        assertThatThrownBy(() -> fundTransferService.fundTransfer(request("-1")))
            .isInstanceOf(InvalidTransferAmountException.class);

        assertNothingPersistedOrSent();
    }

    @Test
    void fundTransfer_smallestPositiveAmount_transferCompleted() {
        List<TransactionStatus> savedStatuses = stubSuccessfulTransfer();

        fundTransferService.fundTransfer(request("0.01"));

        assertThat(savedStatuses).containsExactly(TransactionStatus.PENDING, TransactionStatus.SUCCESS);
    }
}
