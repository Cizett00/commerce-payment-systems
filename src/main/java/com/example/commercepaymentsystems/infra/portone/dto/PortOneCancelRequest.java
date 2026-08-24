package com.example.commercepaymentsystems.infra.portone.dto;

public record PortOneCancelRequest (
        String reason,
        String storeId
) {
}
