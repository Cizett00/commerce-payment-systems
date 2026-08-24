package com.example.commercepaymentsystems.orders.dto.request;

import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

// 주문 생성
public record CreateOrderRequest(List<Long> cartItemIds,
                                 @PositiveOrZero(message = "사용 포인트는 0 이상이어야 합니다.") Long pointUsed) {

    // null이 들어오면 빈 리스트로 변화
    // 빈 리스트는 전체 장비구니 주문으로 처리
    public CreateOrderRequest {
        if (cartItemIds == null) {
            cartItemIds = List.of();
        }

        // 포인트를 입력하지 않으면 0포인트 사용
        if (pointUsed == null) {
            pointUsed = 0L;
        }
    }
}
