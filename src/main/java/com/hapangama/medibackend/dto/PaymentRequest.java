package com.hapangama.medibackend.dto;

import com.hapangama.medibackend.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long appointmentId;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private String cardNumber;
    private String cvv;
}
