package com.javatodev.finance.controller;

import com.javatodev.finance.exception.GlobalExceptionHandler;
import com.javatodev.finance.model.dto.request.FundTransferRequest;
import com.javatodev.finance.model.dto.response.FundTransferResponse;
import com.javatodev.finance.model.entity.FundTransferEntity;
import com.javatodev.finance.model.repository.FundTransferRepository;
import com.javatodev.finance.service.FundTransferService;
import com.javatodev.finance.service.rest.client.BankingCoreFeignClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FundTransferControllerTest {

    @Mock
    private FundTransferRepository fundTransferRepository;

    @Mock
    private BankingCoreFeignClient bankingCoreFeignClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FundTransferService service = new FundTransferService(fundTransferRepository, bankingCoreFeignClient);
        mockMvc = MockMvcBuilders.standaloneSetup(new FundTransferController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    private String body(String amount) {
        return "{\"fromAccount\":\"100015003000\",\"toAccount\":\"100015003001\",\"amount\":" + amount + "}";
    }

    @Test
    void sendFundTransfer_amountAboveLimit_returnsBadRequestAndSavesNothing() throws Exception {
        mockMvc.perform(post("/api/v1/transfer").contentType(MediaType.APPLICATION_JSON).content(body("100000.01")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FUND-TRANSFER-SERVICE-1000"))
            .andExpect(jsonPath("$.message").value("Transfer amount exceeds the per-transaction limit of 100000"));

        verify(fundTransferRepository, never()).save(any());
        verifyNoInteractions(bankingCoreFeignClient);
    }

    @Test
    void sendFundTransfer_amountEqualToLimit_returnsOk() throws Exception {
        when(fundTransferRepository.save(any(FundTransferEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        FundTransferResponse coreResponse = new FundTransferResponse();
        coreResponse.setTransactionId("TXN-1");
        when(bankingCoreFeignClient.fundTransfer(any(FundTransferRequest.class))).thenReturn(coreResponse);

        mockMvc.perform(post("/api/v1/transfer").contentType(MediaType.APPLICATION_JSON).content(body("100000")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").value("TXN-1"));
    }
}
