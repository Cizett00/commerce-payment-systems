package com.example.commercepaymentsystems.point;

import org.springframework.stereotype.Service;

@Service
public interface PointService {
    void restoreUsedPoint(Long customerId, Long points);
    boolean verifyPoint(Long userId, Long points);
    void usePoint(Long userId, Long points);
}
