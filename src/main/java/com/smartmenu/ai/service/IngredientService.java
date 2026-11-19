package com.smartmenu.ai.service;

import com.smartmenu.ai.dto.IngredientMapper;
import com.smartmenu.ai.dto.IngredientRequest;
import com.smartmenu.ai.dto.IngredientResponse;
import com.smartmenu.ai.infra.exception.BusinessException;
import com.smartmenu.ai.infra.exception.ResourceNotFoundException;
import com.smartmenu.ai.model.Ingredient;
import com.smartmenu.ai.repository.IngredientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class IngredientService {

    private static final int DEFAULT_EXPIRATION_WINDOW_DAYS = 7;
    private static final int MAX_EXPIRATION_WINDOW_DAYS = 90;

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional
    public IngredientResponse create(IngredientRequest request) {
        Ingredient ingredient = new Ingredient(request.name(), request.quantity(), request.expirationDate());
        return IngredientMapper.toResponse(ingredientRepository.save(ingredient));
    }

    @Transactional(readOnly = true)
    public IngredientResponse findById(Long id) {
        return IngredientMapper.toResponse(getIngredient(id));
    }

    @Transactional(readOnly = true)
    public Page<IngredientResponse> findAll(Pageable pageable) {
        return ingredientRepository.findAll(pageable).map(IngredientMapper::toResponse);
    }

    /**
     * Lists ingredients with available stock whose expiration date falls
     * between today and the requested number of days, ordered by urgency.
     */
    @Transactional(readOnly = true)
    public List<IngredientResponse> findExpiring(int withinDays) {
        if (withinDays < 0 || withinDays > MAX_EXPIRATION_WINDOW_DAYS) {
            throw new BusinessException(
                    "O período deve estar entre 0 e " + MAX_EXPIRATION_WINDOW_DAYS + " dias"
            );
        }

        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.plusDays(withinDays);

        return ingredientRepository
                .findByExpirationDateBetweenAndQuantityGreaterThanOrderByExpirationDateAsc(
                        today,
                        lastDay,
                        BigDecimal.ZERO
                )
                .stream()
                .map(IngredientMapper::toResponse)
                .toList();
    }

    @Transactional
    public IngredientResponse update(Long id, IngredientRequest request) {
        Ingredient ingredient = getIngredient(id);
        ingredient.setName(request.name());
        ingredient.setQuantity(request.quantity());
        ingredient.setExpirationDate(request.expirationDate());
        return IngredientMapper.toResponse(ingredient);
    }

    @Transactional
    public void delete(Long id) {
        Ingredient ingredient = getIngredient(id);
        ingredientRepository.delete(ingredient);
    }

    private Ingredient getIngredient(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente não encontrado: " + id));
    }
}
