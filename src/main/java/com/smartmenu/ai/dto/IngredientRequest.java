package com.smartmenu.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IngredientRequest(
        @NotBlank(message = "O nome do ingrediente é obrigatório")
        String name,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        BigDecimal quantity,

        @NotNull(message = "A data de validade é obrigatória")
        LocalDate expirationDate
) {
}
