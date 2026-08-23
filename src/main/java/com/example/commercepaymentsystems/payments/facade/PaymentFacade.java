package com.example.commercepaymentsystems.payments.facade;

import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.entity.OrderStatus;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.port.PaymentGateway;
import com.example.commercepaymentsystems.payments.port.PaymentGatewayResponse;
import com.example.commercepaymentsystems.payments.service.PaymentCommandService;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import com.example.commercepaymentsystems.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFacade {
    private final PaymentService paymentService;
    private final PaymentCommandService commandService;
    private final PaymentGateway paymentGateway;
    private final PointService pointService;

    public PaymentConfirmResponse paymentConfirm(Long userId, PaymentConfirmRequest confirmRequest) {
        Payment payment = paymentService.findByOrderIdWithOrder(confirmRequest.orderId());
        Order order = payment.getOrder();
        Long customerId = order.getCustomer().getId();

        //주문자와 사용자 일치 확인
        if (!customerId.equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        //결제 중복 확인
        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        //주문 상태 전이 가능 여부 확인
        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        //포인트 사용 가능 여부 확인
        if (!pointService.verifyPoint(customerId, payment.getPointUsed())) {
           throw new RuntimeException("Invalid point");
        }

        //PG 사에서 실결제 정보 확인
        PaymentGatewayResponse paymentInfo = paymentGateway.getPayment(payment.getPortoneId());

        //결제 금액과 주문 금액 검증
        if (!confirmRequest.paymentPrice().equals(paymentInfo.totalAmount())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        //결제 실패 시 payment 상태를 FAILED, order 상태를 CANCELLED로
        //상품 재고 전량 복구
        if(confirmRequest.result().equals("FAIL")) {
            commandService.failPaymentAndOrder(order.getId());

            throw new BusinessException(ErrorCode.PG_FAILURE);
        }

        return commandService.approvePaymentAndOrder(order.getId());
    }
}
