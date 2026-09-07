package com.smartmenu.ai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IngredientResponse(
        Long id,
        String name,
        BigDecimal quantity,
        LocalDate expirationDate
) {
}
