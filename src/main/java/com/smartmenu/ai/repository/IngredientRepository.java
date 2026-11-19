package com.smartmenu.ai.repository;

import com.smartmenu.ai.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findByExpirationDateBetweenAndQuantityGreaterThanOrderByExpirationDateAsc(
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minimumQuantity
    );
}
