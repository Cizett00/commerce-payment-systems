package com.example.commercepaymentsystems.payments.service;

import com.example.commercepaymentsystems.cart.service.CartService;
import com.example.commercepaymentsystems.customers.entity.Customers;
import com.example.commercepaymentsystems.customers.service.customers.CustomersService;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.entity.OrderItem;
import com.example.commercepaymentsystems.orders.service.OrderService;
import com.example.commercepaymentsystems.payments.dto.PaymentCancelResponse;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.point.PointService;
import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CartService cartService;
    private final CustomersService  customersService;
    private final PointService pointService;

    @Transactional
    public void failPaymentAndOrder(Long orderId) {
        Payment payment = paymentService.findByOrderIdWithOrder(orderId);
        Order order = payment.getOrder();

        paymentService.failPayment(payment);
        orderService.cancelOrder(order);

        //포인트 계산 후 포인트 복구
        restorePoints(payment.getOrder().getCustomer().getId(), payment);

        restoreStock(order);
    }

    @Transactional
    public PaymentConfirmResponse approvePaymentAndOrder(Long orderId) {
        Payment payment = paymentService.findByOrderIdWithOrder(orderId);
        Order order = payment.getOrder();
        Long customerId = order.getCustomer().getId();
        Customers customer = customersService.findById(customerId);

        paymentService.confirmPayment(payment);
        orderService.confirmOrder(order);
        pointService.use(customer, payment, payment.getPointUsed());

        cartService.removeAllItems(order.getCustomer().getId());

        return new PaymentConfirmResponse(
                payment.getId(),
                orderId,
                payment.getFinalPrice(),
                payment.getStatus().name(),
                order.getOrderStatus().name()
        );
    }

    @Transactional
    public PaymentCancelResponse cancelPaymentAndOrder(Long id) {
        Payment payment = paymentService.findByOrderIdWithOrder(id);
        Order order = payment.getOrder();
        Long customerId = order.getCustomer().getId();

        paymentService.cancelPayment(payment);
        orderService.cancelOrder(order);

        restorePoints(customerId, payment);

        return new PaymentCancelResponse(
                payment.getId(),
                order.getId(),
                payment.getPortoneId(),
                payment.getStatus().name(),
                order.getStatus().name(),
                "결제가 취소되었습니다."
        );
    }

    private void restoreStock(Order order) {
        List<OrderItem> items = orderService.getOrderItems(order.getId());

        for (OrderItem item : items) {
            Product product = productService.findEntityById(item.getProduct().getId());
            product.restoreStock(item.getQuantity());
        }
    }

    private void restorePoints(Long customerId, Payment payment) {
        Customers customer = customersService.findById(customerId);

        //복구할 포인트 계산
        Long pointsToRestore = payment.getSavedPoints() - payment.getPointUsed();
        pointService.restoreUse(customer, payment, pointsToRestore);
    }
}
