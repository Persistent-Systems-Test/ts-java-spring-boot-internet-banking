package com.javatodev.finance.service;

import com.javatodev.finance.model.TransactionStatus;
import com.javatodev.finance.model.entity.UtilityPaymentEntity;
import com.javatodev.finance.model.rest.request.UtilityPaymentRequest;
import com.javatodev.finance.model.rest.response.UtilityPaymentResponse;
import com.javatodev.finance.repository.UtilityPaymentRepository;
import com.javatodev.finance.service.rest.BankingCoreRestClient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilityPaymentServiceTest {

    @Mock
    private UtilityPaymentRepository utilityPaymentRepository;

    @Mock
    private BankingCoreRestClient bankingCoreRestClient;

    @InjectMocks
    private UtilityPaymentService utilityPaymentService;

    @Test
    void utilPayment_savesProcessingThenSuccess_andReturnsCoreTransactionId() {
        UtilityPaymentRequest request = new UtilityPaymentRequest();
        request.setProviderId(1L);
        request.setAmount(new BigDecimal("150.00"));
        request.setReferenceNumber("REF-001");
        request.setAccount("100015003000");

        List<TransactionStatus> savedStatuses = new ArrayList<>();
        when(utilityPaymentRepository.save(any(UtilityPaymentEntity.class))).thenAnswer(invocation -> {
            UtilityPaymentEntity entity = invocation.getArgument(0);
            savedStatuses.add(entity.getStatus());
            return entity;
        });
        when(bankingCoreRestClient.utilityPayment(request))
                .thenReturn(UtilityPaymentResponse.builder().transactionId("TXN-123").build());

        UtilityPaymentResponse response = utilityPaymentService.utilPayment(request);

        assertThat(response.getTransactionId()).isEqualTo("TXN-123");
        assertThat(response.getMessage()).isEqualTo("Utility Payment Successfully Processed");
        assertThat(savedStatuses).containsExactly(TransactionStatus.PROCESSING, TransactionStatus.SUCCESS);
        verify(utilityPaymentRepository, times(2)).save(any(UtilityPaymentEntity.class));
        verify(bankingCoreRestClient).utilityPayment(request);
    }
}
