package com.smartmenu.ai.controller;

import com.smartmenu.ai.dto.GeneratePromoDishRequest;
import com.smartmenu.ai.dto.PageResponse;
import com.smartmenu.ai.dto.PromoDishResponse;
import com.smartmenu.ai.service.AiRecipeGeneratorService;
import com.smartmenu.ai.service.PromoDishService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promo-dishes")
@Validated
public class PromoDishController {

    private final PromoDishService promoDishService;
    private final AiRecipeGeneratorService aiRecipeGeneratorService;

    public PromoDishController(
            PromoDishService promoDishService,
            AiRecipeGeneratorService aiRecipeGeneratorService
    ) {
        this.promoDishService = promoDishService;
        this.aiRecipeGeneratorService = aiRecipeGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<PromoDishResponse> generate(@Valid @RequestBody GeneratePromoDishRequest request) {
        PromoDishResponse response = aiRecipeGeneratorService.generate(request.ingredientIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Automatically chooses the soonest-expiring ingredients with positive stock.
     * Defaults to a 7-day window and at most 5 ingredients.
     */
    @PostMapping("/generate/automatic")
    public ResponseEntity<PromoDishResponse> generateAutomatically(
            @RequestParam(defaultValue = "7") @Min(0) @Max(90) int withinDays,
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) int limit
    ) {
        PromoDishResponse response = aiRecipeGeneratorService.generateFromExpiringStock(withinDays, limit);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public PromoDishResponse findById(@PathVariable Long id) {
        return promoDishService.findById(id);
    }

    @GetMapping
    public PageResponse<PromoDishResponse> findAll(@PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(promoDishService.findAll(pageable));
    }
}
