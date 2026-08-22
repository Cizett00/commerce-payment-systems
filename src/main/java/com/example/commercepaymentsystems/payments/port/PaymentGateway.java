package com.example.commercepaymentsystems.payments.port;

public interface PaymentGateway {
    PaymentGatewayResponse getPayment(String paymentId);
    void cancelPayment(String paymentId, String reason);
}
