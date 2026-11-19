package com.smartmenu.ai.service;

import com.smartmenu.ai.dto.IngredientResponse;
import com.smartmenu.ai.infra.exception.BusinessException;
import com.smartmenu.ai.model.Ingredient;
import com.smartmenu.ai.repository.IngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    private IngredientService ingredientService;

    @BeforeEach
    void setUp() {
        ingredientService = new IngredientService(ingredientRepository);
    }

    @Test
    void shouldReturnExpiringIngredientsOrderedByRepositoryQuery() {
        LocalDate today = LocalDate.now();
        Ingredient first = new Ingredient("Tomate", new BigDecimal("2.000"), today.plusDays(1));
        Ingredient second = new Ingredient("Queijo", new BigDecimal("1.500"), today.plusDays(5));

        when(ingredientRepository
                .findByExpirationDateBetweenAndQuantityGreaterThanOrderByExpirationDateAsc(
                        today, today.plusDays(7), BigDecimal.ZERO
                ))
                .thenReturn(List.of(first, second));

        List<IngredientResponse> result = ingredientService.findExpiring(7);

        assertEquals(2, result.size());
        assertEquals("Tomate", result.get(0).name());
        assertEquals("Queijo", result.get(1).name());
        verify(ingredientRepository)
                .findByExpirationDateBetweenAndQuantityGreaterThanOrderByExpirationDateAsc(
                        today, today.plusDays(7), BigDecimal.ZERO
                );
    }

    @Test
    void shouldRejectNegativeExpirationWindow() {
        assertThrows(BusinessException.class, () -> ingredientService.findExpiring(-1));
        verifyNoInteractions(ingredientRepository);
    }

    @Test
    void shouldRejectExpirationWindowGreaterThanNinetyDays() {
        assertThrows(BusinessException.class, () -> ingredientService.findExpiring(91));
        verifyNoInteractions(ingredientRepository);
    }

    @Test
    void shouldAllowZeroDayWindow() {
        LocalDate today = LocalDate.now();
        when(ingredientRepository
                .findByExpirationDateBetweenAndQuantityGreaterThanOrderByExpirationDateAsc(
                        today, today, BigDecimal.ZERO
                ))
                .thenReturn(List.of());

        assertEquals(List.of(), ingredientService.findExpiring(0));
    }
}
