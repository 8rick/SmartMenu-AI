package com.smartmenu.ai.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record GeneratePromoDishRequest(
        @NotEmpty(message = "Informe ao menos um ingrediente")
        List<@NotNull(message = "O ID do ingrediente é obrigatório")
        @Positive(message = "O ID do ingrediente deve ser positivo") Long> ingredientIds
) {
}
