package com.example.commercepaymentsystems.point;

import com.example.commercepaymentsystems.customers.entity.Customers;
import com.example.commercepaymentsystems.payments.entity.Payment;
import org.springframework.stereotype.Service;

@Service
public interface PointService {
    void restoreUse(Customers customer, Payment payment, Long points);
    Long getBalance(Long customerId);
    void use(Customers customer, Payment payment, Long points);
}
