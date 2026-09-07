package com.smartmenu.ai.dto;

import com.smartmenu.ai.model.Ingredient;

public final class IngredientMapper {

    private IngredientMapper() {
    }

    public static IngredientResponse toResponse(Ingredient ingredient) {
        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getQuantity(),
                ingredient.getExpirationDate()
        );
    }
}
