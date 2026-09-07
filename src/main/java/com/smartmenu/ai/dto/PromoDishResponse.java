package com.smartmenu.ai.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PromoDishResponse(
        Long id,
        String name,
        String marketingDescription,
        BigDecimal originalPrice,
        BigDecimal promoPrice,
        Instant createdAt
) {
}
